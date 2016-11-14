CREATE TABLE benchmark_group_identifier (
    id integer NOT NULL,
    name character varying(1024) NOT NULL,

    CONSTRAINT benchmark_group_identifier_id_check CHECK ((id > 0)),
    PRIMARY KEY (id)
);

CREATE TABLE class_group_identifier (
    id integer NOT NULL,
    name character varying(1024) NOT NULL,

    CONSTRAINT class_group_identifier_id_check CHECK ((id > 0)),
    PRIMARY KEY (id)
);

CREATE TABLE run_results (
    benchmark_group_id integer NOT NULL,
    class_group_id integer NOT NULL,
    benchmark_group_run_id integer NOT NULL,
    run_timestamp timestamp without time zone DEFAULT now() NOT NULL,
    commit_hash character varying(160) NOT NULL,
    branch_name character varying(1024) DEFAULT NULL::character varying,
    benchmark_name character varying(1024) NOT NULL,
    total_executions integer NOT NULL,
    duration integer NOT NULL,
    ignore_result boolean DEFAULT false NOT NULL,
    
    CONSTRAINT run_results_benchmark_group_id_check CHECK ((benchmark_group_id > 0)),
    CONSTRAINT run_results_benchmark_group_run_id_check CHECK ((benchmark_group_run_id > 0)),
    CONSTRAINT run_results_class_group_id_check CHECK ((class_group_id > 0)),
    CONSTRAINT run_results_duration_check CHECK ((duration > 0)),
    FOREIGN KEY (benchmark_group_id) REFERENCES benchmark_group_identifier (id),
    FOREIGN KEY (class_group_id) REFERENCES class_group_identifier (id),
    PRIMARY KEY (benchmark_group_id, benchmark_group_run_id, benchmark_name)
);

