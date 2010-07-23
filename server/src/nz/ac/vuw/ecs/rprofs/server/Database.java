package nz.ac.vuw.ecs.rprofs.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;


public class Database implements InitializingBean {

	private DataSource db;
	private JdbcTemplate template;

	public void setDataSource(DataSource db) {
		this.db = db;
	}

	public void afterPropertiesSet() throws Exception {
		template = new JdbcTemplate(db);

		//template.update("create table profiler_runs (program varchar(20), started timestamp, stopped timestamp, handle varchar(63))");
	}

	public void storeClasses(ProfilerRun run, List<ClassRecord> classes) {
		for (ClassRecord record: classes) {
			template.update("insert into classes_" + run.handle + " (id, name) values (?, ?);", record.id, record.name);
		}
	}
	
	public List<ClassRecord> getClasses(ProfilerRun run) {
		List<ClassRecord> classes = template.query("select * from classes_" + run.handle + ";",
				new RowMapper<ClassRecord>() {
			public ClassRecord mapRow(ResultSet rs, int row) throws SQLException {
				ClassRecord cr = new ClassRecord();
				cr.id = rs.getInt(1);
				cr.name = rs.getString(2);
				return cr;
			}
		});

		return classes;
	}

	public List<ProfilerRun> getProfiles() {
		List<ProfilerRun> runs = template.query("select * from profiler_runs;", new RowMapper<ProfilerRun>() {
			public ProfilerRun mapRow(ResultSet rs, int row) throws SQLException {
				ProfilerRun run = new ProfilerRun();
				run.program = rs.getString(1);
				run.started = rs.getTimestamp(2);
				run.stopped = rs.getTimestamp(3);
				run.handle = rs.getString(4);
				return run;
			}
		});

		return runs;
	}

	public ProfilerRun createRun() {

		Calendar s = Calendar.getInstance();

		ProfilerRun run = new ProfilerRun();
		run.started = s.getTime();
		run.handle = String.format("%02d%02d%02d%02d%02d%02d",
				s.get(Calendar.YEAR),
				s.get(Calendar.MONTH),
				s.get(Calendar.DATE),
				s.get(Calendar.HOUR),
				s.get(Calendar.MINUTE),
				s.get(Calendar.SECOND));

		template.update("insert into profiler_runs (program, started, stopped, handle) values (?, ?, ?, ?)",
				run.program, run.started, run.stopped, run.handle);

		template.update("create table classes_" + run.handle + " (id integer, name varchar(255));");
		template.update("create table events_" + run.handle
				+ " (index serial, thread bigint, event varchar(63), cname varchar(255), mname varchar(255), cnum integer, mnum integer, len integer"
				+ ", arg0 bigint, arg1 bigint, arg2 bigint, arg3 bigint, arg4 bigint, arg5 bigint, arg6 bigint, arg7 bigint, arg8 bigint, arg9 bigint, arg10 bigint, arg11 bigint, arg12 bigint, arg13 bigint, arg14 bigint, arg15 bigint"
				+ ");");

		return run;
	}

	public void update(ProfilerRun run) {
		template.update("update profiler_runs set program=?, stopped=? where started=?;",
				run.program, run.stopped, run.started);
	}

	public void dropRun(ProfilerRun run) {
		template.update("delete from profiler_runs where started=?;", run.started);
		template.update("drop table classes_" + run.handle + ";");
		template.update("drop table events_" + run.handle + ";");
	}

	public List<LogRecord> getLogs(ProfilerRun run) {
		List<LogRecord> classes = template.query(
				"select * from events_" + run.handle + ";",
				new LogRowMapper());

		return classes;
	}
	
	public List<LogRecord> getLogs(ProfilerRun run, int offset, int limit) {
		List<LogRecord> classes = template.query(
				"select * from events_" + run.handle + " order by index limit "
					+ limit + " offset " + offset + ";",
				new LogRowMapper());

		return classes;
	}
	
	public int getNumLogs(ProfilerRun run) {
		List<Integer> number = template.query(
				"select count(1) from events_" + run.handle + ";",
				new RowMapper<Integer>() {

					public Integer mapRow(ResultSet rs, int row)
							throws SQLException {
						return rs.getInt(1);
					}
					
				});
		return number.get(0);
	}
	
	private static class LogRowMapper implements RowMapper<LogRecord> {
		public LogRecord mapRow(ResultSet rs, int row) throws SQLException {
			LogRecord r = new LogRecord();
			r.index = rs.getInt("index");
			r.threadId = rs.getLong("thread");
			r.event = rs.getString("event");
			r.className = rs.getString("cname");
			r.methodName = rs.getString("mname");
			r.classNumber = rs.getInt("cnum");
			r.methodNumber = rs.getInt("mnum");
			
			int numArgs = rs.getInt("len");
			r.arguments = new long[numArgs];
			for (int i = 0; i < numArgs; i++) {
				r.arguments[i] = rs.getLong("arg" + i);
			}
			return r;
		}
	}

	public void storeLogs(ProfilerRun run, List<LogRecord> records) {
		for (LogRecord record: records) {
			int len = record.arguments.length;
			if (len > 16) {
				len = 16;
				System.err.println("warning: more arguments than max - " + record);
			}
			template.update("insert into events_" + run.handle
					+ " (thread, event, cname, mname, cnum, mnum, len"
					+ ", arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15"
					+ ")"
					+ " values (?, ?, ?, ?, ?, ?, ?"
					+ ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
					record.threadId,
					record.event,
					record.className,
					record.methodName,
					record.classNumber,
					record.methodNumber,
					len,
					(0 < len ? record.arguments[0] : 0),
					(1 < len ? record.arguments[1] : 0),
					(2 < len ? record.arguments[2] : 0),
					(3 < len ? record.arguments[3] : 0),
					(4 < len ? record.arguments[4] : 0),
					(5 < len ? record.arguments[5] : 0),
					(6 < len ? record.arguments[6] : 0),
					(7 < len ? record.arguments[7] : 0),
					(8 < len ? record.arguments[8] : 0),
					(9 < len ? record.arguments[9] : 0),
					(10 < len ? record.arguments[10] : 0),
					(11 < len ? record.arguments[11] : 0),
					(12 < len ? record.arguments[12] : 0),
					(13 < len ? record.arguments[13] : 0),
					(14 < len ? record.arguments[14] : 0),
					(15 < len ? record.arguments[15] : 0));
		}
	}

	/*
	public void connect(String address, Calendar logout) {
		Timestamp time = new Timestamp(logout.getTimeInMillis());

		template.update("insert into current_users (address, logout) values (?, ?)", address, time);
	}

	public State getState(final String address) {

		List<State> states = template.query("select * from current_users where address = ?",
				new Object[] { address },
				new RowMapper<State>() {
					public State mapRow(ResultSet rs, int row)
							throws SQLException {
						return new State("Vistagate", address, State.LOGGED_IN, rs.getTimestamp(2));
					}
				}
		);

		return states.isEmpty() ? null : states.get(0);
	}*/

}
