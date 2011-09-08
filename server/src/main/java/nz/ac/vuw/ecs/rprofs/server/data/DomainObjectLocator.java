package nz.ac.vuw.ecs.rprofs.server.data;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.web.bindery.requestfactory.shared.Locator;

@Configurable
public class DomainObjectLocator<T> extends Locator<T, Long> {

	private org.slf4j.Logger log = LoggerFactory.getLogger(DomainObjectLocator.class);

	@PersistenceContext
	private EntityManager em;

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
			return em.find(clazz, id);
		}
		if (clazz == nz.ac.vuw.ecs.rprofs.server.domain.Class.class) {
			return em.find(clazz, new ClassId(id));
		}
		if (clazz == DataSet.class) {
			return em.find(clazz, id.shortValue());
		}
		if (clazz == Event.class) {
			return em.find(clazz, new EventId(id));
		}
		if (clazz == Field.class) {
			return em.find(clazz, new FieldId(id));
		}
		if (clazz == Instance.class) {
			return em.find(clazz, new ObjectId(id));
		}
		if (clazz == Method.class) {
			return em.find(clazz, new MethodId(id));
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
