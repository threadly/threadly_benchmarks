package org.threadly.concurrent.benchmark.dao;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(RunRecordMapper.class)
public interface BenchmarkDao {
  @SqlUpdate("INSERT INTO run_results " + 
               "(benchmark_group_id, class_group_id, benchmark_group_run_id, " + 
               "   commit_hash, branch_name, benchmark_name, total_executions, duration) VALUES " + 
               "(:bGroupId, :cGroupId, :gRunId, :hash, :branch_name, :benchmark_name, :executions, :duration)")
  public void addRecord(@Bind("bGroupId") int bGroupId, @Bind("cGroupId") int cGroupId, @Bind("gRunId") int gRunId, 
                        @Bind("hash") String hash, @Bind("branch_name") String branchName, @Bind("benchmark_name") String benchmarkName, 
                        @Bind("executions") long totalExecutions, @Bind("duration") int duration);
  
  @SqlQuery("SELECT MAX(benchmark_group_run_id) FROM run_results WHERE benchmark_group_id = :bGroupId AND ignore_result = false")
  public int getLastBenchmarkGroupRunId(@Bind("bGroupId") int bGroupId);
  
  @SqlQuery("SELECT MAX(class_group_id) FROM run_results WHERE ignore_result = false")
  public int getMaxClassGroupId();
  
  @SqlQuery("SELECT MIN(benchmark_group_id) FROM run_results WHERE class_group_id = :cGroupId AND ignore_result = false")
  public int getMinBenchmarkGroupId(@Bind("cGroupId") int cGroupId);
  
  @SqlQuery("SELECT MAX(benchmark_group_id) FROM run_results WHERE class_group_id = :cGroupId AND ignore_result = false")
  public int getMaxBenchmarkGroupId(@Bind("cGroupId") int cGroupId);

  @SqlQuery("SELECT * FROM run_results" + 
              " WHERE benchmark_group_id = :bGroupId AND class_group_id = :cGroupId AND ignore_result = false" + 
              " ORDER BY run_timestamp")
  public List<RunRecord> getResultsForBenchmarkGroupId(@Bind("bGroupId") int bGroupId, 
                                                       @Bind("cGroupId") int cGroupId);
  @SqlQuery("SELECT * FROM run_results" + 
              " WHERE benchmark_group_id = :bGroupId AND class_group_id = :cGroupId AND ignore_result = false" + 
              " ORDER BY run_timestamp DESC LIMIT :limit")
  public List<RunRecord> getLastResultsForBenchmarkGroupId(@Bind("bGroupId") int bGroupId, 
                                                           @Bind("cGroupId") int cGroupId, 
                                                           @Bind("limit") int limit);

  @SqlQuery("SELECT DISTINCT(benchmark_name) FROM run_results" + 
              " WHERE benchmark_group_id = :bGroupId AND class_group_id = :cGroupId AND ignore_result = false")
  public List<String> getBenchmarkNamesForBenchmarkGroupId(@Bind("bGroupId") int bGroupId, 
                                                           @Bind("cGroupId") int cGroupId);
  
  @SqlQuery("SELECT name FROM class_group_identifier WHERE id = :cGroupId")
  public String getClassGroupIdentifier(@Bind("cGroupId") int cGroupId);
  
  @SqlQuery("SELECT name FROM benchmark_group_identifier WHERE id = :bGroupId")
  public String getBenchmarkGroupIdentifier(@Bind("bGroupId") int bGroupId);

  public void close();
}