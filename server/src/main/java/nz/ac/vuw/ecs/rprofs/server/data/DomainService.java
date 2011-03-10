package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.db.Datastore;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.DataObject;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.Id;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;

import com.google.gwt.requestfactory.shared.Locator;

public abstract class DomainService<T extends DataObject<T>> extends Locator<T, Long> {

	public static class ClassLocator extends DomainService<Class> {
		public ClassLocator() { super(Class.class, ClassId.class); }
	}

	public static class MethodLocator extends DomainService<Method> {
		public MethodLocator() { super(Method.class, MethodId.class); }
	}

	public static class FieldLocator extends DomainService<Field> {
		public FieldLocator() { super(Field.class, FieldId.class); }
	}

	public static class EventLocator extends DomainService<Event> {
		public EventLocator() { super(Event.class, EventId.class); }
	}

	public static class InstanceLocator extends DomainService<Instance> {
		public InstanceLocator() { super(Instance.class, ObjectId.class); }
	}

	private final java.lang.Class<T> type;

	private final java.lang.Class<? extends Id<T>> idType;

	protected DomainService(java.lang.Class<T> type, java.lang.Class<? extends Id<T>> idType) {
		this.type = type;
		this.idType = idType;
	}

	private Datastore getStore(Long param) {
		short id = (short) (param.longValue() >>> 48);
		return ContextManager.getInstance().getContext(id).db;
	}

	@Override
	public T create(java.lang.Class<? extends T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Id<T> createId(Long id) {
		Id<T> nid;
		try {
			nid = idType.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		nid.setId(id);
		return nid;
	}

	@Override
	public T find(java.lang.Class<? extends T> type, Long id) {
		return getStore(id).findRecord(type, createId(id));
	}

	@Override
	public java.lang.Class<T> getDomainType() {
		return type;
	}

	@Override
	public Long getId(T domainObject) {
		return domainObject.getId().getId();
	}

	@Override
	public java.lang.Class<Long> getIdType() {
		return Long.class;
	}

	@Override
	public Integer getVersion(T domainObject) {
		return domainObject.getVersion();
	}

	protected Context context(String handle) {
		return ContextManager.getInstance().getContext(handle);
	}
}
