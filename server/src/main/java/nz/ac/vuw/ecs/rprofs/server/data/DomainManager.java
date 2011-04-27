package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.DataObject;
import nz.ac.vuw.ecs.rprofs.server.domain.id.Id;

import com.google.gwt.requestfactory.shared.Locator;

public abstract class DomainManager<T extends DataObject<T>> extends Locator<T, Long> {

	protected final ContextManager cm = ContextManager.getInstance();

	private final java.lang.Class<T> type;
	private final java.lang.Class<? extends Id<T>> idType;

	protected DomainManager(java.lang.Class<T> type, java.lang.Class<? extends Id<T>> idType) {
		this.type = type;
		this.idType = idType;
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

	public T find(Id<T> id) {
		return cm.getCurrent().find(type, id);
	}

	@Override
	public T find(java.lang.Class<? extends T> type, Long id) {
		return cm.getCurrent().find(type, createId(id));
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
