-- Sample Spring Batch metadata. Runs once on container start, after 01-schema.sql.
-- Dates are computed relative to CONVERT(DATETIME, CAST(GETDATE() AS DATE)) (today at
-- 00:00:00) so the data always looks "recent" no matter when the container is provisioned.
-- CAST(GETDATE() AS DATE) gives a DATE type; wrapping in CONVERT(DATETIME, ...) is required
-- before DATEADD(SECOND, ...) because SQL Server's DATEADD does not support second-level
-- offsets on the DATE type.
--
-- Per-step counts, durations, and per-execution statuses are randomized via CHECKSUM(NEWID())
-- so each engine's dashboard view shows distinct values; today's anchor execution per job is
-- pinned to a known status so repository tests can rely on:
--   * exec 90  (today's dailyImportJob)     -> COMPLETED  (anchors NEWEST_DAILY_EXEC)
--   * exec 120 (today's reconcileLedgerJob) -> FAILED     (anchors findMostRecentFailed)
--   * exec 132 (today's sendDigestEmailJob) -> STARTED    (anchors the in-flight tile)
--
-- Layout:
--   * dailyImportJob:     90 runs (instance/exec IDs 1..90, step IDs 1..180).
--   * reconcileLedgerJob: 30 runs (instance/exec IDs 91..120, step IDs 181..210).
--   * sendDigestEmailJob: 12 runs (instance/exec IDs 121..132, step IDs 211..222).
--
-- Statement separator: GO (each logical batch ends with a bare GO line so that both sqlcmd
-- and the Java initializer can split correctly without interpreting semicolons inside CTEs).

-- ===== Job instances =====
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 90
)
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT n, 0, 'dailyImportJob',
    RIGHT(REPLICATE('0', 32) + LOWER(CONVERT(VARCHAR(32), CONVERT(VARBINARY(8), CAST(n AS BIGINT)), 2)), 32)
FROM seq
OPTION (MAXRECURSION 200)
GO

WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
)
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 90 + n, 0, 'reconcileLedgerJob',
    RIGHT(REPLICATE('0', 32) + LOWER(CONVERT(VARCHAR(32), CONVERT(VARBINARY(8), CAST(90 + n AS BIGINT)), 2)), 32)
FROM seq
OPTION (MAXRECURSION 200)
GO

WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 12
)
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT 120 + n, 0, 'sendDigestEmailJob',
    RIGHT(REPLICATE('0', 32) + LOWER(CONVERT(VARCHAR(32), CONVERT(VARBINARY(8), CAST(120 + n AS BIGINT)), 2)), 32)
FROM seq
OPTION (MAXRECURSION 200)
GO

-- ===== Job executions =====
-- Daily import: today (n=90) pinned to COMPLETED; past days flip ~20% to FAILED at random.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 90
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
),
base AS (
    SELECT
        seq.n,
        DATEADD(SECOND, 8 * 3600,
            DATEADD(DAY, -(90 - seq.n), today.dt))                                                    AS create_ts,
        DATEADD(SECOND, 8 * 3600 + 1,
            DATEADD(DAY, -(90 - seq.n), today.dt))                                                    AS start_ts,
        DATEADD(SECOND, 8 * 3600 + 1 + 60 + ABS(CHECKSUM(NEWID())) % 241,
            DATEADD(DAY, -(90 - seq.n), today.dt))                                                    AS end_ts,
        CASE WHEN seq.n = 90 THEN 'COMPLETED'
             WHEN ABS(CHECKSUM(NEWID())) % 100 < 20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                      AS status
    FROM seq CROSS JOIN today
)
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME,
     STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    n, 2, n,
    create_ts, start_ts, end_ts,
    status, status,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE '' END,
    end_ts
FROM base
OPTION (MAXRECURSION 200)
GO

-- Reconcile: today (n=30 -> exec 120) pinned to FAILED; past days ~75% COMPLETED / ~25% FAILED.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
),
base AS (
    SELECT
        seq.n,
        DATEADD(SECOND, 18 * 3600,
            DATEADD(DAY, -(30 - seq.n), today.dt))                                                    AS create_ts,
        DATEADD(SECOND, 18 * 3600 + 1,
            DATEADD(DAY, -(30 - seq.n), today.dt))                                                    AS start_ts,
        DATEADD(SECOND, 18 * 3600 + 1 + 90 + ABS(CHECKSUM(NEWID())) % 211,
            DATEADD(DAY, -(30 - seq.n), today.dt))                                                    AS end_ts,
        CASE WHEN seq.n = 30 THEN 'FAILED'
             WHEN ABS(CHECKSUM(NEWID())) % 100 < 25 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                      AS status
    FROM seq CROSS JOIN today
)
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME,
     STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    90 + n, 2, 90 + n,
    create_ts, start_ts, end_ts,
    status, status,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE '' END,
    end_ts
