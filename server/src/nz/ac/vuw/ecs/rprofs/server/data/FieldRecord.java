/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.FieldInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.google.gwt.dev.asm.Opcodes;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class FieldRecord extends FieldInfo implements Comparable<FieldRecord> {
	private static final long serialVersionUID = 2503578755127962360L;

	public final ClassRecord parent;
	public final int id;
	
	public String name;
	public String desc;
	public int access;
	public boolean equals;
	public boolean hash;

	FieldRecord(ClassRecord parent, int id) {
		this.parent = parent;
		this.id = id;
		
		parent.addField(this);
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass().equals(this.getClass())) {
			FieldRecord fr = (FieldRecord) o;
			if (this.parent.equals(fr.parent)) {
				return fr.id == this.id;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return id;
	}
	
	public int compareTo(FieldRecord fr) {
		if (fr == null) return -1;
		if (this.parent.equals(fr.parent)) {
			return this.id - fr.id;
		}
		else {
			return this.parent.compareTo(fr.parent);
		}
	}

	void init(int access, String name, String desc) {
		this.name = name;
		this.desc = desc;
		this.access = access;
		if ((Opcodes.ACC_STATIC & access) == 0) {
			parent.addWatch(this);
		}
	}
	
	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean inEquals() {
		return equals;
	}

	@Override
	public boolean inHashCode() {
		return hash;
	}
	
	public String toString() {
		return parent + "." + name + ":" + desc;
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
					ps.setInt(1, r.getId());
					ps.setInt(2, r.parent.getId());
					ps.setString(3, r.getName().substring(0, Math.min(r.name.length(), 255)));
					ps.setString(4, r.desc.substring(0, Math.min(r.desc.length(), 255)));
					ps.setBoolean(5, r.inEquals());
					ps.setBoolean(6, r.inHashCode());
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public RowMapper<FieldRecord> mapper(final Context context) {
			return new RowMapper<FieldRecord>() {
				public FieldRecord mapRow(ResultSet rs, int row) throws SQLException {
					int id = rs.getInt(1);
					int pid = rs.getInt(2);
					FieldRecord mr = new FieldRecord(context.getClass(pid), id);
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
					cr.setString(1, r.getName().substring(0, Math.min(r.name.length(), 255)));
					cr.setString(2, r.getDescription().substring(0, Math.min(r.desc.length(), 255)));
					cr.setBoolean(3, r.inEquals());
					cr.setBoolean(4, r.inHashCode());
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
					cr.setInt(1, r.getId());
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
