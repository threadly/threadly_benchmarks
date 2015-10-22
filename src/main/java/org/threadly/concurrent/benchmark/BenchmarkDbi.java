package org.threadly.concurrent.benchmark;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface BenchmarkDbi {
  @SqlUpdate("INSERT INTO run_results " + 
               "(benchmark_group_id, class_group_id, benchmark_group_run_id, " + 
               "   commit_hash, branch_name, benchmark_name, total_executions, duration) VALUES " + 
               "(:bGroupId, :cGroupId, :gRunId, :hash, :branch_name, :benchmark_name, :executions, :duration)")
  public void addRecord(@Bind("bGroupId") int bGroupId, @Bind("cGroupId") int cGroupId, @Bind("gRunId") int gRunId, 
                        @Bind("hash") String hash, @Bind("branch_name") String branchName, @Bind("benchmark_name") String benchmarkName, 
                        @Bind("executions") long totalExecutions, @Bind("duration") int duration);
  
  @SqlQuery("SELECT MAX(benchmark_group_run_id) FROM run_results WHERE benchmark_group_id = :bGroupId")
  public int getLastBenchmarkGroupRunId(@Bind("bGroupId") int bGroupId);
  
  @SqlQuery("SELECT MAX(class_group_id) FROM run_results")
  public int getMaxClassGroupId();
  
  @SqlQuery("SELECT MIN(benchmark_group_id) FROM run_results WHERE class_group_id = :cGroupId")
  public int getMinBenchmarkGroupId(@Bind("cGroupId") int cGroupId);
  
  @SqlQuery("SELECT MAX(benchmark_group_id) FROM run_results WHERE class_group_id = :cGroupId")
  public int getMaxBenchmarkGroupId(@Bind("cGroupId") int cGroupId);

  @SqlQuery("SELECT * FROM run_results WHERE benchmark_group_id = :bGroupId AND class_group_id = :cGroupId")
  public List<RunRecord> getResultsForBenchmarkGroupId(@Bind("bGroupId") int bGroupId, 
                                                       @Bind("cGroupId") int cGroupId);
  
  @SqlQuery("SELECT name FROM class_group_identifier WHERE id = :cGroupId")
  public String getClassGroupIdentifier(@Bind("cGroupId") int cGroupId);

  public void close();
  
  public static class RunRecord {
    private int benchmark_group_id;
    private int class_group_id;
    private int benchmark_group_run_id;
    private String commit_hash;
    private String branch_name;
    private String benchmark_name;
    private int executions;
    private int duration;
    
    public int getBenchmark_group_id() {
      return benchmark_group_id;
    }
    public void setBenchmark_group_id(int benchmark_group_id) {
      this.benchmark_group_id = benchmark_group_id;
    }
    public int getClass_group_id() {
      return class_group_id;
    }
    public void setClass_group_id(int class_group_id) {
      this.class_group_id = class_group_id;
    }
    public int getBenchmark_group_run_id() {
      return benchmark_group_run_id;
    }
    public void setBenchmark_group_run_id(int benchmark_group_run_id) {
      this.benchmark_group_run_id = benchmark_group_run_id;
    }
    public String getCommit_hash() {
      return commit_hash;
    }
    public void setCommit_hash(String commit_hash) {
      this.commit_hash = commit_hash;
    }
    public String getBranch_name() {
      return branch_name;
    }
    public void setBranch_name(String branch_name) {
      this.branch_name = branch_name;
    }
    public String getBenchmark_name() {
      return benchmark_name;
    }
    public void setBenchmark_name(String benchmark_name) {
      this.benchmark_name = benchmark_name;
    }
    public int getExecutions() {
      return executions;
    }
    public void setExecutions(int executions) {
      this.executions = executions;
    }
    public int getDuration() {
      return duration;
    }
    public void setDuration(int duration) {
      this.duration = duration;
    }
  }
}