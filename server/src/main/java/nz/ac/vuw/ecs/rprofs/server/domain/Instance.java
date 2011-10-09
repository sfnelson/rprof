/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import com.google.common.collect.Maps;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

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

	@NotNull
	private Map<FieldId, FieldInfo> fields = Maps.newHashMap();

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

	public static class FieldInfo {

		@NotNull
		private FieldId id;

		@NotNull
		private String name;

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

		public FieldInfo() {
		}

		public FieldInfo(FieldId id, String name) {
			this.id = id;
			this.name = name;
		}

		public FieldInfo(Field field) {
			this.id = field.getId();
			this.name = field.getName();
		}

		public FieldId getId() {
			return id;
		}

		public void setId(FieldId id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getReads() {
			return reads;
		}

		public void setReads(int reads) {
			this.reads = reads;
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
