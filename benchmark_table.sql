create table class_group_identifier (
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

  FORIEGN KEY (class_group_id) REFERENCES class_group_identifier (id), 
  PRIMARY KEY (benchmark_group_id, benchmark_group_run_id) 
);

INSERT INTO class_group_identifier (id, name) VALUES (1, 'JavaUtilBaseline');
INSERT INTO class_group_identifier (id, name) VALUES (2, 'PriorityScheduler');
INSERT INTO class_group_identifier (id, name) VALUES (3, 'SingleThreadScheduler');
INSERT INTO class_group_identifier (id, name) VALUES (4, 'SubmitterSchedulerLimiter');
INSERT INTO class_group_identifier (id, name) VALUES (5, 'KeyedLimiter');
INSERT INTO class_group_identifier (id, name) VALUES (6, 'KeyDistributedScheduler');