FROM base
OPTION (MAXRECURSION 200)
GO

-- Digest: today (n=12 -> exec 132) pinned to STARTED with NULL end_time; past weeks finished.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 12
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
),
base AS (
    SELECT
        seq.n,
        DATEADD(SECOND, 9 * 3600 + 30 * 60,
            DATEADD(DAY, -(7 * (12 - seq.n)), today.dt))                                              AS create_ts,
        DATEADD(SECOND, 9 * 3600 + 30 * 60 + 1,
            DATEADD(DAY, -(7 * (12 - seq.n)), today.dt))                                              AS start_ts,
        DATEADD(SECOND, 9 * 3600 + 30 * 60 + 1 + 45 + ABS(CHECKSUM(NEWID())) % 121,
            DATEADD(DAY, -(7 * (12 - seq.n)), today.dt))                                              AS end_ts,
        CASE WHEN seq.n = 12 THEN 'STARTED'
             WHEN ABS(CHECKSUM(NEWID())) % 100 < 20 THEN 'FAILED'
             ELSE 'COMPLETED' END                                                                      AS status
    FROM seq CROSS JOIN today
)
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME,
     STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    120 + n,
    CASE WHEN n = 12 THEN 1 ELSE 2 END,
    120 + n,
    create_ts,
    start_ts,
    CASE WHEN n = 12 THEN NULL ELSE end_ts END,
    status,
    CASE WHEN n = 12 THEN 'UNKNOWN' ELSE status END,
    CASE WHEN status = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    CASE WHEN n = 12 THEN DATEADD(SECOND, 29, start_ts) ELSE end_ts END
FROM base
OPTION (MAXRECURSION 200)
GO

-- ===== Job execution params =====
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 90
)
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT n, 'run.id', 'java.lang.Long', CAST(n AS NVARCHAR(20)), 'Y'
FROM seq
OPTION (MAXRECURSION 200)
GO

WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
)
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 90 + seq.n, 'asOfDate', 'java.time.LocalDate',
    CONVERT(NVARCHAR(10), DATEADD(DAY, -(30 - seq.n), today.dt), 23),
    'Y'
FROM seq CROSS JOIN today
OPTION (MAXRECURSION 200)
GO

WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 12
)
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT 120 + n, 'audience', 'java.lang.String', 'weekly-digest', 'Y'
FROM seq
OPTION (MAXRECURSION 200)
GO

-- ===== Step executions =====
-- readUsersStep: always COMPLETED. Random duration / counts.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 90
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
)
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * seq.n - 1, 3, 'readUsersStep', seq.n,
    DATEADD(SECOND, 8 * 3600 + 1,                            DATEADD(DAY, -(90 - seq.n), today.dt)),
    DATEADD(SECOND, 8 * 3600 + 2,                            DATEADD(DAY, -(90 - seq.n), today.dt)),
    DATEADD(SECOND, 8 * 3600 + 2 + 20 + ABS(CHECKSUM(NEWID())) % 61, DATEADD(DAY, -(90 - seq.n), today.dt)),
    'COMPLETED',
    8 + ABS(CHECKSUM(NEWID())) % 5,
    800 + ABS(CHECKSUM(NEWID())) % 401,
    0,
    800 + ABS(CHECKSUM(NEWID())) % 401,
    0, 0, 0, 0, 'COMPLETED', '',
    DATEADD(SECOND, 8 * 3600 + 2 + 20 + ABS(CHECKSUM(NEWID())) % 61, DATEADD(DAY, -(90 - seq.n), today.dt))
FROM seq CROSS JOIN today
OPTION (MAXRECURSION 200)
GO

-- writeUsersStep: status follows its execution. FAILED writes get rollback_count = 1.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 90
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
)
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * seq.n, 3, 'writeUsersStep', seq.n,
    DATEADD(SECOND, 8 * 3600 + 90,                              DATEADD(DAY, -(90 - seq.n), today.dt)),
    DATEADD(SECOND, 8 * 3600 + 91,                              DATEADD(DAY, -(90 - seq.n), today.dt)),
    DATEADD(SECOND, 8 * 3600 + 91 + 30 + ABS(CHECKSUM(NEWID())) % 91, DATEADD(DAY, -(90 - seq.n), today.dt)),
    je.STATUS,
    8 + ABS(CHECKSUM(NEWID())) % 5,
    800 + ABS(CHECKSUM(NEWID())) % 401,
    0,
    800 + ABS(CHECKSUM(NEWID())) % 401,
    0, 0, 0,
    CASE WHEN je.STATUS = 'FAILED' THEN 1 ELSE 0 END,
    je.STATUS,
    CASE WHEN je.STATUS = 'FAILED' THEN 'java.lang.IllegalStateException: import job aborted' ELSE '' END,
    DATEADD(SECOND, 8 * 3600 + 91 + 30 + ABS(CHECKSUM(NEWID())) % 91, DATEADD(DAY, -(90 - seq.n), today.dt))
