package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.web.bindery.requestfactory.shared.Locator;

@Configurable
public class DomainObjectLocator<T> extends Locator<T, Long> {

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
		if (clazz == Argument.class) {
			return null;
		}
		if (clazz == Clazz.class) {
			return null;
		}
		if (clazz == Dataset.class) {
			return null;
		}
		if (clazz == Event.class) {
			return null;
		}
		if (clazz == Field.class) {
			return null;
		}
		if (clazz == Instance.class) {
			return null;
		}
		if (clazz == Method.class) {
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
	public Long getId(T o) {
		return ((DataObject<?, ?>) o).getRpcId();
	}

	@Override
	public java.lang.Class<Long> getIdType() {
		return Long.TYPE;
	}

	@Override
	public Object getVersion(T o) {
		return ((DataObject<?, ?>) o).getVersion();
	}

}
