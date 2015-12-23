package org.threadly.concurrent.benchmark.dao;

public class RunRecord {
  private final int benchmark_group_id;
  private final int class_group_id;
  private final int benchmark_group_run_id;
  private final long run_timestamp;
  private final String commit_hash;
  private final String branch_name;
  private final String benchmark_name;
  private final int total_executions;
  private final int duration;
  
  public RunRecord(int benchmark_group_id, int class_group_id, int benchmark_group_run_id, 
                   long run_timestamp, String commit_hash, String branch_name, 
                   String benchmark_name, int total_executions, int duration) {
    this.benchmark_group_id = benchmark_group_id;
    this.class_group_id = class_group_id;
    this.benchmark_group_run_id = benchmark_group_run_id;
    this.run_timestamp = run_timestamp;
    this.commit_hash = commit_hash;
    this.branch_name = branch_name;
    this.benchmark_name = benchmark_name;
    this.total_executions = total_executions;
    this.duration = duration;
  }
  
  public int getBenchmarkGroupId() {
    return benchmark_group_id;
  }
  public int getClassGroupId() {
    return class_group_id;
  }
  public int getBenchmarkGroupRunId() {
    return benchmark_group_run_id;
  }
  public long getRunTimestamp() {
    return run_timestamp;
  }
  public String getCommitHash() {
    return commit_hash;
  }
  public String getBranchName() {
    return branch_name;
  }
  public String getBenchmarkName() {
    return benchmark_name;
  }
  public int getTotalExecutions() {
    return total_executions;
  }
  public int getDuration() {
    return duration;
  }
}