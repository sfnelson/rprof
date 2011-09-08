package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;

import org.springframework.transaction.annotation.Transactional;

public class InstanceManager implements InstanceService {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Instance findInstance(Long id) {
		if (id == null || id == 0) {
			return null;
		}
		else {
			return em.find(Instance.class, new ObjectId(id));
		}
	}

	@Override
	public List<? extends Instance> findInstancesForClass(Class cls) {
		TypedQuery<Instance> q = em.createNamedQuery("instancesForType", Instance.class);
		q.setParameter("dataset", owner());
		q.setParameter("type", cls);
		return q.getResultList();
	}

	@Override
	public int findNumInstancesForClass(Class cls) {
		TypedQuery<Number> q = em.createNamedQuery("numInstancesForType", Number.class);
		q.setParameter("dataset", owner());
		q.setParameter("type", cls);
		return q.getSingleResult().intValue();
	}

	@Override
	public int findNumInstances() {
		TypedQuery<Number> q = em.createNamedQuery("numInstances", Number.class);
		q.setParameter("dataset", owner());
		return q.getSingleResult().intValue();
	}

	@Override
	public List<? extends Instance> findInstances() {
		TypedQuery<Instance> q = em.createNamedQuery("allInstances", Instance.class);
		q.setParameter("dataset", owner());
		return q.getResultList();
	}

	@Transactional
	public Instance createInstance(ObjectId id) {
		Instance instance = new Instance(owner(), id, null, null);
		em.persist(instance);
		return instance;
	}

	@Transactional
	public Instance createInstance(ObjectId id, Class type, Method constructor) {
		Instance i = createInstance(id);
		return updateInstance(i);
	}

	@Transactional
	public Instance updateInstance(Instance instance) {
		Instance i = em.find(Instance.class, instance.getId());
		i.setConstructor(instance.getConstructor());
		i.setType(instance.getType());
		return i;
	}

	private DataSet owner() {
		return ContextManager.getThreadLocal();
	}

}
