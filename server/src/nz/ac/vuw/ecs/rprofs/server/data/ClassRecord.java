/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassRecord extends nz.ac.vuw.ecs.rprofs.client.data.ClassRecord<MethodRecord, FieldRecord> {
	private static final long serialVersionUID = 5868120036712274141L;

	public int version;
	public int access;
	public String signature;
	public String superName;
	public String[] interfaces;

	public int id() {
		return id;
	}
	
	public void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		this.instances = 0;
	}
	
	public String toString() {
		return "c:" + name;
	}

	public static Template<ClassRecord, ProfilerRun> getTemplate() {
		return template;
	}
	
	private static final Template<ClassRecord, ProfilerRun> template = new Template<ClassRecord, ProfilerRun>() {
		@Override
		public String createTable(ProfilerRun run) {
			return "create table classes_" + run.handle + " (id integer, name varchar(255), instances integer);";
		}

		@Override
		public String insert(ProfilerRun run) {
			return "insert into classes_" + run.handle + " (id, name, instances) values (?, ?, ?);";
		}

		@Override
		public PreparedStatementSetter inserter(final ClassRecord cr) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setInt(1, cr.id);
					ps.setString(2, cr.name.substring(0, Math.min(cr.name.length(), 255)));
					ps.setInt(3, cr.instances);
				}
			};
		}

		@Override
		public String countSelectAll(ProfilerRun p) {
			return "select count(1) from classes_" + p.handle + ";";
		}

		@Override
		public String selectAll(ProfilerRun run) {
			return "select * from classes_" + run.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit) {
			return "select * from classes_" + p.handle + " order by id limit"
			+ limit + " offset " + offset + ";";
		}
		
		@Override
		public <T> RowMapper<ClassRecord> mapper(T param) {
			return mapper;
		}
		
		private final RowMapper<ClassRecord> mapper = new RowMapper<ClassRecord>() {
			@Override
			public ClassRecord mapRow(ResultSet rs, int row) throws SQLException {
				ClassRecord cr = new ClassRecord();
				cr.id = rs.getInt("id");
				cr.name = rs.getString("name");
				cr.instances = rs.getInt("instances");
				return cr;
			}
		};

		@Override
		public String update(ProfilerRun p) {
			return "update classes_" + p.handle + " set name = ?, instances = ? where id = ?;";
		}

		@Override
		public PreparedStatementSetter updater(final ClassRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setString(1, r.name);
					ps.setInt(2, r.instances);
					ps.setInt(3, r.id);
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from classes_" + p.handle + " where id = ?;";
		}

		@Override
		public PreparedStatementSetter deleter(final ClassRecord r) {
			return new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setInt(1, r.id);
				}
			};
		}

		@Override
		public String drop(ProfilerRun p) {
			return "drop table classes_" + p.handle + ";";
		}
	};
}
