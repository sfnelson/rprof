package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.request.FieldService;

public class FieldManager extends DomainManager<Field> implements FieldService {

	public FieldManager(ContextManager cm) {
		super(cm, Field.class, FieldId.class);
	}

	@Override
	public List<? extends Field> findFields(Class cls) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Field> q = em.createNamedQuery("fieldsForType", Field.class);
		q.setParameter("type", cls);
		return q.getResultList();
	}

}
