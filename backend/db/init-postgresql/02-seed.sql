-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
-- Dates are computed relative to CURRENT_DATE so the data always looks "recent" no matter when
-- the container is provisioned. Per-step counts and durations are randomized so each engine's
-- dashboard view shows distinct values; status counts stay deterministic so repository tests
-- can assert exact totals (90 COMPLETED, 1 FAILED, 1 STARTED).
--
-- Layout:
--   * dailyImportJob:     90 COMPLETED runs, one per day going back 90 days (instance/execution
--                         IDs 1..90, step IDs 1..180 — read+write step pair per execution).
--   * reconcileLedgerJob: 1 FAILED run today (instance/execution ID 91, step ID 181).
--   * sendDigestEmailJob: 1 STARTED (in-flight) run today (instance/execution ID 92,
--                         step ID 182).

-- 90 daily import job instances
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT n, 0, 'dailyImportJob', LPAD(TO_HEX(n), 32, '0')
FROM generate_series(1, 90) AS n;

-- One special instance per long-running tail
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES
    (91, 0, 'reconcileLedgerJob', '4d1f3e6a3c5a6f7d0e9a8b1c2d3e4f50'),
    (92, 0, 'sendDigestEmailJob', '5e2a4f7b4d6b7a8e1f0b9c2d3e4f5061');

-- 90 daily executions: execution N happened (90 - N) days ago, starting at 08:00:01.
-- End time is randomized between 60–300 seconds after start so the trend chart varies.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    n, 2, n,
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second' + (60 + FLOOR(RANDOM() * 241))::int * INTERVAL '1 second',
    'COMPLETED', 'COMPLETED', '',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 second' + (60 + FLOOR(RANDOM() * 241))::int * INTERVAL '1 second'
FROM generate_series(1, 90) AS n;

-- Reconcile (FAILED) and digest (STARTED) tail executions. Both happened today. Fixed values
-- so the QualitySignals tile and the per-execution drill-downs assert against known counts.
INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (91, 2, 91,
        CURRENT_DATE + INTERVAL '2 hours',
        CURRENT_DATE + INTERVAL '2 hours 1 second',
        CURRENT_DATE + INTERVAL '2 hours 5 minutes',
        'FAILED', 'FAILED', 'java.lang.IllegalStateException: ledger out of balance',
        CURRENT_DATE + INTERVAL '2 hours 5 minutes'),
    (92, 1, 92,
        CURRENT_DATE + INTERVAL '9 hours 30 minutes',
        CURRENT_DATE + INTERVAL '9 hours 30 minutes 1 second',
        NULL,
        'STARTED', 'UNKNOWN', '',
        CURRENT_DATE + INTERVAL '9 hours 30 minutes 30 seconds');

-- Daily import params (run.id stamp per execution).
INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING)
SELECT n, 'run.id', 'java.lang.Long', n::text, 'Y'
FROM generate_series(1, 90) AS n;

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING) VALUES
    (91, 'asOfDate', 'java.time.LocalDate', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), 'Y'),
    (92, 'audience', 'java.lang.String',    'weekly-digest',                     'Y');

-- readUsersStep (id 2n-1) per daily import: 800..1200 records, 8..12 commits, 20..80s duration.
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

-- writeUsersStep (id 2n) per daily import: 800..1200 records, 8..12 commits, 30..120s duration.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED)
SELECT
    2 * n, 3, 'writeUsersStep', n,
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 30 seconds',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds' + (30 + FLOOR(RANDOM() * 91))::int * INTERVAL '1 second',
    'COMPLETED',
    (8 + FLOOR(RANDOM() * 5))::int,
    (800 + FLOOR(RANDOM() * 401))::int,
    0,
    (800 + FLOOR(RANDOM() * 401))::int,
    0, 0, 0, 0, 'COMPLETED', '',
    (CURRENT_DATE - (90 - n)) + INTERVAL '8 hours 1 minute 31 seconds' + (30 + FLOOR(RANDOM() * 91))::int * INTERVAL '1 second'
FROM generate_series(1, 90) AS n;

-- Tail steps: reconcile (FAILED) + digest (STARTED). Fixed values.
INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (181, 3, 'reconcileStep', 91,
        CURRENT_DATE + INTERVAL '2 hours 1 second',
        CURRENT_DATE + INTERVAL '2 hours 2 seconds',
        CURRENT_DATE + INTERVAL '2 hours 5 minutes',
        'FAILED', 4, 500, 0, 400, 0, 0, 0, 1, 'FAILED', 'java.lang.IllegalStateException: ledger out of balance',
        CURRENT_DATE + INTERVAL '2 hours 5 minutes'),
    (182, 1, 'composeDigestStep', 92,
        CURRENT_DATE + INTERVAL '9 hours 30 minutes 1 second',
        CURRENT_DATE + INTERVAL '9 hours 30 minutes 2 seconds',
        NULL,
        'STARTED', 0, 20, 0, 15, 0, 0, 0, 0, 'EXECUTING', '',
        CURRENT_DATE + INTERVAL '9 hours 30 minutes 30 seconds');

-- Job-execution context (one per execution, required by the FK in 01-schema.sql).
INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT n, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL
FROM generate_series(1, 90) AS n;

INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (91, '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"ledger out of balance"}', NULL),
    (92, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL);

-- Step-execution context (one per step).
INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT 2 * n - 1, '{"@class":"java.util.HashMap","FlatFileItemReader.read.count":1000}', NULL
FROM generate_series(1, 90) AS n;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT)
SELECT 2 * n, '{"@class":"java.util.HashMap","JdbcBatchItemWriter.write.count":1000}', NULL
FROM generate_series(1, 90) AS n;

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (181, '{"@class":"java.util.HashMap","reconcile.lastAccountId":"ACC-000410"}', NULL),
    (182, '{"@class":"java.util.HashMap","digest.batchOffset":15}', NULL);

-- Bump sequences past seeded IDs so live Spring Batch runs don't collide.
SELECT setval('batch_job_instance_seq',  (SELECT MAX(JOB_INSTANCE_ID)  FROM BATCH_JOB_INSTANCE));
SELECT setval('batch_job_execution_seq', (SELECT MAX(JOB_EXECUTION_ID) FROM BATCH_JOB_EXECUTION));
SELECT setval('batch_step_execution_seq', (SELECT MAX(STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION));
