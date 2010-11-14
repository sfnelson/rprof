/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.ClassInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassRecord extends ClassInfo<ClassRecord, MethodRecord, FieldRecord> {
	private static final long serialVersionUID = 5868120036712274141L;

	final Context context;
	final int id;

	private String name;
	private int flags;
	private String superName;

	public int version;
	public int access;
	public String signature;
	public String[] interfaces;

	ArrayList<MethodRecord> methods = Collections.newList();
	ArrayList<FieldRecord> fields = Collections.newList();

	ClassRecord(Context context, int id) {
		this.context = context;
		this.id = id;
	}

	void init(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.version = version;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superName = superName;
		this.interfaces = interfaces;
		
		if (superName == null) {
			superName = "";
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public List<MethodRecord> getMethods() {
		return Collections.immutable(methods);
	}

	@Override
	public List<FieldRecord> getFields() {
		return Collections.immutable(fields);
	}

	@Override
	public ClassRecord getSuper() {
		return context.getClass(superName);
	}

	public String toString() {
		return name;
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
	
	void addMethod(MethodRecord method) {
		methods.add(method);
		Collections.sort(methods);
	}

	void addField(FieldRecord field) {
		fields.add(field);
		Collections.sort(fields);
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
			return "create table classes_" + run.handle + " (id integer, name varchar(255), flags integer, super varchar(255));";
		}

		@Override
		public String insert(ProfilerRun run) {
			return "insert into classes_" + run.handle + " (id, name, flags, super) values (?, ?, ?, ?);";
		}

		@Override
		public BatchPreparedStatementSetter inserter(final List<ClassRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ClassRecord r = records.get(i);
					ps.setInt(1, r.getId());
					ps.setString(2, r.name.substring(0, Math.min(r.name.length(), 255)));
					ps.setInt(3, r.getFlags());
					
					if (r.superName == null) {
						ps.setString(4, null);
					}
					else {
						ps.setString(4, r.superName.substring(0, Math.min(r.superName.length(), 255)));
					}
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
		public RowMapper<ClassRecord> mapper(final Context context) {
			return new RowMapper<ClassRecord>() {
				@Override
				public ClassRecord mapRow(ResultSet rs, int row) throws SQLException {
					ClassRecord cr = new ClassRecord(context, rs.getInt("id"));
					cr.name = rs.getString("name");
					cr.flags = rs.getInt("flags");
					cr.superName = rs.getString("super");
					return cr;
				}
			};
		}

		@Override
		public String update(ProfilerRun p) {
			return "update classes_" + p.handle + " set name = ?, flags = ?, super = ?, where id = ?;";
		}

		@Override
		public BatchPreparedStatementSetter updater(final List<ClassRecord> records) {
			return new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ClassRecord r = records.get(i);
					ps.setString(1, r.name.substring(0, Math.min(r.name.length(), 255)));
					ps.setInt(2, r.getFlags());
					
					if (r.superName == null) {
						ps.setString(3, null);
					}
					else {
						ps.setString(3, r.superName.substring(0, Math.min(r.superName.length(), 255)));
					}
					
					ps.setInt(4, r.getId());
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
					ps.setInt(1, r.getId());
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
