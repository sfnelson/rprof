/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.google.gwt.dev.asm.Opcodes;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.Context;

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
		FieldRecord mr = Context.getInstance().createFieldRecord(parent);
		mr.name = name;
		mr.desc = desc;
		mr.access = access;
		return mr;
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
			return "create table fields_" + run.handle + " (mid integer, cid integer, name varchar(255), description varchar(255));";
		}

		@Override
		public String insert(ProfilerRun p) {
			return "insert into methods_" + p.handle + " (mid, cid, name, description) values (?, ?, ?, ?);";
		}

		@Override
		public PreparedStatementSetter inserter(final FieldRecord mr) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr) throws SQLException {
					cr.setInt(1, mr.id);
					cr.setInt(2, mr.parent.id);
					cr.setString(3, mr.name.substring(0, Math.min(mr.name.length(), 255)));
					cr.setString(4, mr.desc.substring(0, Math.min(mr.desc.length(), 255)));
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
					return mr;
				}
			};
		}

		@Override
		public String countSelectAll(ProfilerRun p) {
			return "select count(1) from fields_" + p.handle + ";";
		}

		@Override
		public String selectAll(ProfilerRun p) {
			return "select * from fields_" + p.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit) {
			return "select * from fields_" + p.handle + " order by id limit "
			+ limit + " offset " + offset + ";";
		}

		@Override
		public String update(ProfilerRun p) {
			return "update fields_" + p.handle + " set name = ?, desc = ? where id = ?;";
		}

		@Override
		public PreparedStatementSetter updater(final FieldRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr) throws SQLException {
					cr.setString(1, r.name.substring(0, Math.min(r.name.length(), 255)));
					cr.setString(2, r.desc.substring(0, Math.min(r.desc.length(), 255)));
					cr.setInt(3, r.id);
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from fields_" + p.handle + " where id = ?;";
		}

		@Override
		public PreparedStatementSetter deleter(final FieldRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement cr) throws SQLException {
					cr.setInt(1, r.id);
				}
			};
		}

		@Override
		public String drop(ProfilerRun run) {
			return "drop table fields_" + run.handle + ";";
		}
	};
}
