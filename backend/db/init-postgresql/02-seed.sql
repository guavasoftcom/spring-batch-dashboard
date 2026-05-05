-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
-- Dates are computed relative to CURRENT_DATE so the data always looks "recent" no matter when
-- the container is provisioned. Per-step counts, durations, and per-execution statuses are
-- randomized so each engine's dashboard view shows distinct values; today's anchor execution
-- per job is pinned to a known status so repository tests can rely on:
--   * exec 90  (today's dailyImportJob)     → COMPLETED  (anchors NEWEST_DAILY_EXEC)
--   * exec 120 (today's reconcileLedgerJob) → FAILED     (anchors findMostRecentFailed)
--   * exec 132 (today's sendDigestEmailJob) → STARTED    (anchors the in-flight tile)
--
-- Layout:
--   * dailyImportJob:     90 runs, one per day going back 90 days. Each ~80% COMPLETED /
--                         ~20% FAILED (today is forced COMPLETED).
--                         Instance/exec IDs 1..90, step IDs 1..180 (read+write per exec).
--   * reconcileLedgerJob: 30 runs, one per day going back 30 days. Each ~75% COMPLETED /
--                         ~25% FAILED (today is forced FAILED).
--                         Instance/exec IDs 91..120, step IDs 181..210.
--   * sendDigestEmailJob: 12 runs, one per week going back ~84 days. 11 historical runs
--                         ~80% COMPLETED / ~20% FAILED, plus today's STARTED in-flight run.
--                         Instance/exec IDs 121..132, step IDs 211..222.

-- ===== Job instances =====
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT n,        0, 'dailyImportJob',     LPAD(TO_HEX(n),        32, '0') FROM generate_series(1, 90)  AS n;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 90 + n,   0, 'reconcileLedgerJob', LPAD(TO_HEX(90 + n),   32, '0') FROM generate_series(1, 30)  AS n;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 120 + n,  0, 'sendDigestEmailJob', LPAD(TO_HEX(120 + n),  32, '0') FROM generate_series(1, 12)  AS n;

-- ===== Job executions =====
-- Daily import: today (n=90) pinned to COMPLETED; past days flip ~20% to FAILED at random.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    n, 2, n,
    create_ts,
    start_ts,
    end_ts,
    status,
    status,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE '' END,
    end_ts
FROM (
    SELECT
        n,
        (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours'                                                                       AS create_ts,
        (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second'                                                              AS start_ts,
        (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second' + (60 + FLOOR(RANDOM() * 241))::int * INTERVAL '1 second'    AS end_ts,
        CASE WHEN n = 90 THEN 'COMPLETED'
             WHEN RANDOM() < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                            AS status
    FROM generate_series(1, 90) AS n
) sub;

-- Reconcile: today (n=30 → exec 120) pinned to FAILED; past days ~75% COMPLETED / ~25% FAILED.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    90 + n, 2, 90 + n,
    create_ts,
    start_ts,
    end_ts,
    status,
    status,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE '' END,
    end_ts
FROM (
    SELECT
        n,
        (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours'                                                                      AS create_ts,
        (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 1 second'                                                             AS start_ts,
        (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 1 second' + (90 + FLOOR(RANDOM() * 211))::int * INTERVAL '1 second'   AS end_ts,
        CASE WHEN n = 30 THEN 'FAILED'
             WHEN RANDOM() < 0.25 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                            AS status
    FROM generate_series(1, 30) AS n
) sub;

-- Digest: today (n=12 → exec 132) pinned to STARTED with NULL end_time; past weeks finished.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    120 + n, CASE WHEN n = 12 THEN 1 ELSE 2 END, 120 + n,
    create_ts,
    start_ts,
    CASE WHEN n = 12 THEN NULL ELSE end_ts END,
    status,
    CASE WHEN n = 12 THEN 'UNKNOWN' ELSE status END,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    CASE WHEN n = 12 THEN start_ts + INTERVAL '29 seconds' ELSE end_ts END
FROM (
    SELECT
        n,
        (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes'                                                                  AS create_ts,
        (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 1 second'                                                         AS start_ts,
        (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 1 second' + (45 + FLOOR(RANDOM() * 121))::int * INTERVAL '1 second' AS end_ts,
        CASE WHEN n = 12 THEN 'STARTED'
             WHEN RANDOM() < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                                        AS status
    FROM generate_series(1, 12) AS n
) sub;

-- ===== Job execution params (one stamp per execution; tests don't read these) =====
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT n,        'run.id',   'java.lang.Long',      n::text,                                       'Y' FROM generate_series(1, 90) AS n;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 90 + n,   'asOfDate', 'java.time.LocalDate', TO_CHAR(CURRENT_DATE - (30 - n), 'YYYY-MM-DD'), 'Y' FROM generate_series(1, 30) AS n;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 120 + n,  'audience', 'java.lang.String',    'weekly-digest',                               'Y' FROM generate_series(1, 12) AS n;

-- ===== Step executions =====
-- readUsersStep: always COMPLETED (the read finishes before the write fails). Random duration
-- and counts produce per-DB visual variety.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * n - 1, 3, 'readUsersStep', n,
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 2 seconds',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 2 seconds' + (20 + FLOOR(RANDOM() * 61))::int * INTERVAL '1 second',
    'COMPLETED',
    (8 + FLOOR(RANDOM() * 5))::int,
    (800 + FLOOR(RANDOM() * 401))::int,
    0,
    (800 + FLOOR(RANDOM() * 401))::int,
    0, 0, 0, 0, 'COMPLETED', '',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 2 seconds' + (20 + FLOOR(RANDOM() * 61))::int * INTERVAL '1 second'
FROM generate_series(1, 90) AS n;

-- writeUsersStep: status follows its execution. FAILED writes get rollback_count = 1.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * n, 3, 'writeUsersStep', n,
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 30 seconds',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds' + (30 + FLOOR(RANDOM() * 91))::int * INTERVAL '1 second',
    je.status,
    (8 + FLOOR(RANDOM() * 5))::int,
    (800 + FLOOR(RANDOM() * 401))::int,
    0,
    (800 + FLOOR(RANDOM() * 401))::int,
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE '' END,
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds' + (30 + FLOOR(RANDOM() * 91))::int * INTERVAL '1 second'
FROM generate_series(1, 90) AS n
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = n;

-- reconcileStep: status matches its execution. FAILED reconciles add a rollback marker.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    180 + n, 3, 'reconcileStep', 90 + n,
    (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 1 second',
    (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 2 seconds',
    (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 2 seconds' + (60 + FLOOR(RANDOM() * 181))::int * INTERVAL '1 second',
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 4 ELSE 5 END,
    500,
    0,
    CASE WHEN je.status = 'FAILED' THEN 400 ELSE 500 END,
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE '' END,
    (CURRENT_DATE - (30 - n)) + INTERVAL '18 hours 2 seconds' + (60 + FLOOR(RANDOM() * 181))::int * INTERVAL '1 second'
FROM generate_series(1, 30) AS n
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 90 + n;

-- composeDigestStep: today (n=12) is in-flight (STARTED, NULL end_time); past weeks finished.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    210 + n,
    CASE WHEN n = 12 THEN 1 ELSE 3 END,
    'composeDigestStep',
    120 + n,
    (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 1 second',
    (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 2 seconds',
    CASE WHEN n = 12 THEN NULL
         ELSE (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 2 seconds' + (45 + FLOOR(RANDOM() * 121))::int * INTERVAL '1 second' END,
    je.status,
    CASE WHEN n = 12 THEN 0 ELSE 2 END,
    CASE WHEN n = 12 THEN 20 ELSE 80 END,
    0,
    CASE WHEN n = 12 THEN 15 WHEN je.status = 'FAILED' THEN 60 ELSE 80 END,
    0, 0, 0,
    0,
    CASE WHEN n = 12 THEN 'EXECUTING' ELSE je.status END,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    (CURRENT_DATE - (7 * (12 - n))) + INTERVAL '9 hours 30 minutes 30 seconds'
FROM generate_series(1, 12) AS n
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 120 + n;

-- ===== Contexts (one per execution / step, required by FKs) =====
INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT je.job_execution_id,
       CASE WHEN je.status = 'FAILED'
            THEN '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"see exit message"}'
            ELSE '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}' END,
       NULL
FROM BATCH_JOB_EXECUTION je;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT step_execution_id,
       '{"@class":"java.util.HashMap","step.bookmark":"' || step_execution_id || '"}',
       NULL
FROM BATCH_STEP_EXECUTION;

-- ===== Bump sequences past seeded IDs so live Spring Batch runs don't collide. =====
SELECT setval('batch_job_instance_seq',  (SELECT MAX(JOB_INSTANCE_ID)  FROM BATCH_JOB_INSTANCE));
SELECT setval('batch_job_execution_seq', (SELECT MAX(JOB_EXECUTION_ID) FROM BATCH_JOB_EXECUTION));
SELECT setval('batch_step_execution_seq', (SELECT MAX(STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION));
