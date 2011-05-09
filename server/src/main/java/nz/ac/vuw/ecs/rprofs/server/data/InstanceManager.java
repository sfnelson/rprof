package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.InstanceService;

public class InstanceManager extends DomainManager<Instance> implements InstanceService {

	public InstanceManager() {
		super(Instance.TYPE, ObjectId.class);
	}

	@Override
	public Instance findInstance(Long id) {
		if (id == null || id == 0) {
			return null;
		}
		else {
			return find(new ObjectId(id));
		}
	}

	@Override
	public List<? extends Instance> findInstancesForClass(Class cls) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Instance> q = em.createNamedQuery("instancesForType", Instance.class);
		q.setParameter("type", cls);
		return q.getResultList();
	}

	@Override
	public int findNumInstancesForClass(Class cls) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numInstancesForType", Number.class);
		q.setParameter("type", cls);
		return q.getSingleResult().intValue();
	}

	@Override
	public int findNumInstances() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("numInstances", Number.class).getSingleResult().intValue();
	}

	@Override
	public List<? extends Instance> findInstances() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("allInstances", Instance.class).getResultList();
	}

	public Instance createInstance(ObjectId id) {
		EntityManager em = cm.getCurrent().em();

		Instance instance = new Instance(id, null, null);
		em.persist(instance);
		return instance;
	}

}
