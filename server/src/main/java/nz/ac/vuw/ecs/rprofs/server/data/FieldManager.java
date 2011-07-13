package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;

public class FieldManager implements FieldService {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<? extends Field> findFields(Class cls) {
		TypedQuery<Field> q = em.createNamedQuery("fieldsForType", Field.class);
		q.setParameter("owner", ContextManager.getThreadLocal());
		q.setParameter("type", cls);
		return q.getResultList();
	}

}
