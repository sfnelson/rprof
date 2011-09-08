package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
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
	public List<? extends Clazz> findClassesInPackage(String pkg) {
		TypedQuery<Clazz> q = em.createNamedQuery("classesForPackage", Clazz.class);
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
	public List<? extends Clazz> findClasses() {
		TypedQuery<Clazz> q = em.createNamedQuery("allClasses", Clazz.class);
		q.setParameter("dataset", owner());
		return q.getResultList();
	}

	public Clazz findClass(String fqname) {
		TypedQuery<Clazz> q = em.createNamedQuery("findClassByName", Clazz.class);
		q.setParameter("dataset", owner());
		q.setParameter("name", fqname);
		List<Clazz> results = q.getResultList();
		if (results.isEmpty()) return null;
		else return results.get(0);
	}

	@Transactional
	public Clazz setParent(Clazz cls, Clazz parent) {
		cls = em.find(Clazz.class, cls.getId());
		parent = em.find(Clazz.class, parent.getId());
		cls.setParent(parent);
		return cls;
	}

	private DataSet owner() {
		return ContextManager.getThreadLocal();
	}
}