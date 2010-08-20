/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.Context;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@SuppressWarnings("serial")
public class LogRecord extends nz.ac.vuw.ecs.rprofs.client.data.LogRecord {
	
	public static LogRecord create() {
		return new LogRecord(Context.getInstance().nextEvent());
	}
	
	public LogRecord(long index) {
		this.index = index;
	}

	/**
	 * @return
	 */
	public static Template<LogRecord, ProfilerRun> getTemplate() {
		return template;
	}

	private static final Template<LogRecord, ProfilerRun> template = new Template<LogRecord, ProfilerRun>() {

		@Override
		public String createTable(ProfilerRun p) {
			return "create table events_" + p.handle
			+ " (index bigint, thread bigint, event varchar(63), cnum integer, mnum integer, args bigint[]);";
		}

		@Override
		public String countSelectAll(ProfilerRun p) {
			return "select count(1) from events_" + p.handle + ";";
		}
		
		@Override
		public String selectAll(ProfilerRun p) {
			return "select * from events_" + p.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit) {
			return "select * from events_" + p.handle + " order by index limit "
			+ limit + " offset " + offset + ";";
		}

		@Override
		public <T> RowMapper<LogRecord> mapper(T param) {
			return mapper;
		}

		private final RowMapper<LogRecord> mapper = new RowMapper<LogRecord>() {
			public LogRecord mapRow(ResultSet rs, int row) throws SQLException {
				LogRecord r = new LogRecord(rs.getLong("index"));
				r.thread = rs.getLong("thread");
				r.event = rs.getInt("event");
				r.cnum = rs.getInt("cnum");
				r.mnum = rs.getInt("mnum");
				r.args = ArrayUtils.toPrimitive((Long[]) rs.getArray("args").getArray());
				return r;
			}
		};

		@Override
		public String insert(ProfilerRun p) {
			return "insert into events_" + p.handle +
			" (index, thread, event, cnum, mnum, args) values (?, ?, ?, ?, ?, ?);";
		}

		@Override
		public PreparedStatementSetter inserter(final LogRecord cr) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps)
						throws SQLException {
					ps.setLong(1, cr.index);
					ps.setLong(2, cr.thread);
					ps.setInt(3, cr.event);
					ps.setInt(4, cr.cnum);
					ps.setInt(5, cr.mnum);
					ps.setArray(6, ps.getConnection().createArrayOf("bigint", ArrayUtils.toObject(cr.args)));
				}
			};
		}
		
		@Override
		public String update(ProfilerRun p) {
			return "update events_" + p.handle + " set cnum = ?, mnum = ? where index = ?;";
		}

		@Override
		public PreparedStatementSetter updater(final LogRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps)
						throws SQLException {
					ps.setInt(1, r.cnum);
					ps.setInt(2, r.mnum);
					ps.setLong(3, r.index);
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from events_" + p.handle + " where index = ?;";
		}

		@Override
		public PreparedStatementSetter deleter(final LogRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setLong(1, r.index);
				}
			};
		}

		@Override
		public String drop(ProfilerRun p) {
			return "drop table events_" + p.handle + ";";
		}
	};
}
