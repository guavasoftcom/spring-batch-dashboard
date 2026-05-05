-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
-- Dates are computed relative to TRUNC(SYSDATE) so the data always looks "recent" no matter
-- when the container is provisioned. Per-step counts, durations, and per-execution statuses
-- are randomized via DBMS_RANDOM so each engine's dashboard view shows distinct values;
-- today's anchor execution per job is pinned to a known status so repository tests can rely on:
--   * exec 90  (today's dailyImportJob)     → COMPLETED  (anchors NEWEST_DAILY_EXEC)
--   * exec 120 (today's reconcileLedgerJob) → FAILED     (anchors findMostRecentFailed)
--   * exec 132 (today's sendDigestEmailJob) → STARTED    (anchors the in-flight tile)
--
-- Each gvenzl init script gets a fresh CDB$ROOT-as-sysdba session, so we re-target FREEPDB1 /
-- SYSTEM here too.
ALTER SESSION SET CONTAINER = FREEPDB1;
ALTER SESSION SET CURRENT_SCHEMA = SYSTEM;

-- Layout:
--   * dailyImportJob:     90 runs (instance/exec IDs 1..90, step IDs 1..180).
--   * reconcileLedgerJob: 30 runs (instance/exec IDs 91..120, step IDs 181..210).
--   * sendDigestEmailJob: 12 runs (instance/exec IDs 121..132, step IDs 211..222).

-- ===== Job instances =====
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT LEVEL,        0, 'dailyImportJob',     LOWER(STANDARD_HASH('daily-' || TO_CHAR(LEVEL),     'MD5')) FROM dual CONNECT BY LEVEL <= 90;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 90 + LEVEL,   0, 'reconcileLedgerJob', LOWER(STANDARD_HASH('reconcile-' || TO_CHAR(LEVEL), 'MD5')) FROM dual CONNECT BY LEVEL <= 30;

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 120 + LEVEL,  0, 'sendDigestEmailJob', LOWER(STANDARD_HASH('digest-' || TO_CHAR(LEVEL),    'MD5')) FROM dual CONNECT BY LEVEL <= 12;

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
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE NULL END,
    end_ts
FROM (
    SELECT
        LEVEL AS n,
        TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR                                                                                       AS create_ts,
        TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '1' SECOND                                                                 AS start_ts,
        TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '1' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(60, 301)), 'SECOND')  AS end_ts,
        CASE WHEN LEVEL = 90 THEN 'COMPLETED'
             WHEN DBMS_RANDOM.VALUE(0, 1) < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                                               AS status
    FROM dual CONNECT BY LEVEL <= 90
);

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
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE NULL END,
    end_ts
FROM (
    SELECT
        LEVEL AS n,
        TRUNC(SYSDATE) - (30 - LEVEL) + INTERVAL '18' HOUR                                                                                      AS create_ts,
        TRUNC(SYSDATE) - (30 - LEVEL) + INTERVAL '18' HOUR + INTERVAL '1' SECOND                                                                AS start_ts,
        TRUNC(SYSDATE) - (30 - LEVEL) + INTERVAL '18' HOUR + INTERVAL '1' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(90, 301)), 'SECOND') AS end_ts,
        CASE WHEN LEVEL = 30 THEN 'FAILED'
             WHEN DBMS_RANDOM.VALUE(0, 1) < 0.25 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                                               AS status
    FROM dual CONNECT BY LEVEL <= 30
);

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
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE NULL END,
    CASE WHEN n = 12 THEN start_ts + INTERVAL '29' SECOND ELSE end_ts END
FROM (
    SELECT
        LEVEL AS n,
        TRUNC(SYSDATE) - (7 * (12 - LEVEL)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE                                                                                                            AS create_ts,
        TRUNC(SYSDATE) - (7 * (12 - LEVEL)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '1' SECOND                                                                                      AS start_ts,
        TRUNC(SYSDATE) - (7 * (12 - LEVEL)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '1' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(45, 166)), 'SECOND')                       AS end_ts,
        CASE WHEN LEVEL = 12 THEN 'STARTED'
             WHEN DBMS_RANDOM.VALUE(0, 1) < 0.20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                                                                                                                 AS status
    FROM dual CONNECT BY LEVEL <= 12
);

-- ===== Job execution params =====
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT LEVEL,        'run.id',   'java.lang.Long',      TO_CHAR(LEVEL),                                       'Y' FROM dual CONNECT BY LEVEL <= 90;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 90 + LEVEL,   'asOfDate', 'java.time.LocalDate', TO_CHAR(TRUNC(SYSDATE) - (30 - LEVEL), 'YYYY-MM-DD'), 'Y' FROM dual CONNECT BY LEVEL <= 30;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 120 + LEVEL,  'audience', 'java.lang.String',    'weekly-digest',                                     'Y' FROM dual CONNECT BY LEVEL <= 12;

-- ===== Step executions =====
-- readUsersStep: always COMPLETED. Random duration / counts.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * LEVEL - 1, 3, 'readUsersStep', LEVEL,
    TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '1' SECOND,
    TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '2' SECOND,
    TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '2' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(20, 81)), 'SECOND'),
    'COMPLETED',
    TRUNC(DBMS_RANDOM.VALUE(8, 13)),
    TRUNC(DBMS_RANDOM.VALUE(800, 1201)),
    0,
    TRUNC(DBMS_RANDOM.VALUE(800, 1201)),
    0, 0, 0, 0, 'COMPLETED', NULL,
    TRUNC(SYSDATE) - (90 - LEVEL) + INTERVAL '8' HOUR + INTERVAL '2' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(20, 81)), 'SECOND')
