package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;

public class MethodManager extends DomainManager<Method> implements MethodService {

	public MethodManager(ContextManager cm) {
		super(cm, Method.class, MethodId.class);
	}

	@Override
	public List<? extends Method> findMethods(Class cls) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Method> q = em.createNamedQuery("methodsForType", Method.class);
		q.setParameter("type", cls);
		return q.getResultList();
	}

}
