package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;

import org.springframework.transaction.annotation.Transactional;

public class ClassManager implements ClassService {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Integer findNumPackages() {
		TypedQuery<Number> q = em.createNamedQuery("numPackages", Number.class);
		q.setParameter("dataset", owner());
		return q.getSingleResult().intValue();
	}

	@Override
	public List<String> findPackages() {
		TypedQuery<String> q = em.createNamedQuery("allPackages", String.class);
		q.setParameter("dataset", owner());
		return q.getResultList();
	}

	@Override
	public Integer findNumClassesInPackage(String pkg) {
		TypedQuery<Number> q = em.createNamedQuery("numClassesForPackage", Number.class);
		q.setParameter("dataset", owner());
		q.setParameter("package", pkg);
		return q.getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClassesInPackage(String pkg) {
		TypedQuery<Class> q = em.createNamedQuery("classesForPackage", Class.class);
		q.setParameter("dataset", owner());
		q.setParameter("package", pkg);
		return q.getResultList();
	}

	@Override
	public Integer findNumClasses() {
		TypedQuery<Number> q = em.createNamedQuery("numClasses", Number.class);
		q.setParameter("dataset", owner());
		return q.getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClasses() {
		TypedQuery<Class> q = em.createNamedQuery("allClasses", Class.class);
		q.setParameter("dataset", owner());
		return q.getResultList();
	}

	public Class findClass(String fqname) {
		TypedQuery<Class> q = em.createNamedQuery("findClassByName", Class.class);
		q.setParameter("dataset", owner());
		q.setParameter("name", fqname);
		List<Class> results = q.getResultList();
		if (results.isEmpty()) return null;
		else return results.get(0);
	}

	@Transactional
	public Class setParent(Class cls, Class parent) {
		cls = em.find(Class.class, cls.getId());
		parent = em.find(Class.class, parent.getId());
		cls.setParent(parent);
		return cls;
	}

	private Dataset owner() {
		return ContextManager.getThreadLocal();
	}
}