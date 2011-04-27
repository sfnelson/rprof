package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;

public class ClassManager extends DomainManager<Class> implements ClassService {

	public ClassManager() {
		super(Class.class, ClassId.TYPE);
	}

	@Override
	public int findNumPackages() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("numPackages", Number.class).getSingleResult().intValue();
	}

	@Override
	public List<String> findPackages() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("allPackages", String.class).getResultList();
	}

	@Override
	public int findNumClasses(String pkg) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numClassesForPackage", Number.class);
		q.setParameter("package", pkg);
		return q.getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClasses(String pkg) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Class> q = em.createNamedQuery("classesForPackage", Class.class);
		q.setParameter("package", pkg);
		return q.getResultList();
	}

	@Override
	public int findNumClasses() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("numClasses", Number.class).getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClasses() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("allClasses", Class.class).getResultList();
	}

}