-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
-- Dates are computed relative to CURDATE() so the data always looks "recent" no matter when
-- the container is provisioned. Per-step counts, durations, and per-execution statuses are
-- randomized via RAND() so each engine's dashboard view shows distinct values; today's anchor
-- execution per job is pinned to a known status so repository tests can rely on:
--   * exec 90  (today's dailyImportJob)     → COMPLETED  (anchors NEWEST_DAILY_EXEC)
--   * exec 120 (today's reconcileLedgerJob) → FAILED     (anchors findMostRecentFailed)
--   * exec 132 (today's sendDigestEmailJob) → STARTED    (anchors the in-flight tile)
--
-- Layout:
--   * dailyImportJob:     90 runs (instance/exec IDs 1..90, step IDs 1..180).
--   * reconcileLedgerJob: 30 runs (instance/exec IDs 91..120, step IDs 181..210).
--   * sendDigestEmailJob: 12 runs (instance/exec IDs 121..132, step IDs 211..222).

-- ===== Job instances =====
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90)
SELECT n,        0, 'dailyImportJob',     LPAD(HEX(n),       32, '0') FROM seq;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 30)
SELECT 90 + n,   0, 'reconcileLedgerJob', LPAD(HEX(90 + n),  32, '0') FROM seq;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 12)
SELECT 120 + n,  0, 'sendDigestEmailJob', LPAD(HEX(120 + n), 32, '0') FROM seq;

-- ===== Job executions =====
-- Daily import: today (n=90) pinned to COMPLETED; past days flip ~20% to FAILED at random.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90)
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
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:00') AS create_ts,
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01') AS start_ts,
        DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01'), INTERVAL (60 + FLOOR(RAND() * 241)) SECOND) AS end_ts,
        CASE WHEN n = 90 THEN 'COMPLETED'
             WHEN RAND() < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END AS status
    FROM seq
) sub;

-- Reconcile: today (n=30 → exec 120) pinned to FAILED; past days ~75% COMPLETED / ~25% FAILED.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 30)
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
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:00') AS create_ts,
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:01') AS start_ts,
        DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:01'), INTERVAL (90 + FLOOR(RAND() * 211)) SECOND) AS end_ts,
        CASE WHEN n = 30 THEN 'FAILED'
             WHEN RAND() < 0.25 THEN 'FAILED'
             ELSE 'COMPLETED' END AS status
    FROM seq
) sub;

-- Digest: today (n=12 → exec 132) pinned to STARTED with NULL end_time; past weeks finished.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 12)
SELECT
    120 + n, CASE WHEN n = 12 THEN 1 ELSE 2 END, 120 + n,
    create_ts,
    start_ts,
    CASE WHEN n = 12 THEN NULL ELSE end_ts END,
    status,
    CASE WHEN n = 12 THEN 'UNKNOWN' ELSE status END,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    CASE WHEN n = 12 THEN DATE_ADD(start_ts, INTERVAL 29 SECOND) ELSE end_ts END
FROM (
    SELECT
        n,
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:00') AS create_ts,
        TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:01') AS start_ts,
        DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:01'), INTERVAL (45 + FLOOR(RAND() * 121)) SECOND) AS end_ts,
        CASE WHEN n = 12 THEN 'STARTED'
             WHEN RAND() < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END AS status
    FROM seq
) sub;

-- ===== Job execution params =====
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90)
SELECT n,        'run.id',   'java.lang.Long',      CAST(n AS CHAR),                                                            'Y' FROM seq;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 30)
SELECT 90 + n,   'asOfDate', 'java.time.LocalDate', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '%Y-%m-%d'),         'Y' FROM seq;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 12)
SELECT 120 + n,  'audience', 'java.lang.String',    'weekly-digest',                                                            'Y' FROM seq;

-- ===== Step executions =====
-- readUsersStep: always COMPLETED. Random duration / counts.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90)
SELECT
    2 * n - 1, 3, 'readUsersStep', n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:02'),
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:02'), INTERVAL (20 + FLOOR(RAND() * 61)) SECOND),
    'COMPLETED',
    8 + FLOOR(RAND() * 5),
    800 + FLOOR(RAND() * 401),
    0,
    800 + FLOOR(RAND() * 401),
    0, 0, 0, 0, 'COMPLETED', '',
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:02'), INTERVAL (20 + FLOOR(RAND() * 61)) SECOND)
FROM seq;

-- writeUsersStep: status follows its execution. FAILED writes get rollback_count = 1.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90)
SELECT
    2 * n, 3, 'writeUsersStep', n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:30'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'),
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'), INTERVAL (30 + FLOOR(RAND() * 91)) SECOND),
    je.status,
    8 + FLOOR(RAND() * 5),
    800 + FLOOR(RAND() * 401),
    0,
    800 + FLOOR(RAND() * 401),
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE '' END,
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'), INTERVAL (30 + FLOOR(RAND() * 91)) SECOND)
FROM seq
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = n;

-- reconcileStep: status matches its execution.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 30)
SELECT
    180 + n, 3, 'reconcileStep', 90 + n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:01'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:02'),
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:02'), INTERVAL (60 + FLOOR(RAND() * 181)) SECOND),
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 4 ELSE 5 END,
    500,
    0,
    CASE WHEN je.status = 'FAILED' THEN 400 ELSE 500 END,
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE '' END,
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (30 - n) DAY), '18:00:02'), INTERVAL (60 + FLOOR(RAND() * 181)) SECOND)
FROM seq
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 90 + n;

-- composeDigestStep: today's run is in-flight (STARTED, NULL end_time); past weeks finished.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 12)
SELECT
    210 + n,
    CASE WHEN n = 12 THEN 1 ELSE 3 END,
    'composeDigestStep',
    120 + n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:01'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:02'),
    CASE WHEN n = 12 THEN NULL
         ELSE DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:02'), INTERVAL (45 + FLOOR(RAND() * 121)) SECOND) END,
    je.status,
    CASE WHEN n = 12 THEN 0 ELSE 2 END,
    CASE WHEN n = 12 THEN 20 ELSE 80 END,
    0,
    CASE WHEN n = 12 THEN 15 WHEN je.status = 'FAILED' THEN 60 ELSE 80 END,
    0, 0, 0,
    0,
    CASE WHEN n = 12 THEN 'EXECUTING' ELSE je.status END,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (7 * (12 - n)) DAY), '09:30:30')
FROM seq
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 120 + n;

-- ===== Contexts =====
INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT je.job_execution_id,
       CASE WHEN je.status = 'FAILED'
            THEN '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"see exit message"}'
            ELSE '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}' END,
       NULL
FROM BATCH_JOB_EXECUTION je;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT step_execution_id,
       CONCAT('{"@class":"java.util.HashMap","step.bookmark":"', step_execution_id, '"}'),
       NULL
FROM BATCH_STEP_EXECUTION;

-- ===== Bump sequence tables past seeded IDs so live Spring Batch runs don't collide. =====
UPDATE BATCH_JOB_INSTANCE_SEQ   SET ID = (SELECT MAX(JOB_INSTANCE_ID)  FROM BATCH_JOB_INSTANCE);
UPDATE BATCH_JOB_EXECUTION_SEQ  SET ID = (SELECT MAX(JOB_EXECUTION_ID) FROM BATCH_JOB_EXECUTION);
UPDATE BATCH_STEP_EXECUTION_SEQ SET ID = (SELECT MAX(STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION);
