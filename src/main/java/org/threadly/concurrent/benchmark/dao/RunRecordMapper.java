package org.threadly.concurrent.benchmark.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class RunRecordMapper implements ResultSetMapper<RunRecord> {
  @Override
  public RunRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    return new RunRecord(r.getInt("benchmark_group_id"), r.getInt("class_group_id"), 
                         r.getInt("benchmark_group_run_id"), r.getTimestamp("run_timestamp").getTime(), 
                         r.getString("commit_hash"), r.getString("branch_name"), 
                         r.getString("benchmark_name"), r.getInt("total_executions"), r.getInt("duration"));
  }
}
