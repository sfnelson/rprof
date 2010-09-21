/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Report implements IsSerializable, Comparable<Report> {
	
	public String name;
	public String[] headings;
	public String[] headingTitle;
	public Type[] types;
	public String[][] flags;
	
	public Report() {}
	public Report(String name, int length) {
		this.name = name;
		headings = new String[length];
		headingTitle = new String[length];
		types = new Type[length];
		flags = new String[length][];
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!getClass().equals(o.getClass())) return false;
		return name.equals(((Report) o).name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public int compareTo(Report o) {
		return this.name.compareTo(o.name);
	}
	
	public static class Status implements IsSerializable {
		public State state;
		public int progress;
		public int limit;
		public String stage;
	}
	
	public static abstract class Entry implements IsSerializable, Comparable<Entry> {
		public int[] values;
		
		public Entry() {}
		public Entry(int... values) {
			this.values = values;
		}
		
		public abstract <T> T visit(EntryVisitor<T> visitor);
	}
	
	public static class PackageEntry extends Entry {
		public String pkg;
		public int classes;
		public int instances;
		
		public PackageEntry() {}
		public PackageEntry(String pkg, int classes, int instances, int... values) {
			super(values);
			this.pkg = pkg;
			this.classes = classes;
			this.instances = instances;
		}

		@Override
		public <T> T visit(EntryVisitor<T> visitor) {
			return visitor.visitPackageEntry(this);
		}
		
		@Override
		public String toString() {
			return "pkg:" + pkg;
		}
		
		@Override
		public int compareTo(Entry o) {
			if (o instanceof PackageEntry) {
				return pkg.compareTo(((PackageEntry) o).pkg);
			}
			else {
				return -1;
			}
		}
	}
	
	public static class ClassEntry extends Entry {
		public ClassRecord<MethodRecord, FieldRecord> cls;
		public int instances;
		
		public ClassEntry() {}
		public ClassEntry(ClassRecord<MethodRecord, FieldRecord> cls, int instances, int... values) {
			super(values);
			this.cls = cls;
			this.instances = instances;
		}

		@Override
		public <T> T visit(EntryVisitor<T> visitor) {
			return visitor.visitClassEntry(this);
		}
		
		@Override
		public String toString() {
			return "cls:" + cls.name;
		}

		@Override
		public int compareTo(Entry o) {
			if (o instanceof ClassEntry) {
				return cls.compareTo(((ClassEntry) o).cls);
			}
			else {
				return -o.compareTo(this);
			}
		}
	}
	
	public static class InstanceEntry extends Entry {
		public long id;
		public String name;
		
		public InstanceEntry() {}
		public InstanceEntry(long id, int... values) {
			super(values);
			this.id = id;
		}
		public InstanceEntry(String name, int... values) {
			super(values);
			this.name = name;
		}

		@Override
		public <T> T visit(EntryVisitor<T> visitor) {
			return visitor.visitInstanceEntry(this);
		}
		
		@Override
		public String toString() {
			if (name == null) {
				return "id:" + id;
			}
			else {
				return name;
			}
		}
		
		@Override
		public int compareTo(Entry o) {
			if (o instanceof InstanceEntry) {
				if (name == null) {
					return (int)((InstanceEntry) o).id - (int) id;
				}
				else {
					return name.compareTo(((InstanceEntry) o).name);
				}
			}
			else {
				return 1;
			}
		}
		
		public Object id() {
			if (name == null) {
				return id;
			}
			return name;
		}
	}
	
	public interface EntryVisitor<T> {
		public T visitPackageEntry(PackageEntry entry);
		public T visitClassEntry(ClassEntry entry);
		public T visitInstanceEntry(InstanceEntry entry);
	}
	
	public enum Type {
		NAME, COUNT, FLAG, OBJECT
	}
	
	public enum State {
		UNINITIALIZED, GENERATING, READY
	}
}