FROM dual CONNECT BY LEVEL <= 90;

-- writeUsersStep: status follows its execution. FAILED writes get rollback_count = 1.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * d.n, 3, 'writeUsersStep', d.n,
    TRUNC(SYSDATE) - (90 - d.n) + INTERVAL '8' HOUR + INTERVAL '1' MINUTE + INTERVAL '30' SECOND,
    TRUNC(SYSDATE) - (90 - d.n) + INTERVAL '8' HOUR + INTERVAL '1' MINUTE + INTERVAL '31' SECOND,
    TRUNC(SYSDATE) - (90 - d.n) + INTERVAL '8' HOUR + INTERVAL '1' MINUTE + INTERVAL '31' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(30, 121)), 'SECOND'),
    je.status,
    TRUNC(DBMS_RANDOM.VALUE(8, 13)),
    TRUNC(DBMS_RANDOM.VALUE(800, 1201)),
    0,
    TRUNC(DBMS_RANDOM.VALUE(800, 1201)),
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE NULL END,
    TRUNC(SYSDATE) - (90 - d.n) + INTERVAL '8' HOUR + INTERVAL '1' MINUTE + INTERVAL '31' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(30, 121)), 'SECOND')
FROM (SELECT LEVEL AS n FROM dual CONNECT BY LEVEL <= 90) d
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = d.n;

-- reconcileStep: status matches its execution.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    180 + d.n, 3, 'reconcileStep', 90 + d.n,
    TRUNC(SYSDATE) - (30 - d.n) + INTERVAL '18' HOUR + INTERVAL '1' SECOND,
    TRUNC(SYSDATE) - (30 - d.n) + INTERVAL '18' HOUR + INTERVAL '2' SECOND,
    TRUNC(SYSDATE) - (30 - d.n) + INTERVAL '18' HOUR + INTERVAL '2' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(60, 241)), 'SECOND'),
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 4 ELSE 5 END,
    500,
    0,
    CASE WHEN je.status = 'FAILED' THEN 400 ELSE 500 END,
    0, 0, 0,
    CASE WHEN je.status = 'FAILED' THEN 1 ELSE 0 END,
    je.status,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE NULL END,
    TRUNC(SYSDATE) - (30 - d.n) + INTERVAL '18' HOUR + INTERVAL '2' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(60, 241)), 'SECOND')
FROM (SELECT LEVEL AS n FROM dual CONNECT BY LEVEL <= 30) d
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 90 + d.n;

-- composeDigestStep: today's run is in-flight (STARTED, NULL end_time); past weeks finished.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    210 + d.n,
    CASE WHEN d.n = 12 THEN 1 ELSE 3 END,
    'composeDigestStep',
    120 + d.n,
    TRUNC(SYSDATE) - (7 * (12 - d.n)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '1' SECOND,
    TRUNC(SYSDATE) - (7 * (12 - d.n)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '2' SECOND,
    CASE WHEN d.n = 12 THEN NULL
         ELSE TRUNC(SYSDATE) - (7 * (12 - d.n)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '2' SECOND + NUMTODSINTERVAL(TRUNC(DBMS_RANDOM.VALUE(45, 166)), 'SECOND') END,
    je.status,
    CASE WHEN d.n = 12 THEN 0 ELSE 2 END,
    CASE WHEN d.n = 12 THEN 20 ELSE 80 END,
    0,
    CASE WHEN d.n = 12 THEN 15 WHEN je.status = 'FAILED' THEN 60 ELSE 80 END,
    0, 0, 0,
    0,
    CASE WHEN d.n = 12 THEN 'EXECUTING' ELSE je.status END,
    CASE WHEN je.status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE NULL END,
    TRUNC(SYSDATE) - (7 * (12 - d.n)) + INTERVAL '9' HOUR + INTERVAL '30' MINUTE + INTERVAL '30' SECOND
FROM (SELECT LEVEL AS n FROM dual CONNECT BY LEVEL <= 12) d
JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = 120 + d.n;

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
       '{"@class":"java.util.HashMap","step.bookmark":"' || step_execution_id || '"}',
       NULL
FROM BATCH_STEP_EXECUTION;

COMMIT;

-- ===== Bump sequences past seeded IDs so live Spring Batch runs don't collide. =====
ALTER SEQUENCE BATCH_JOB_INSTANCE_SEQ   RESTART START WITH 133;
ALTER SEQUENCE BATCH_JOB_EXECUTION_SEQ  RESTART START WITH 133;
ALTER SEQUENCE BATCH_STEP_EXECUTION_SEQ RESTART START WITH 223;
