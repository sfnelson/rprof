/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.Context;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class MethodRecord extends nz.ac.vuw.ecs.rprofs.client.data.MethodRecord {
	private static final long serialVersionUID = -357201240938009655L;

	public ClassRecord parent;
	
	public String signature;
	public String[] exceptions;
	
	public MethodRecord() {}
	
	public MethodRecord(ClassRecord parent) {
		this.parent = parent;
	}

	public static MethodRecord create(ClassRecord parent, int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodRecord mr = Context.getCurrent().createMethodRecord(parent);
		mr.access = access;
		mr.name = name;
		mr.desc = desc;
		mr.signature = signature;
		mr.exceptions = exceptions;
		return mr;
	}
	
	public String toString() {
		return parent + "." + name + ":" + desc;
	}
	
	public String toMethodString() {
		return name + ":" + desc;
	}
	
	public static Template<MethodRecord, ProfilerRun> getTemplate() {
		return template;
	}

	private static final Template<MethodRecord, ProfilerRun> template = new Template<MethodRecord, ProfilerRun>() {

		@Override
		public String createTable(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
			return "create table methods_" + run.handle + " (mid integer, cid integer, name varchar(255), description varchar(255), access integer);";
		}

		@Override
		public String insert(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun p) {
			return "insert into methods_" + p.handle + " (mid, cid, name, description, access) values (?, ?, ?, ?, ?);";
		}

		@Override
		public BatchPreparedStatementSetter inserter(final List<MethodRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr, int i) throws SQLException {
					MethodRecord mr = records.get(i);
					cr.setInt(1, mr.id);
					cr.setInt(2, mr.parent.id);
					cr.setString(3, mr.name.substring(0, Math.min(mr.name.length(), 255)));
					cr.setString(4, mr.desc.substring(0, Math.min(mr.desc.length(), 255)));
					cr.setInt(5, mr.access);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> RowMapper<MethodRecord> mapper(T param) {
			final Map<Integer, ClassRecord> map = (Map<Integer, ClassRecord>) param;
			return new RowMapper<MethodRecord>() {
				public MethodRecord mapRow(ResultSet rs, int row) throws SQLException {
					MethodRecord mr = new MethodRecord();
					mr.id = rs.getInt(1);
					mr.parent = map.get(rs.getInt(2));
					mr.parent.getMethods().add(mr);
					mr.name = rs.getString(3);
					mr.desc = rs.getString(4);
					mr.access = rs.getInt(5);
					return mr;
				}
			};
		}

		@Override
		public String countSelect(ProfilerRun p, Object... filter) {
			return "select count(1) from methods_" + p.handle + ";";
		}

		@Override
		public String select(ProfilerRun p, Object... filter) {
			return "select * from methods_" + p.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit, Object... filter) {
			return "select * from methods_" + p.handle + " order by id limit "
			+ limit + " offset " + offset + ";";
		}

		@Override
		public String update(ProfilerRun p) {
			return "update methods_" + p.handle + " set name = ?, desc = ?, access = ? where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<MethodRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr, int i) throws SQLException {
					MethodRecord r = records.get(i);
					cr.setString(1, r.name.substring(0, Math.min(r.name.length(), 255)));
					cr.setString(2, r.desc.substring(0, Math.min(r.desc.length(), 255)));
					cr.setInt(3, r.access);
					cr.setInt(4, r.id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from methods_" + p.handle + " where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter deleter(final List<MethodRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr, int i) throws SQLException {
					cr.setInt(1, records.get(i).id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String drop(ProfilerRun run) {
			return "drop table methods_" + run.handle + ";";
		}
	};
}