INSERT INTO benchmark_group_identifier VALUES (1, 'JavaUtilConcurrentExecutorExecute');
INSERT INTO benchmark_group_identifier VALUES (2, 'JavaUtilConcurrentExecutorExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (3, 'JavaUtilConcurrentSchedulerExecute');
INSERT INTO benchmark_group_identifier VALUES (4, 'JavaUtilConcurrentSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (5, 'JavaUtilConcurrentSchedulerRecurring');
INSERT INTO benchmark_group_identifier VALUES (6, 'JavaUtilConcurrentSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (7, 'JavaUtilConcurrentSchedulerSchedule');
INSERT INTO benchmark_group_identifier VALUES (8, 'JavaUtilConcurrentSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (9, 'PrioritySchedulerExecute');
INSERT INTO benchmark_group_identifier VALUES (10, 'PrioritySchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (11, 'PrioritySchedulerRecurring');
INSERT INTO benchmark_group_identifier VALUES (12, 'PrioritySchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (13, 'PrioritySchedulerSchedule');
INSERT INTO benchmark_group_identifier VALUES (14, 'PrioritySchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (15, 'UnfairExecutorExecute');
INSERT INTO benchmark_group_identifier VALUES (16, 'UnfairExecutorExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (17, 'SingleThreadSchedulerExecute');
INSERT INTO benchmark_group_identifier VALUES (18, 'SingleThreadSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (19, 'SingleThreadSchedulerRecurring');
INSERT INTO benchmark_group_identifier VALUES (20, 'SingleThreadSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (21, 'SingleThreadSchedulerSchedule');
INSERT INTO benchmark_group_identifier VALUES (22, 'SingleThreadSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (23, 'NoThreadScheduler');
INSERT INTO benchmark_group_identifier VALUES (24, 'SubmitterSchedulerLimiterExecute');
INSERT INTO benchmark_group_identifier VALUES (25, 'SubmitterSchedulerLimiterExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (26, 'SubmitterSchedulerLimiterRecurring');
INSERT INTO benchmark_group_identifier VALUES (27, 'SubmitterSchedulerLimiterRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (28, 'SubmitterSchedulerLimiterSchedule');
INSERT INTO benchmark_group_identifier VALUES (29, 'SubmitterSchedulerLimiterScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (30, 'KeyedLimiterExecute');
INSERT INTO benchmark_group_identifier VALUES (31, 'KeyedLimiterExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (32, 'KeyedLimiterExecuteUnfairExecutor');
INSERT INTO benchmark_group_identifier VALUES (33, 'KeyedLimiterExecuteUnfairExecutorNoOp');
INSERT INTO benchmark_group_identifier VALUES (34, 'KeyedLimiterRecurring');
INSERT INTO benchmark_group_identifier VALUES (35, 'KeyedLimiterRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (36, 'KeyedLimiterSchedule');
INSERT INTO benchmark_group_identifier VALUES (37, 'KeyedLimiterScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (38, 'KeyDistributedSchedulerExecute');
INSERT INTO benchmark_group_identifier VALUES (39, 'KeyDistributedSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (40, 'KeyDistributedSchedulerExecuteUnfairExecutor');
INSERT INTO benchmark_group_identifier VALUES (41, 'KeyDistributedSchedulerExecuteUnfairExecutorNoOp');
INSERT INTO benchmark_group_identifier VALUES (42, 'KeyDistributedSchedulerRecurring');
INSERT INTO benchmark_group_identifier VALUES (43, 'KeyDistributedSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (44, 'KeyDistributedSchedulerSchedule');
INSERT INTO benchmark_group_identifier VALUES (45, 'KeyDistributedSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (46, 'KeyDistributedExecutorSimpleExecute');
INSERT INTO benchmark_group_identifier VALUES (47, 'KeyDistributedExecutorManySubmitterExecute');
INSERT INTO benchmark_group_identifier VALUES (48, 'KeyDistributedExecutorManySubmitterSchedule');
INSERT INTO benchmark_group_identifier VALUES (49, 'KeyDistributedExecutorUniqueKeyExecute');
INSERT INTO benchmark_group_identifier VALUES (50, 'KeyDistributedExecutorUniqueKeySchedule');
INSERT INTO benchmark_group_identifier VALUES (51, 'KeyDistributedExecutorUniqueKeyUnfairExecutor');
INSERT INTO benchmark_group_identifier VALUES (52, 'PrioritySchedulerStatisticTrackerExecute');
INSERT INTO benchmark_group_identifier VALUES (53, 'PrioritySchedulerStatisticTrackerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (54, 'PrioritySchedulerStatisticTrackerRecurring');
INSERT INTO benchmark_group_identifier VALUES (55, 'PrioritySchedulerStatisticTrackerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (56, 'PrioritySchedulerStatisticTrackerSchedule');
INSERT INTO benchmark_group_identifier VALUES (57, 'PrioritySchedulerStatisticTrackerScheduleNoOp');
INSERT INTO benchmark_group_identifier VALUES (58, 'SingleThreadSchedulerStatisticTrackerExecute');
INSERT INTO benchmark_group_identifier VALUES (59, 'SingleThreadSchedulerStatisticTrackerExecuteNoOp');
INSERT INTO benchmark_group_identifier VALUES (60, 'SingleThreadSchedulerStatisticTrackerRecurring');
INSERT INTO benchmark_group_identifier VALUES (61, 'SingleThreadSchedulerStatisticTrackerRecurringNoOp');
INSERT INTO benchmark_group_identifier VALUES (62, 'SingleThreadSchedulerStatisticTrackerSchedule');
INSERT INTO benchmark_group_identifier VALUES (63, 'SingleThreadSchedulerStatisticTrackerScheduleNoOp');

INSERT INTO class_group_identifier VALUES (1, 'JavaUtilBaseline');
INSERT INTO class_group_identifier VALUES (2, 'PriorityScheduler');
INSERT INTO class_group_identifier VALUES (7, 'KeyDistributedScheduler');
INSERT INTO class_group_identifier VALUES (6, 'KeyedLimiter');
INSERT INTO class_group_identifier VALUES (5, 'SubmitterSchedulerLimiter');
INSERT INTO class_group_identifier VALUES (4, 'SingleThreadScheduler');
INSERT INTO class_group_identifier VALUES (3, 'UnfairExecutor');
INSERT INTO class_group_identifier VALUES (8, 'PrioritySchedulerStatisticTracker');
INSERT INTO class_group_identifier VALUES (9, 'SingleThreadSchedulerStatisticTracker');
