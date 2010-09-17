/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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
	}
	
	public String toString() {
		return "c:" + name;
	}
	
	private Set<FieldRecord> watches = Collections.newSet();

	public void addWatch(FieldRecord fr) {
		watches.add(fr);
	}
	
	public void removeWatch(FieldRecord field) {
		watches.remove(field);
	}

	public FieldRecord getField(String owner, String name, String desc) {
		if (!this.name.equals(owner)) {
			System.err.printf("%s doesn't know about %s's fields\n", this.name, owner);
			return null;
		}
		for (FieldRecord fr: getFields()) {
			if (fr.name.equals(name)) {
				if (fr.desc.equals(desc)) {
					return fr;
				}
				else {
					System.err.printf("%s doesn't match %s for %s.%s\n", desc, fr.desc, owner, name);
					return null;
				}
			}
		}
		System.err.printf("could not find %s.%s (%s)\n", owner, name, desc);
		return null;
	}
	
	public Collection<FieldRecord> getWatches() {
		return Collections.immutable(watches);
	}

	public static Template<ClassRecord, ProfilerRun> getTemplate() {
		return template;
	}
	
	private static final Template<ClassRecord, ProfilerRun> template = new Template<ClassRecord, ProfilerRun>() {
		@Override
		public String createTable(ProfilerRun run) {
			return "create table classes_" + run.handle + " (id integer, name varchar(255), flags integer);";
		}

		@Override
		public String insert(ProfilerRun run) {
			return "insert into classes_" + run.handle + " (id, name, flags) values (?, ?, ?);";
		}

		@Override
		public BatchPreparedStatementSetter inserter(final List<ClassRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ClassRecord r = records.get(i);
					ps.setInt(1, r.id);
					ps.setString(2, r.name.substring(0, Math.min(r.name.length(), 255)));
					ps.setInt(3, r.flags);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String countSelect(ProfilerRun p, Object... filter) {
			return "select count(1) from classes_" + p.handle + ";";
		}

		@Override
		public String select(ProfilerRun run, Object... filter) {
			return "select * from classes_" + run.handle + ";";
		}
		
		@Override
		public String select(ProfilerRun p, int offset, int limit, Object... filter) {
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
				cr.flags = rs.getInt("flags");
				return cr;
			}
		};

		@Override
		public String update(ProfilerRun p) {
			return "update classes_" + p.handle + " set name = ?, flags = ? where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<ClassRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ClassRecord r = records.get(i);
					ps.setString(1, r.name);
					ps.setInt(2, r.flags);
					ps.setInt(3, r.id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String delete(ProfilerRun p) {
			return "delete from classes_" + p.handle + " where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter deleter(final List<ClassRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ClassRecord r = records.get(i);
					ps.setInt(1, r.id);
				}
				@Override
				public int getBatchSize() {
					return records.size();
				}
			};
		}

		@Override
		public String drop(ProfilerRun p) {
			return "drop table classes_" + p.handle + ";";
		}
	};
}
