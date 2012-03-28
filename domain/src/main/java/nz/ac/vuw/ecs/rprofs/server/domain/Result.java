package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ResultId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class Result implements DataObject<ResultId, Result> {

	public static final int TOTAL_EQUALS_COLL = 3;
	public static final int TOTAL_EQUALS = 2;
	public static final int TOTAL_COLL = 1;
	public static final int TOTAL_NONE = 0;

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

	public static int totalIndex(boolean inEquals, boolean inColl) {
		if (inEquals && inColl) return TOTAL_EQUALS_COLL;
		if (inEquals) return TOTAL_EQUALS;
		if (inColl) return TOTAL_COLL;
		else return TOTAL_NONE;
	}

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
	private ResultId id;

	@NotNull
	private Integer version;

	@Nullable
	private String className;

	@Nullable
	private String packageName;

	private int numObjects;

	@NotNull
	private int[] totals;

	@NotNull
	private int[] counts;

	@NotNull
	private Map<FieldId, FieldInfo> fields;

	public Result(ResultId id) {
		this.id = id;
		this.version = 0;

		numObjects = 0;
		totals = new int[4];
		counts = new int[32];

		fields = Maps.newHashMap();
	}

	public Result(ResultId id, String className, String packageName,
				  EventId lastWrite, EventId constructor, EventId firstRead,
				  EventId equals, EventId collection, Map<FieldId, Instance.FieldInfo> fields,
				  Set<FieldId> mutable) {
		this(id);

		this.className = className;
		this.packageName = packageName;

		numObjects = 1;

		boolean isConstructor = (lastWrite == null) || (constructor != null && lastWrite.before(constructor));
		boolean isCoarse = (firstRead == null) || (lastWrite == null) || (lastWrite.before(firstRead));
		boolean isFine = mutable.isEmpty();

		boolean inEquals = equals != null;
		boolean isEquals = (equals != null) && (lastWrite == null || lastWrite.before(equals));

		boolean inColl = collection != null;
		boolean isColl = (collection != null) && (lastWrite == null || lastWrite.before(collection));

		totals[totalIndex(inEquals, inColl)] = 1;
		counts[countIndex(isConstructor, isCoarse, isFine, isEquals, isColl)] = 1;

		for (FieldId field : fields.keySet()) {
			boolean isMutable = mutable.contains(field);
			Instance.FieldInfo info = fields.get(field);
			this.fields.put(field, new FieldInfo(field, isMutable ? 1 : 0, info.getReads(), info.getWrites()));
		}
	}

	public Result(ResultId id, String className, String packageName, int numObjects, int[] totals, int[] counts, Map<FieldId, FieldInfo> fields) {
		this.id = id;
		this.className = className;
		this.packageName = packageName;
		this.numObjects = numObjects;
		this.totals = totals;
		this.counts = counts;
		this.fields = fields;
	}

	@Override
	@NotNull
	public ResultId getId() {
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
	public int[] getTotals() {
		return totals;
	}

	public void setTotals(@NotNull int[] totals) {
		this.totals = totals;
	}

	@NotNull
	public int[] getCounts() {
		return counts;
	}

	public void setCounts(@NotNull int[] counts) {
		this.counts = counts;
	}

	@NotNull
	public Map<FieldId, FieldInfo> getFields() {
		return fields;
	}

	public void setFields(@NotNull Map<FieldId, FieldInfo> fields) {
		this.fields = fields;
	}

	public void append(@NotNull Result result) {
		assert (id.equals(result.getId()));

		if (result.className != null) this.className = result.className;
		if (result.packageName != null) this.packageName = result.packageName;

		numObjects += result.numObjects;

		for (int i = 0; i < totals.length; i++) {
			totals[i] += result.totals[i];
		}

		for (int i = 0; i < counts.length; i++) {
			counts[i] += result.counts[i];
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
			return id.equals(((Result) other).getId());
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode() ^ Result.class.hashCode();
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