/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.Map;

import com.google.common.collect.Maps;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 */
public class Instance implements DataObject<InstanceId, Instance> {

	@NotNull
	private InstanceId id;

	@NotNull
	private Integer version;

	@Nullable
	private ClazzId type;

	@Nullable
	private MethodId constructor;

	@Nullable
	private EventId constructorReturn;

	@Nullable
	private EventId firstEquals;

	@Nullable
	private EventId firstHashCode;

	@Nullable
	private EventId firstCollection;

	@NotNull
	private Map<FieldId, FieldInfo> fields = Maps.newHashMap();

	/**
	 * This field is not persisted in the database
	 */
	@Nullable
	private EventId inEquals;

	/**
	 * This field is not persisted in the database
	 */
	@Nullable
	private EventId inHashCode;

	public Instance() {
	}

	public Instance(@NotNull InstanceId id) {
		this.id = id;
	}

	@NotNull
	@Override
	public InstanceId getId() {
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
	public ClazzId getType() {
		return type;
	}

	public void setType(@Nullable ClazzId type) {
		this.type = type;
	}

	@Nullable
	public MethodId getConstructor() {
		return constructor;
	}

	public void setConstructor(@Nullable MethodId m) {
		this.constructor = m;
	}

	@Nullable
	public EventId getConstructorReturn() {
		return constructorReturn;
	}

	public void setConstructorReturn(@Nullable EventId constructorReturn) {
		this.constructorReturn = constructorReturn;
	}

	@Nullable
	public EventId getFirstEquals() {
		return firstEquals;
	}

	public void setFirstEquals(@Nullable EventId firstEquals) {
		this.firstEquals = firstEquals;
	}

	@Nullable
	public EventId getFirstHashCode() {
		return firstHashCode;
	}

	public void setFirstHashCode(@Nullable EventId firstHashCode) {
		this.firstHashCode = firstHashCode;
	}

	@Nullable
	public EventId getFirstCollection() {
		return firstCollection;
	}

	public void setFirstCollection(@Nullable EventId firstCollection) {
		this.firstCollection = firstCollection;
	}

	public boolean hasFieldInfo(FieldId fieldId) {
		return fields.containsKey(fieldId);
	}

	@Nullable
	public FieldInfo getFieldInfo(FieldId fieldId) {
		return fields.get(fieldId);
	}

	public void addFieldInfo(@NotNull FieldId fieldId, @NotNull FieldInfo fieldInfo) {
		fields.put(fieldId, fieldInfo);
	}

	@NotNull
	public Map<FieldId, FieldInfo> getFields() {
		return fields;
	}

	@Nullable
	public EventId getInEquals() {
		return inEquals;
	}

	@Nullable
	public void setInEquals(EventId inEquals) {
		this.inEquals = inEquals;
	}

	@Nullable
	public EventId getInHashCode() {
		return inHashCode;
	}

	@Nullable
	public void setInHashCode(EventId inHashCode) {
		this.inHashCode = inHashCode;
	}

	public boolean inEqualsOrHashCode() {
		return inEquals != null || inHashCode != null;
	}

	public static class FieldInfo {

		@NotNull
		private FieldId id;

		private int reads = 0;

		@Nullable
		private EventId firstRead;

		@Nullable
		private EventId lastRead;

		private int writes = 0;

		@Nullable
		private EventId firstWrite;

		@Nullable
		private EventId lastWrite;

		@Nullable
		private EventId firstEqualsRead;

		@Nullable
		private EventId lastEqualsRead;

		public FieldInfo() {
		}

		public FieldInfo(FieldId id) {
			this.id = id;
		}

		public FieldId getId() {
			return id;
		}

		public void setId(FieldId id) {
			this.id = id;
		}

		public int getReads() {
			return reads;
		}

		public void setReads(int reads) {
			this.reads = reads;
		}

		public void addReads(int reads) {
			this.reads += reads;
		}

		@Nullable
		public EventId getFirstRead() {
			return firstRead;
		}

		public void setFirstRead(@Nullable EventId firstRead) {
			this.firstRead = firstRead;
		}

		@Nullable
		public EventId getLastRead() {
			return lastRead;
		}

		public void setLastRead(@Nullable EventId lastRead) {
			this.lastRead = lastRead;
		}

		public int getWrites() {
			return writes;
		}

		public void setWrites(int writes) {
			this.writes = writes;
		}

		public void addWrites(int writes) {
			this.writes += writes;
		}

		@Nullable
		public EventId getFirstWrite() {
			return firstWrite;
		}

		public void setFirstWrite(@Nullable EventId firstWrite) {
			this.firstWrite = firstWrite;
		}

		@Nullable
		public EventId getLastWrite() {
			return lastWrite;
		}

		public void setLastWrite(@Nullable EventId lastWrite) {
			this.lastWrite = lastWrite;
		}

		@Nullable
		public EventId getFirstEqualsRead() {
			return firstEqualsRead;
		}

		public void setFirstEqualsRead(@Nullable EventId firstEqualsRead) {
			this.firstEqualsRead = firstEqualsRead;
		}

		@Nullable
		public EventId getLastEqualsRead() {
			return lastEqualsRead;
		}

		public void setLastEqualsRead(@Nullable EventId lastEqualsRead) {
			this.lastEqualsRead = lastEqualsRead;
		}

		@Override
		public int hashCode() {
			return id.hashCode() ^ FieldInfo.class.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			FieldInfo fieldInfo = (FieldInfo) o;

			return id.equals(fieldInfo.id);
		}
	}
}