FROM seq
CROSS JOIN today
JOIN BATCH_JOB_EXECUTION je ON je.JOB_EXECUTION_ID = seq.n
OPTION (MAXRECURSION 200)
GO

-- reconcileStep: status matches its execution. FAILED reconciles add a rollback marker.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
)
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    180 + seq.n, 3, 'reconcileStep', 90 + seq.n,
    DATEADD(SECOND, 18 * 3600 + 1,                                DATEADD(DAY, -(30 - seq.n), today.dt)),
    DATEADD(SECOND, 18 * 3600 + 2,                                DATEADD(DAY, -(30 - seq.n), today.dt)),
    DATEADD(SECOND, 18 * 3600 + 2 + 60 + ABS(CHECKSUM(NEWID())) % 181, DATEADD(DAY, -(30 - seq.n), today.dt)),
    je.STATUS,
    CASE WHEN je.STATUS = 'FAILED' THEN 4 ELSE 5 END,
    500,
    0,
    CASE WHEN je.STATUS = 'FAILED' THEN 400 ELSE 500 END,
    0, 0, 0,
    CASE WHEN je.STATUS = 'FAILED' THEN 1 ELSE 0 END,
    je.STATUS,
    CASE WHEN je.STATUS = 'FAILED' THEN 'java.lang.IllegalStateException: ledger out of balance' ELSE '' END,
    DATEADD(SECOND, 18 * 3600 + 2 + 60 + ABS(CHECKSUM(NEWID())) % 181, DATEADD(DAY, -(30 - seq.n), today.dt))
FROM seq
CROSS JOIN today
JOIN BATCH_JOB_EXECUTION je ON je.JOB_EXECUTION_ID = 90 + seq.n
OPTION (MAXRECURSION 200)
GO

-- composeDigestStep: today's run is in-flight (STARTED, NULL end_time); past weeks finished.
WITH seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 12
),
today AS (
    SELECT CONVERT(DATETIME, CAST(GETDATE() AS DATE)) AS dt
)
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    210 + seq.n,
    CASE WHEN seq.n = 12 THEN 1 ELSE 3 END,
    'composeDigestStep',
    120 + seq.n,
    DATEADD(SECOND, 9 * 3600 + 30 * 60 + 1,  DATEADD(DAY, -(7 * (12 - seq.n)), today.dt)),
    DATEADD(SECOND, 9 * 3600 + 30 * 60 + 2,  DATEADD(DAY, -(7 * (12 - seq.n)), today.dt)),
    CASE WHEN seq.n = 12 THEN NULL
         ELSE DATEADD(SECOND, 9 * 3600 + 30 * 60 + 2 + 45 + ABS(CHECKSUM(NEWID())) % 121,
                              DATEADD(DAY, -(7 * (12 - seq.n)), today.dt)) END,
    je.STATUS,
    CASE WHEN seq.n = 12 THEN 0 ELSE 2 END,
    CASE WHEN seq.n = 12 THEN 20 ELSE 80 END,
    0,
    CASE WHEN seq.n = 12 THEN 15 WHEN je.STATUS = 'FAILED' THEN 60 ELSE 80 END,
    0, 0, 0,
    0,
    CASE WHEN seq.n = 12 THEN 'EXECUTING' ELSE je.STATUS END,
    CASE WHEN je.STATUS = 'FAILED' THEN 'java.lang.IllegalStateException: digest provider unavailable' ELSE '' END,
    DATEADD(SECOND, 9 * 3600 + 30 * 60 + 30, DATEADD(DAY, -(7 * (12 - seq.n)), today.dt))
FROM seq
CROSS JOIN today
JOIN BATCH_JOB_EXECUTION je ON je.JOB_EXECUTION_ID = 120 + seq.n
OPTION (MAXRECURSION 200)
GO

-- ===== Contexts (one per execution / step, required by FKs) =====
INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT je.JOB_EXECUTION_ID,
       CASE WHEN je.STATUS = 'FAILED'
            THEN '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"see exit message"}'
            ELSE '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}' END,
       NULL
FROM BATCH_JOB_EXECUTION je
GO

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT STEP_EXECUTION_ID,
       '{"@class":"java.util.HashMap","step.bookmark":"' + CAST(STEP_EXECUTION_ID AS NVARCHAR(20)) + '"}',
       NULL
FROM BATCH_STEP_EXECUTION
GO

-- ===== Bump sequences past seeded IDs so live Spring Batch runs don't collide. =====
ALTER SEQUENCE BATCH_JOB_INSTANCE_SEQ   RESTART WITH 133
GO
ALTER SEQUENCE BATCH_JOB_EXECUTION_SEQ  RESTART WITH 133
GO
ALTER SEQUENCE BATCH_STEP_EXECUTION_SEQ RESTART WITH 223
GO
