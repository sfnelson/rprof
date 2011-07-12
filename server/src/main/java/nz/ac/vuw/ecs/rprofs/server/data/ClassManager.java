package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.request.ClassService;

public class ClassManager extends DomainManager<Class> implements ClassService {

	public ClassManager() {
		this(ContextManager.getInstance());
	}

	public ClassManager(ContextManager cm) {
		super(cm, Class.class, ClassId.TYPE);
	}

	@Override
	public Integer findNumPackages() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("numPackages", Number.class).getSingleResult().intValue();
	}

	@Override
	public List<String> findPackages() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("allPackages", String.class).getResultList();
	}

	@Override
	public Integer findNumClassesInPackage(String pkg) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Number> q = em.createNamedQuery("numClassesForPackage", Number.class);
		q.setParameter("package", pkg);
		return q.getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClassesInPackage(String pkg) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Class> q = em.createNamedQuery("classesForPackage", Class.class);
		q.setParameter("package", pkg);
		return q.getResultList();
	}

	@Override
	public Integer findNumClasses() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("numClasses", Number.class).getSingleResult().intValue();
	}

	@Override
	public List<? extends Class> findClasses() {
		EntityManager em = cm.getCurrent().em();

		return em.createNamedQuery("allClasses", Class.class).getResultList();
	}

	public Class findClass(String fqname) {
		EntityManager em = cm.getCurrent().em();

		TypedQuery<Class> q = em.createNamedQuery("findClassByName", Class.class);
		q.setParameter("name", fqname);
		List<Class> results = q.getResultList();
		if (results.isEmpty()) return null;
		else return results.get(0);
	}

}