package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassSummaryId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class ClassSummary implements DataObject<ClassSummaryId, ClassSummary> {

	public static final int CONSTRUCTOR_COARSE_FINE_EQUALS_COLL = 31;
	public static final int CONSTRUCTOR_COARSE_FINE_EQUALS = 30;
	public static final int CONSTRUCTOR_COARSE_FINE_COLL = 29;
	public static final int CONSTRUCTOR_COARSE_FINE = 28;
	public static final int CONSTRUCTOR_COARSE_EQUALS_COLL = 27;
	public static final int CONSTRUCTOR_COARSE_EQUALS = 26;
	public static final int CONSTRUCTOR_COARSE_COLL = 25;
	public static final int CONSTRUCTOR_COARSE = 24;
	public static final int CONSTRUCTOR_FINE_EQUALS_COLL = 23;
	public static final int CONSTRUCTOR_FINE_EQUALS = 22;
	public static final int CONSTRUCTOR_FINE_COLL = 21;
	public static final int CONSTRUCTOR_FINE = 20;
	public static final int CONSTRUCTOR_EQUALS_COLL = 19;
	public static final int CONSTRUCTOR_EQUALS = 18;
	public static final int CONSTRUCTOR_COLL = 17;
	public static final int CONSTRUCTOR = 16;
	public static final int COARSE_FINE_EQUALS_COLL = 15;
	public static final int COARSE_FINE_EQUALS = 14;
	public static final int COARSE_FINE_COLL = 13;
	public static final int COARSE_FINE = 12;
	public static final int COARSE_EQUALS_COLL = 11;
	public static final int COARSE_EQUALS = 10;
	public static final int COARSE_COLL = 9;
	public static final int COARSE = 8;
	public static final int FINE_EQUALS_COLL = 7;
	public static final int FINE_EQUALS = 6;
	public static final int FINE_COLL = 5;
	public static final int FINE = 4;
	public static final int EQUALS_COLL = 3;
	public static final int EQUALS = 2;
	public static final int COLL = 1;
	public static final int NONE = 0;

	public static int countIndex(boolean isConstructor, boolean isCoarse, boolean isFine, boolean isEquals, boolean isColl) {
		if (isConstructor && isCoarse && isFine && isEquals && isColl) return CONSTRUCTOR_COARSE_FINE_EQUALS_COLL;
		else if (isConstructor && isCoarse && isFine && isEquals) return CONSTRUCTOR_COARSE_FINE_EQUALS;
		else if (isConstructor && isCoarse && isFine && isColl) return CONSTRUCTOR_COARSE_FINE_COLL;
		else if (isConstructor && isCoarse && isFine) return CONSTRUCTOR_COARSE_FINE;
		else if (isConstructor && isCoarse && isEquals && isColl) return CONSTRUCTOR_COARSE_EQUALS_COLL;
		else if (isConstructor && isCoarse && isEquals) return CONSTRUCTOR_COARSE_EQUALS;
		else if (isConstructor && isCoarse && isColl) return CONSTRUCTOR_COARSE_COLL;
		else if (isConstructor && isCoarse) return CONSTRUCTOR_COARSE;
		else if (isConstructor && isFine && isEquals && isColl) return CONSTRUCTOR_FINE_EQUALS_COLL;
		else if (isConstructor && isFine && isEquals) return CONSTRUCTOR_FINE_EQUALS;
		else if (isConstructor && isFine && isColl) return CONSTRUCTOR_FINE_COLL;
		else if (isConstructor && isFine) return CONSTRUCTOR_FINE;
		else if (isConstructor && isEquals && isColl) return CONSTRUCTOR_EQUALS_COLL;
		else if (isConstructor && isEquals) return CONSTRUCTOR_EQUALS;
		else if (isConstructor && isColl) return CONSTRUCTOR_COLL;
		else if (isConstructor) return CONSTRUCTOR;
		else if (isCoarse && isFine && isEquals && isColl) return COARSE_FINE_EQUALS_COLL;
		else if (isCoarse && isFine && isEquals) return COARSE_FINE_EQUALS;
		else if (isCoarse && isFine && isColl) return COARSE_FINE_COLL;
		else if (isCoarse && isFine) return COARSE_FINE;
		else if (isCoarse && isEquals && isColl) return COARSE_EQUALS_COLL;
		else if (isCoarse && isEquals) return COARSE_EQUALS;
		else if (isCoarse && isColl) return COARSE_COLL;
		else if (isCoarse) return COARSE;
		else if (isFine && isEquals && isColl) return FINE_EQUALS_COLL;
		else if (isFine && isEquals) return FINE_EQUALS;
		else if (isFine && isColl) return FINE_COLL;
		else if (isFine) return FINE;
		else if (isEquals && isColl) return EQUALS_COLL;
		else if (isEquals) return EQUALS;
		else if (isColl) return COLL;
		else return NONE;
	}

	@NotNull
	private ClassSummaryId id;

	@NotNull
	private Integer version;

	@Nullable
	private String className;

	@Nullable
	private String packageName;

	private int numObjects;

	@NotNull
	private int[] eqcol;

	@NotNull
	private int[] eq;

	@NotNull
	private int[] col;

	@NotNull
	private int[] none;

	@NotNull
	private Map<FieldId, FieldInfo> fields;

	public ClassSummary(ClassSummaryId id) {
		this.id = id;
		this.version = 0;

		numObjects = 0;
		eqcol = new int[32];
		col = new int[32];
		eq = new int[32];
		none = new int[32];

		fields = Maps.newHashMap();
	}

	public ClassSummary(ClassSummaryId id,
						EventId lastWrite, EventId constructor, EventId firstRead,
						EventId equals, EventId collection, Map<FieldId, Instance.FieldInfo> fields,
						Set<FieldId> mutable) {
		this(id);

		numObjects = 1;

		boolean isConstructor = (lastWrite == null) || (constructor != null && lastWrite.before(constructor));
		boolean isCoarse = (firstRead == null) || (lastWrite == null) || (lastWrite.before(firstRead));
		boolean isFine = mutable.isEmpty();

		boolean inEquals = equals != null;
		boolean isEquals = (equals != null) && (lastWrite == null || lastWrite.before(equals));

		boolean inColl = collection != null;
		boolean isColl = (collection != null) && (lastWrite == null || lastWrite.before(collection));

		int index = countIndex(isConstructor, isCoarse, isFine, isEquals, isColl);
		if (inEquals && inColl) eqcol[index] = 1;
		else if (inEquals) eq[index] = 1;
		else if (inColl) col[index] = 1;
		else none[index] = 1;

		for (FieldId field : fields.keySet()) {
			boolean isMutable = mutable.contains(field);
			Instance.FieldInfo info = fields.get(field);
			this.fields.put(field, new FieldInfo(field, isMutable ? 1 : 0, info.getReads(), info.getWrites()));
		}
	}

	public ClassSummary(ClassSummaryId id, int numObjects,
						int[] eqcol, int[] eq, int[] col, int[] none, Map<FieldId, FieldInfo> fields) {
		this.id = id;
		this.numObjects = numObjects;
		this.eqcol = eqcol;
		this.eq = eq;
		this.col = col;
		this.none = none;
		this.fields = fields;
	}

	@Override
	@NotNull
	public ClassSummaryId getId() {
		return id;
	}

	@NotNull
	public Integer getVersion() {
		return version;
	}

	public void setVersion(@NotNull Integer version) {
		this.version = version;
	}

	@Nullable
	public String getClassName() {
		return className;
	}

	public void setClassName(@Nullable String className) {
		this.className = className;
	}

	@Nullable
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(@Nullable String packageName) {
		this.packageName = packageName;
	}

	public int getNumObjects() {
		return numObjects;
	}

	public void setNumObjects(int numObjects) {
		this.numObjects = numObjects;
	}

	@NotNull
	public int[] getEqCol() {
		return eqcol;
	}

	public void setEqCol(@NotNull int[] eqcol) {
		this.eqcol = eqcol;
	}

	@NotNull
	public int[] getEq() {
		return eq;
	}

	public void setEq(@NotNull int[] eq) {
		this.eq = eq;
	}

	@NotNull
	public int[] getCol() {
		return col;
	}

	public void setCol(@NotNull int[] col) {
		this.col = col;
	}

	@NotNull
	public int[] getNone() {
		return none;
	}

	public void setNone(@NotNull int[] none) {
		this.none = none;
	}

	@NotNull
	public Map<FieldId, FieldInfo> getFields() {
		return fields;
	}

	public void setFields(@NotNull Map<FieldId, FieldInfo> fields) {
		this.fields = fields;
	}

	public void append(@NotNull ClassSummary result) {
		assert (id.equals(result.getId()));

		numObjects += result.numObjects;

		for (int i = 0; i < eqcol.length; i++) {
			eqcol[i] += result.eqcol[i];
		}

		for (int i = 0; i < eq.length; i++) {
			eq[i] += result.eq[i];
		}

		for (int i = 0; i < col.length; i++) {
			col[i] += result.col[i];
		}

		for (int i = 0; i < none.length; i++) {
			none[i] += result.none[i];
		}

		for (FieldId field : result.fields.keySet()) {
			FieldInfo info = fields.get(field);
			if (info == null) {
				info = new FieldInfo(field, 0, 0, 0);
				fields.put(field, info);
			}
			info.append(result.fields.get(field));
		}
	}

	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (getClass() == other.getClass()) {
			return id.equals(((ClassSummary) other).getId());
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode() ^ ClassSummary.class.hashCode();
	}

	public static class FieldInfo {
		private FieldId id;
		private int mutable;
		private int reads;
		private int writes;

		public FieldInfo(FieldId id, int mutable, int reads, int writes) {
			this.id = id;
			this.mutable = mutable;
			this.reads = reads;
			this.writes = writes;
		}

		public FieldId getId() {
			return id;
		}

		public int getMutable() {
			return mutable;
		}

		public int getReads() {
			return reads;
		}

		public int getWrites() {
			return writes;
		}

		public void append(FieldInfo info) {
			assert (id.equals(info.getId()));

			mutable += info.mutable;
			reads += info.reads;
			writes += info.writes;
		}

		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null) return false;
			if (getClass() == o.getClass()) {
				return id.equals(((FieldInfo) o).id);
			}
			return false;
		}

		public int hashCode() {
			return id.hashCode() ^ FieldInfo.class.hashCode();
		}
	}
}