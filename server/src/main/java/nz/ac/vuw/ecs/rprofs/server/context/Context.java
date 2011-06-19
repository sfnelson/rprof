package nz.ac.vuw.ecs.rprofs.server.context;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public interface Context {

	public void open();

	public boolean isOpen();

	public EntityManager em();
	public EntityTransaction tx();

	public <T> T find(Class<T> clazz, Object id);

	public void close();
	public void clear();
}
