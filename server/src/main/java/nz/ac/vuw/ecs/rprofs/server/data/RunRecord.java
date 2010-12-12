/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import nz.ac.vuw.ecs.rprofs.client.data.RunInfo;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table(name = "profiler_runs")
public class RunRecord extends nz.ac.vuw.ecs.rprofs.client.data.RunInfo {

	@Id private String handle;
	private String program;
	private Date started;
	private Date stopped;

	@Transient int numClasses;

	public RunRecord() {}

	public static RunRecord create() {
		RunRecord run = new RunRecord();
		Calendar s = Calendar.getInstance();

		run.started = s.getTime();
		run.handle = String.format("%02d%02d%02d%02d%02d%02d",
				s.get(Calendar.YEAR),
				s.get(Calendar.MONTH),
				s.get(Calendar.DATE),
				s.get(Calendar.HOUR),
				s.get(Calendar.MINUTE),
				s.get(Calendar.SECOND));

		return run;
	}

	public RunRecord(RunInfo run) {
		this(run.getProgram(), run.getStarted(), run.getStopped(), run.getHandle());
	}

	public RunRecord(String program, Date started, Date stopped, String handle) {
		this.program = program;
		this.started = started;
		this.stopped = stopped;
		this.handle = handle;
	}

	@Override
	public String toString() {
		return handle;
	}

	@Override
	public String getHandle() {
		return handle;
	}

	@Override
	public String getProgram() {
		return program;
	}

	@Override
	public Date getStarted() {
		return started;
	}

	@Override
	public Date getStopped() {
		return stopped;
	}

	public void setStopped(Date time) {
		stopped = time;
	}

	public void setProgram(String name) {
		program = name;
	}

	/*
	public static Template<RunRecord, Void> getTemplate() {
		return template;
	}


	private static final Template<RunRecord, Void> template = new Template<RunRecord, Void>() {

		@Override
		public String createTable(Void param) {
			return "create table profiler_runs "
			+ "(program varchar(255), started timestamp, stopped timestamp, handle varchar(63))";
		}

		@Override
		public String insert(Void p) {
			return "insert into profiler_runs (program, started, stopped, handle) values (?, ?, ?, ?);";
		}

		@Override
		public BatchPreparedStatementSetter inserter(final List<RunRecord> runs) {
			return new BatchPreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					RunRecord cr = runs.get(i);
					String program = cr.program;
					if (program != null && program.length() > 255) {
						program = program.substring(0, 255);
					}

					String handle = cr.handle;
					if (handle != null && handle.length() > 63) {
						handle = handle.substring(0, 63);
					}

					Timestamp started = null;
					if (cr.started != null) {
						started = new Timestamp(cr.started.getTime());
					}

					Timestamp stopped = null;
					if (cr.stopped != null) {
						stopped = new Timestamp(cr.stopped.getTime());
					}

					ps.setString(1, program);
					ps.setTimestamp(2, started);
					ps.setTimestamp(3, stopped);
					ps.setString(4, handle);
				}

				@Override
				public int getBatchSize() {
					return runs.size();
				}
			};
		}

		@Override
		public String countSelect(Void param, Object... filter) {
			return "select count(1) from profiler_runs;";
		}

		@Override
		public String select(Void p, Object... filter) {
			return "select * from profiler_runs order by started;";
		}

		@Override
		public String select(Void p, int offset, int limit, Object... filter) {
			return "select * from profiler_runs order by started limit "
			+ limit + " offset " + offset + ";";
		}

		@Override
		public RowMapper<RunRecord> mapper(Context param) {
			return mapper;
		}

		private final RowMapper<RunRecord> mapper = new RowMapper<RunRecord>() {
			@Override
			public RunRecord mapRow(ResultSet rs, int row) throws SQLException {
				RunRecord run = new RunRecord();
				run.program = rs.getString("program");
				run.started = rs.getTimestamp("started");
				run.stopped = rs.getTimestamp("stopped");
				run.handle = rs.getString("handle");
				return run;
			}
		};

		@Override
		public String update(Void p) {
			return "update profiler_runs set program=?, stopped=? where started=?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<RunRecord> runs) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					RunRecord r = runs.get(i);
					String program = r.program;
					if (program != null && program.length() > 255) {
						program = program.substring(0, 255);
					}

					Timestamp started = null;
					if (r.started != null) {
						started = new Timestamp(r.started.getTime());
					}

					Timestamp stopped = null;
					if (r.stopped != null) {
						stopped = new Timestamp(r.stopped.getTime());
					}

					ps.setString(1, program);
					ps.setTimestamp(2, stopped);
					ps.setTimestamp(3, started);
				}

				@Override
				public int getBatchSize() {
					return runs.size();
				}
			};
		}

		@Override
		public String delete(Void p) {
			return "delete from profiler_runs where started = ?;";
		}

		@Override
		public BatchPreparedStatementSetter deleter(final List<RunRecord> runs) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					RunRecord r = runs.get(i);
					Timestamp started = null;
					if (r.started != null) {
						started = new Timestamp(r.started.getTime());
					}

					ps.setTimestamp(1, started);
				}
				@Override
				public int getBatchSize() {
					return runs.size();
				}
			};
		}

		@Override
		public String drop(Void run) {
			return "drop table profiler_runs;";
		}
	};
	 */
}
