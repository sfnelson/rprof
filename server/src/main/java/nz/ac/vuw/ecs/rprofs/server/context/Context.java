package nz.ac.vuw.ecs.rprofs.server.context;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

public class Context {

	@PersistenceUnit
	private EntityManagerFactory emf;

	private ContextManager cm = ContextManager.getInstance();

	private ThreadLocal<EntityManager> em = new ThreadLocal<EntityManager>();

	public void open() {
		em.set(emf.createEntityManager());
	}

	public boolean isOpen() {
		return em.get() == null;
	}

	public EntityManager em() {
		return em.get();
	}

	public EntityTransaction tx() {
		return em.get().getTransaction();
	}

	public <T> T find(Class<T> clazz, Object id) {
		return em.get().find(clazz, id);
	}

	public void close() {
		em.get().close();
		em.remove();

		if (cm.getDefault() != this && cm.getDefault().isOpen()) {
			cm.getDefault().close();
		}
	}

	public void clear() {
		em.get().clear();
		em.remove();
	}
}
