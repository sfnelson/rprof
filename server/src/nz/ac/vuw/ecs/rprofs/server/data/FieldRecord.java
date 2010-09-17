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

import com.google.gwt.dev.asm.Opcodes;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class FieldRecord extends nz.ac.vuw.ecs.rprofs.client.data.FieldRecord {
	private static final long serialVersionUID = 2503578755127962360L;

	public ClassRecord parent;
	
	public int access;
	
	public FieldRecord() {}
	
	public FieldRecord(ClassRecord parent) {
		this.parent = parent;
	}

	public static FieldRecord create(ClassRecord parent, int access, String name, String desc) {
		FieldRecord fr = Context.getCurrent().createFieldRecord(parent);
		fr.name = name;
		fr.desc = desc;
		fr.access = access;
		if ((Opcodes.ACC_STATIC & access) == 0) {
			parent.addWatch(fr);
		}
		return fr;
	}
	
	public String toString() {
		return "f:" + parent + "." + name + ":" + desc;
	}
	
	public boolean isStatic() {
		return (Opcodes.ACC_STATIC & access) != 0;
	}
	
	public static Template<FieldRecord, ProfilerRun> getTemplate() {
		return template;
	}

	private static final Template<FieldRecord, ProfilerRun> template = new Template<FieldRecord, ProfilerRun>() {

		@Override
		public String createTable(ProfilerRun run) {
			return "create table fields_" + run.handle + " (mid integer, cid integer, name varchar(255), description varchar(255), equals boolean, hash boolean);";
		}

		@Override
		public String insert(ProfilerRun p) {
			return "insert into fields_" + p.handle + " (mid, cid, name, description, equals, hash) values (?, ?, ?, ?, ?, ?);";
		}

		@Override
		public BatchPreparedStatementSetter inserter(final List<FieldRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					FieldRecord r = records.get(i);
					ps.setInt(1, r.id);
					ps.setInt(2, r.parent.id);
					ps.setString(3, r.name.substring(0, Math.min(r.name.length(), 255)));
					ps.setString(4, r.desc.substring(0, Math.min(r.desc.length(), 255)));
					ps.setBoolean(5, r.equals);
					ps.setBoolean(6, r.hash);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> RowMapper<FieldRecord> mapper(T param) {
			final Map<Integer, ClassRecord> map = (Map<Integer, ClassRecord>) param;
			return new RowMapper<FieldRecord>() {
				public FieldRecord mapRow(ResultSet rs, int row) throws SQLException {
					FieldRecord mr = new FieldRecord();
					mr.id = rs.getInt(1);
					mr.parent = map.get(rs.getInt(2));
					mr.parent.getFields().add(mr);
					mr.name = rs.getString(3);
					mr.desc = rs.getString(4);
					mr.equals = rs.getBoolean(5);
					mr.hash = rs.getBoolean(6);
					return mr;
				}
			};
		}

		@Override
		public String countSelect(ProfilerRun p, Object... filter) {
			return "select count(1) from fields_" + p.handle + ";";
		}

		@Override
		public String select(ProfilerRun p, Object... filter) {
			return "select * from fields_" + p.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit, Object... filter) {
			return "select * from fields_" + p.handle + " order by id limit "
			+ limit + " offset " + offset + ";";
		}

		@Override
		public String update(ProfilerRun p) {
			return "update fields_" + p.handle + " set name = ?, desc = ?, equals = ?, hash = ? where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<FieldRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr, int i) throws SQLException {
					FieldRecord r = records.get(i);
					cr.setString(1, r.name.substring(0, Math.min(r.name.length(), 255)));
					cr.setString(2, r.desc.substring(0, Math.min(r.desc.length(), 255)));
					cr.setBoolean(3, r.equals);
					cr.setBoolean(4, r.hash);
					cr.setInt(5, r.id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from fields_" + p.handle + " where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter deleter(final List<FieldRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr, int i) throws SQLException {
					FieldRecord r = records.get(i);
					cr.setInt(1, r.id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String drop(ProfilerRun run) {
			return "drop table fields_" + run.handle + ";";
		}
	};
}
