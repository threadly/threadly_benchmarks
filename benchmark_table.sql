create table class_group_identifier (
  id int NOT NULL CHECK (id > 0),
  name varchar(1024) NOT NULL,
  
  PRIMARY KEY (id)
);

create table benchmark_group_identifier (
  id int NOT NULL CHECK (id > 0), 
  name varchar(1024) NOT NULL,
  
  PRIMARY KEY (id)
);

create table run_results (
  benchmark_group_id int NOT NULL CHECK (benchmark_group_id > 0), 
  class_group_id int NOT NULL CHECK (class_group_id > 0), 
  benchmark_group_run_id int NOT NULL CHECK (benchmark_group_run_id > 0), 
  run_timestamp timestamp NOT NULL DEFAULT NOW(), 
  commit_hash varchar(160) NOT NULL,
  branch_name varchar(1024) DEFAULT NULL, 
  benchmark_name varchar(1024) NOT NULL, 
  total_executions int NOT NULL,
  duration int NOT NULL CHECK (duration > 0),
  ignore_result boolean NOT NULL DEFAULT FALSE, 

  FOREIGN KEY (class_group_id) REFERENCES class_group_identifier (id), 
  PRIMARY KEY (benchmark_group_id, benchmark_group_run_id, benchmark_name) 
);

INSERT INTO class_group_identifier (id, name) VALUES (1, 'JavaUtilBaseline');
INSERT INTO class_group_identifier (id, name) VALUES (2, 'PriorityScheduler');
INSERT INTO class_group_identifier (id, name) VALUES (3, 'SingleThreadScheduler');
INSERT INTO class_group_identifier (id, name) VALUES (4, 'SubmitterSchedulerLimiter');
INSERT INTO class_group_identifier (id, name) VALUES (5, 'KeyedLimiter');
INSERT INTO class_group_identifier (id, name) VALUES (6, 'KeyDistributedScheduler');

INSERT INTO benchmark_group_identifier (id, name) VALUES (1, 'JavaUtilConcurrentExecutorExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (2, 'JavaUtilConcurrentExecutorExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (3, 'JavaUtilConcurrentSchedulerExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (4, 'JavaUtilConcurrentSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (5, 'JavaUtilConcurrentSchedulerRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (6, 'JavaUtilConcurrentSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (7, 'JavaUtilConcurrentSchedulerSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (8, 'JavaUtilConcurrentSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (9, 'PrioritySchedulerExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (10, 'PrioritySchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (11, 'PrioritySchedulerRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (12, 'PrioritySchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (13, 'PrioritySchedulerSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (14, 'PrioritySchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (15, 'SingleThreadSchedulerExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (16, 'SingleThreadSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (17, 'SingleThreadSchedulerRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (18, 'SingleThreadSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (19, 'SingleThreadSchedulerSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (20, 'SingleThreadSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (21, 'NoThreadScheduler');
INSERT INTO benchmark_group_identifier (id, name) VALUES (22, 'SubmitterSchedulerLimiterExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (23, 'SubmitterSchedulerLimiterExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (24, 'SubmitterSchedulerLimiterRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (25, 'SubmitterSchedulerLimiterRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (26, 'SubmitterSchedulerLimiterSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (27, 'SubmitterSchedulerLimiterScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (28, 'KeyedLimiterExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (29, 'KeyedLimiterExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (30, 'KeyedLimiterRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (31, 'KeyedLimiterRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (32, 'KeyedLimiterSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (33, 'KeyedLimiterScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (34, 'KeyDistributedSchedulerExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (35, 'KeyDistributedSchedulerExecuteNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (36, 'KeyDistributedSchedulerRecurring');
INSERT INTO benchmark_group_identifier (id, name) VALUES (37, 'KeyDistributedSchedulerRecurringNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (38, 'KeyDistributedSchedulerSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (39, 'KeyDistributedSchedulerScheduleNoOp');
INSERT INTO benchmark_group_identifier (id, name) VALUES (40, 'KeyDistributedExecutorSimpleExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (41, 'KeyDistributedExecutorManySubmitterExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (42, 'KeyDistributedExecutorManySubmitterSchedule');
INSERT INTO benchmark_group_identifier (id, name) VALUES (43, 'KeyDistributedExecutorUniqueKeyExecute');
INSERT INTO benchmark_group_identifier (id, name) VALUES (44, 'KeyDistributedExecutorUniqueKeySchedule');
