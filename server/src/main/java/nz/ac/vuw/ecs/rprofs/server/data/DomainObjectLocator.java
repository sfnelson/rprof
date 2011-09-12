package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class DomainObjectLocator<I extends Id<I, T>, T extends DataObject<I, T>> extends Locator<T, Long> {

	private org.slf4j.Logger log = LoggerFactory.getLogger(DomainObjectLocator.class);

	@Override
	public T create(java.lang.Class<? extends T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			log.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			log.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public T find(java.lang.Class<? extends T> clazz, Long id) {
		if (Clazz.class.equals(clazz)) {
			return null;
		}
		if (Dataset.class.equals(clazz)) {
			return null;
		}
		if (Event.class.equals(clazz)) {
			return null;
		}
		if (Field.class.equals(clazz)) {
			return null;
		}
		if (Instance.class.equals(clazz)) {
			return null;
		}
		if (Method.class.equals(clazz)) {
			return null;
		}
		log.warn("could not find locator case for {}", clazz.getName());
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public java.lang.Class getDomainType() {
		return DataObject.class;
	}

	@Override
	public Long getId(T object) {
		return object.getId().longValue();
	}

	@Override
	public java.lang.Class<Long> getIdType() {
		return Long.TYPE;
	}

	@Override
	public Object getVersion(T o) {
		return 1;
	}

}
