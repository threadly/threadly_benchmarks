package org.threadly.concurrent.benchmark.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class RunRecordMapper implements RowMapper<RunRecord> {
  @Override
  public RunRecord map(ResultSet r, StatementContext ctx) throws SQLException {
    return new RunRecord(r.getInt("benchmark_group_id"), r.getInt("class_group_id"), 
                         r.getInt("benchmark_group_run_id"), r.getTimestamp("run_timestamp").getTime(), 
                         r.getString("commit_hash"), r.getString("branch_name"), 
                         r.getString("benchmark_name"), r.getInt("total_executions"), r.getInt("duration"));
  }
}
