/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.Context;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class LogRecord extends nz.ac.vuw.ecs.rprofs.client.data.LogRecord {
	
	public static LogRecord create() {
		return new LogRecord(Context.getCurrent().nextEvent());
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
			+ " (index bigint, thread bigint, event integer, cnum integer, mnum integer, args bigint[]);";
		}

		@Override
		public String countSelect(ProfilerRun p, Object... filter) {
			return "select count(1) from events_" + p.handle
				+ getFilterClause(filter) + ";";
		}
		
		@Override
		public String select(ProfilerRun p, Object... filter) {
			return "select * from events_" + p.handle
			+ getFilterClause(filter) + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit, Object... filter) {
			return "select * from events_" + p.handle + getFilterClause(filter)
			+ " order by index limit " + limit + " offset " + offset + ";";
		}
		
		private String getFilterClause(Object... filter) {
			assert(filter.length == 1);
			assert(filter[0] != null);
			assert(filter[0] instanceof Integer);
			int type = (Integer) filter[0];
			if (type == LogRecord.ALL) return "";
			return " where (event & " + type + ") <> 0";
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
		public BatchPreparedStatementSetter inserter(final List<LogRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i)
						throws SQLException {
					LogRecord r = records.get(i);
					ps.setLong(1, r.index);
					ps.setLong(2, r.thread);
					ps.setInt(3, r.event);
					ps.setInt(4, r.cnum);
					ps.setInt(5, r.mnum);
					ps.setArray(6, ps.getConnection().createArrayOf("bigint", ArrayUtils.toObject(r.args)));
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}
		
		@Override
		public String update(ProfilerRun p) {
			return "update events_" + p.handle + " set cnum = ?, mnum = ? where index = ?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<LogRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					LogRecord r = records.get(i);
					ps.setInt(1, r.cnum);
					ps.setInt(2, r.mnum);
					ps.setLong(3, r.index);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from events_" + p.handle + " where index = ?;";
		}

		@Override
		public BatchPreparedStatementSetter deleter(final List<LogRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					LogRecord r = records.get(i);
					ps.setLong(1, r.index);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String drop(ProfilerRun p) {
			return "drop table events_" + p.handle + ";";
		}
	};
}
