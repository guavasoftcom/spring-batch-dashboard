-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
-- Dates are computed relative to CURDATE() so the data always looks "recent" no matter when
-- the container is provisioned. Per-step counts and durations are randomized via RAND() so
-- each engine's dashboard view shows distinct values; status counts stay deterministic so
-- repository tests can assert exact totals (90 COMPLETED, 1 FAILED, 1 STARTED).
--
-- Layout:
--   * dailyImportJob:     90 COMPLETED runs, one per day going back 90 days (instance/execution
--                         IDs 1..90, step IDs 1..180 — read+write step pair per execution).
--   * reconcileLedgerJob: 1 FAILED run today (instance/execution ID 91, step ID 181).
--   * sendDigestEmailJob: 1 STARTED (in-flight) run today (instance/execution ID 92,
--                         step ID 182).

-- 90 daily import job instances
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT n, 0, 'dailyImportJob', LPAD(HEX(n), 32, '0') FROM seq;

-- One special instance per long-running tail
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES
    (91, 0, 'reconcileLedgerJob', '4d1f3e6a3c5a6f7d0e9a8b1c2d3e4f50'),
    (92, 0, 'sendDigestEmailJob', '5e2a4f7b4d6b7a8e1f0b9c2d3e4f5061');

-- 90 daily executions: execution N happened (90 - N) days ago, starting at 08:00:01.
-- End time is randomized between 60–300 seconds after start so the trend chart varies.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT
    n, 2, n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:00'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01'),
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01'), INTERVAL (60 + FLOOR(RAND() * 241)) SECOND),
    'COMPLETED',
    'COMPLETED',
    '',
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:00:01'), INTERVAL (60 + FLOOR(RAND() * 241)) SECOND)
FROM seq;

-- Reconcile (FAILED) and digest (STARTED) tail executions. Both happened today. Fixed values.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (91, 2, 91,
        TIMESTAMP(CURDATE(), '02:00:00'),
        TIMESTAMP(CURDATE(), '02:00:01'),
        TIMESTAMP(CURDATE(), '02:05:00'),
        'FAILED', 'FAILED', 'java.lang.IllegalStateException: ledger out of balance',
        TIMESTAMP(CURDATE(), '02:05:00')),
    (92, 1, 92,
        TIMESTAMP(CURDATE(), '09:30:00'),
        TIMESTAMP(CURDATE(), '09:30:01'),
        NULL,
        'STARTED', 'UNKNOWN', '',
        TIMESTAMP(CURDATE(), '09:30:30'));

-- Daily import params (run.id stamp per execution).
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT n, 'run.id', 'java.lang.Long', CAST(n AS CHAR), 'Y' FROM seq;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING) VALUES
    (91, 'asOfDate', 'java.time.LocalDate', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), 'Y'),
    (92, 'audience', 'java.lang.String',    'weekly-digest',                    'Y');

-- readUsersStep (id 2n-1) per daily import: 800..1200 records, 8..12 commits, 20..80s duration.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
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

-- writeUsersStep (id 2n) per daily import: 800..1200 records, 8..12 commits, 30..120s duration.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT
    2 * n, 3, 'writeUsersStep', n,
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:30'),
    TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'),
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'), INTERVAL (30 + FLOOR(RAND() * 91)) SECOND),
    'COMPLETED',
    8 + FLOOR(RAND() * 5),
    800 + FLOOR(RAND() * 401),
    0,
    800 + FLOOR(RAND() * 401),
    0, 0, 0, 0, 'COMPLETED', '',
    DATE_ADD(TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL (90 - n) DAY), '08:01:31'), INTERVAL (30 + FLOOR(RAND() * 91)) SECOND)
FROM seq;

-- Tail steps: reconcile (FAILED) + digest (STARTED). Fixed values.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (181, 3, 'reconcileStep', 91,
        TIMESTAMP(CURDATE(), '02:00:01'),
        TIMESTAMP(CURDATE(), '02:00:02'),
        TIMESTAMP(CURDATE(), '02:05:00'),
        'FAILED', 4, 500, 0, 400, 0, 0, 0, 1, 'FAILED', 'java.lang.IllegalStateException: ledger out of balance',
        TIMESTAMP(CURDATE(), '02:05:00')),
    (182, 1, 'composeDigestStep', 92,
        TIMESTAMP(CURDATE(), '09:30:01'),
        TIMESTAMP(CURDATE(), '09:30:02'),
        NULL,
        'STARTED', 0, 20, 0, 15, 0, 0, 0, 0, 'EXECUTING', '',
        TIMESTAMP(CURDATE(), '09:30:30'));

-- Job-execution context (one per execution, required by the FK in 01-schema.sql).
INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT n, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL FROM seq;

INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (91, '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"ledger out of balance"}', NULL),
    (92, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL);

-- Step-execution context (one per step).
INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT 2 * n - 1, '{"@class":"java.util.HashMap","FlatFileItemReader.read.count":1000}', NULL FROM seq;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
WITH RECURSIVE seq(n) AS (
    SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 90
)
SELECT 2 * n, '{"@class":"java.util.HashMap","JdbcBatchItemWriter.write.count":1000}', NULL FROM seq;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (181, '{"@class":"java.util.HashMap","reconcile.lastAccountId":"ACC-000410"}', NULL),
    (182, '{"@class":"java.util.HashMap","digest.batchOffset":15}', NULL);

-- Bump sequence tables past seeded IDs so live Spring Batch runs don't collide.
UPDATE BATCH_JOB_INSTANCE_SEQ   SET ID = (SELECT MAX(JOB_INSTANCE_ID)  FROM BATCH_JOB_INSTANCE);
UPDATE BATCH_JOB_EXECUTION_SEQ  SET ID = (SELECT MAX(JOB_EXECUTION_ID) FROM BATCH_JOB_EXECUTION);
UPDATE BATCH_STEP_EXECUTION_SEQ SET ID = (SELECT MAX(STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION);
