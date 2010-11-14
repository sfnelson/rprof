/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.MethodInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class MethodRecord extends MethodInfo implements Comparable<MethodRecord> {
	private static final long serialVersionUID = -357201240938009655L;

	public final ClassRecord parent;
	private final int id;

	private String name;
	private String desc;
	private int access;

	public String signature;
	public String[] exceptions;

	MethodRecord(ClassRecord parent, int id) {
		this.parent = parent;
		this.id = id;

		parent.addMethod(this);
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass().equals(this.getClass())) {
			MethodRecord mr = (MethodRecord) o;
			if (this.parent.equals(mr.parent)) {
				return mr.id == this.id;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return id;
	}

	public int compareTo(MethodRecord mr) {
		if (parent.equals(mr.parent)) {
			return this.id - mr.id;
		}
		return parent.compareTo(mr.parent);
	}

	public void init(int access, String name, String desc, String signature, String[] exceptions) {
		this.access = access;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.exceptions = exceptions;
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

	public String toString() {
		return parent + "." + name + ":" + desc;
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
					cr.setInt(1, mr.getId());
					cr.setInt(2, mr.parent.getId());
					cr.setString(3, mr.getName().substring(0, Math.min(mr.getName().length(), 255)));
					cr.setString(4, mr.getDescription().substring(0, Math.min(mr.getDescription().length(), 255)));
					cr.setInt(5, mr.access);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public RowMapper<MethodRecord> mapper(final Context param) {
			return new RowMapper<MethodRecord>() {
				public MethodRecord mapRow(ResultSet rs, int row) throws SQLException {
					int id = rs.getInt(1);
					int pid = rs.getInt(2);
					MethodRecord mr = new MethodRecord(param.getClass(pid), id);
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
					cr.setString(1, r.getName().substring(0, Math.min(r.name.length(), 255)));
					cr.setString(2, r.getDescription().substring(0, Math.min(r.desc.length(), 255)));
					cr.setInt(3, r.getAccess());
					cr.setInt(4, r.getId());
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
					cr.setInt(1, records.get(i).getId());
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
