-- Sample Spring Batch metadata. Runs once on first container start, after 01-schema.sql.
INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES
    (1, 0, 'importUsersJob',     '2b9d1c4f1a3e4d5b8c7e6f9a0b1c2d3e'),
    (2, 0, 'importUsersJob',     '3c0e2d5f2b4f5e6c9d8f7a0b1c2d3e4f'),
    (3, 0, 'reconcileLedgerJob', '4d1f3e6a3c5a6f7d0e9a8b1c2d3e4f50'),
    (4, 0, 'sendDigestEmailJob', '5e2a4f7b4d6b7a8e1f0b9c2d3e4f5061');

INSERT INTO BATCH_JOB_EXECUTION
    (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (1, 2, 1, '2026-04-22 08:00:00', '2026-04-22 08:00:01', '2026-04-22 08:02:14', 'COMPLETED', 'COMPLETED', '', '2026-04-22 08:02:14'),
    (2, 2, 2, '2026-04-23 08:00:00', '2026-04-23 08:00:01', '2026-04-23 08:01:58', 'COMPLETED', 'COMPLETED', '', '2026-04-23 08:01:58'),
    (3, 2, 3, '2026-04-24 02:00:00', '2026-04-24 02:00:01', '2026-04-24 02:05:42', 'FAILED',    'FAILED',    'java.lang.IllegalStateException: ledger out of balance', '2026-04-24 02:05:42'),
    (4, 1, 4, '2026-04-24 09:30:00', '2026-04-24 09:30:01', NULL,                  'STARTED',   'UNKNOWN',   '', '2026-04-24 09:30:01');

INSERT INTO BATCH_JOB_EXECUTION_PARAMS
    (JOB_EXECUTION_ID, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_VALUE, IDENTIFYING) VALUES
    (1, 'inputFile', 'java.lang.String',     '/data/users-2026-04-22.csv', 'Y'),
    (1, 'run.id',    'java.lang.Long',       '1',                          'Y'),
    (2, 'inputFile', 'java.lang.String',     '/data/users-2026-04-23.csv', 'Y'),
    (2, 'run.id',    'java.lang.Long',       '2',                          'Y'),
    (3, 'asOfDate',  'java.time.LocalDate',  '2026-04-23',                 'Y'),
    (4, 'audience',  'java.lang.String',     'weekly-digest',              'Y');

INSERT INTO BATCH_STEP_EXECUTION
    (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, END_TIME, STATUS,
     COMMIT_COUNT, READ_COUNT, FILTER_COUNT, WRITE_COUNT, READ_SKIP_COUNT, WRITE_SKIP_COUNT, PROCESS_SKIP_COUNT,
     ROLLBACK_COUNT, EXIT_CODE, EXIT_MESSAGE, LAST_UPDATED) VALUES
    (1, 3, 'readUsersStep',     1, '2026-04-22 08:00:01', '2026-04-22 08:00:02', '2026-04-22 08:01:30', 'COMPLETED',
     12, 1200, 0, 1200, 0, 0, 0, 0, 'COMPLETED', '', '2026-04-22 08:01:30'),
    (2, 3, 'writeUsersStep',    1, '2026-04-22 08:01:30', '2026-04-22 08:01:31', '2026-04-22 08:02:14', 'COMPLETED',
     12, 1200, 0, 1200, 0, 0, 0, 0, 'COMPLETED', '', '2026-04-22 08:02:14'),
    (3, 3, 'readUsersStep',     2, '2026-04-23 08:00:01', '2026-04-23 08:00:02', '2026-04-23 08:01:10', 'COMPLETED',
     11, 1085, 3, 1082, 3, 0, 0, 0, 'COMPLETED', '', '2026-04-23 08:01:10'),
    (4, 3, 'writeUsersStep',    2, '2026-04-23 08:01:10', '2026-04-23 08:01:11', '2026-04-23 08:01:58', 'COMPLETED',
     11, 1082, 0, 1082, 0, 0, 0, 0, 'COMPLETED', '', '2026-04-23 08:01:58'),
    (5, 3, 'reconcileStep',     3, '2026-04-24 02:00:01', '2026-04-24 02:00:02', '2026-04-24 02:05:42', 'FAILED',
     4,  450,  0, 410,  0, 0, 0, 1, 'FAILED', 'java.lang.IllegalStateException: ledger out of balance', '2026-04-24 02:05:42'),
    (6, 1, 'composeDigestStep', 4, '2026-04-24 09:30:01', '2026-04-24 09:30:02', NULL, 'STARTED',
     0,  25,   0, 24,   0, 0, 0, 0, 'EXECUTING', '', '2026-04-24 09:30:30');

INSERT INTO BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (1, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL),
    (2, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL),
    (3, '{"@class":"java.util.HashMap","batch.taskletType":"chunk","lastError":"ledger out of balance"}', NULL),
    (4, '{"@class":"java.util.HashMap","batch.taskletType":"chunk"}', NULL);

INSERT INTO BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID, SHORT_CONTEXT, SERIALIZED_CONTEXT) VALUES
    (1, '{"@class":"java.util.HashMap","FlatFileItemReader.read.count":1200}', NULL),
    (2, '{"@class":"java.util.HashMap","JdbcBatchItemWriter.write.count":1200}', NULL),
    (3, '{"@class":"java.util.HashMap","FlatFileItemReader.read.count":1085}', NULL),
    (4, '{"@class":"java.util.HashMap","JdbcBatchItemWriter.write.count":1082}', NULL),
    (5, '{"@class":"java.util.HashMap","reconcile.lastAccountId":"ACC-000410"}', NULL),
    (6, '{"@class":"java.util.HashMap","digest.batchOffset":24}', NULL);

-- Bump sequence tables past seeded IDs so live Spring Batch runs don't collide.
UPDATE BATCH_JOB_INSTANCE_SEQ   SET ID = (SELECT MAX(JOB_INSTANCE_ID)  FROM BATCH_JOB_INSTANCE);
UPDATE BATCH_JOB_EXECUTION_SEQ  SET ID = (SELECT MAX(JOB_EXECUTION_ID) FROM BATCH_JOB_EXECUTION);
UPDATE BATCH_STEP_EXECUTION_SEQ SET ID = (SELECT MAX(STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION);
