package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

public class PersistenceContext implements Context {

	private final Logger log = Logger.getLogger("context");

	@PersistenceUnit
	private EntityManagerFactory emf;

	private ContextManager cm = ContextManager.getInstance();

	private ThreadLocal<EntityManager> em = new ThreadLocal<EntityManager>();

	public void open() {
		log.info(String.format("%d: open", System.currentTimeMillis()));
		em.set(emf.createEntityManager());
		em.get().getTransaction().begin();
	}

	public boolean isOpen() {
		return em.get() != null;
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
		em.get().getTransaction().commit();
		em.get().close();
		em.remove();
		log.info(String.format("%d: close", System.currentTimeMillis()));

		if (cm.getDefault() != this && cm.getDefault().isOpen()) {
			cm.getDefault().close();
		}
	}

	public void clear() {
		em.get().clear();
		em.remove();
	}
}
