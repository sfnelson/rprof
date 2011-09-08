package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.request.MethodService;

public class MethodManager implements MethodService {

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<? extends Method> findMethods(Clazz cls) {
		TypedQuery<Method> q = em.createNamedQuery("methodsForType", Method.class);
		q.setParameter("type", cls);
		return q.getResultList();
	}

}
