package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Datastore {

	private static Datastore main;

	static {
		NamingStrategy.currentRun.set(null);

		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		main = c.getBean(Datastore.class);
	}

	public static Datastore getInstance() {
		return main;
	}

	@PersistenceContext
	public EntityManager em;

	Datastore() {}

	public Dataset updateDataset(Dataset dataset) {
		return em.merge(dataset);
	}

	public void deleteDataset(Dataset dataset) {
		dataset = em.find(Dataset.class, dataset.getId());
		em.remove(dataset);
	}

	public void deleteDatastore(Dataset dataset) {
		String handle = dataset.getHandle();
		em.createNativeQuery("drop table if exists run_" + handle + "_classes cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_events cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_events_args cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_fields cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_instances cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_methods cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_profiler_runs cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_field_writes cascade;").executeUpdate();
		em.flush();
	}

	public void close() {
		em.close();
	}

	public <T> T findRecord(Class<T> type, Object primaryKey) {
		return em.find(type, primaryKey);
	}

	public int findNumRecords(Class<?> type) {
		return ((Number) em.createQuery("select count(R) from " + type.getName() + " R").getSingleResult()).intValue();
	}

	public <T> List<? extends T> findRecords(Class<T> type) {
		return em.createQuery("select R from " + type.getName() + " R", type).getResultList();
	}

	public <T> T storeRecord(T record) {
		return em.merge(record);
	}

	public <T> List<? extends T> storeRecords(Iterable<? extends T> records) {
		List<T> result = Collections.newList();
		for (T r: records) {
			result.add(em.merge(r));
		}
		return result;
	}

	public <T> void refreshRecord(T record) {
		em.refresh(record);
	}

	public <T> T updateRecord(T record) {
		return em.merge(record);
	}

	public <T> void updateRecords(Iterable<? extends T> records) {
		for (T record: records) {
			em.merge(record);
		}
	}

	public List<Instance> findInstancesByType(nz.ac.vuw.ecs.rprofs.server.domain.Class cls) {
		TypedQuery<Instance> q = em.createQuery("select I from Instance I where I.type = :cls", Instance.class);
		q.setParameter("cls", cls);
		return q.getResultList();
	}

	public Long findNumEvents(int filter) {
		TypedQuery<Number> q = em.createQuery("select count(E) from Event E where band(E.event, :filter) = E.event", Number.class);
		q.setParameter("filter", filter);
		return q.getSingleResult().longValue();
	}

	public Long findEventIndex(EventId event, int filter) {
		TypedQuery<Number> q = em.createQuery("select count(E) from Event E where "
				+ "band(E.event, :filter) = E.event "
				+ "and E.id.id <= :id", Number.class);
		q.setParameter("filter", filter);
		q.setParameter("id", event.getId());
		return q.getSingleResult().longValue();
	}

	public List<Event> findEvents(int start, int limit, int filter) {
		TypedQuery<Event> q = em.createQuery("select E from Event E where band(E.event, :filter) = E.event", Event.class);
		q.setFirstResult(start);
		q.setMaxResults(limit);
		q.setParameter("filter", filter);
		return q.getResultList();
	}

	public Dataset findDatasetByHandle(String handle) {
		TypedQuery<Dataset> q = em.createQuery("select D from Dataset D where D.handle = :handle", Dataset.class);
		q.setParameter("handle", handle);
		return q.getSingleResult();
	}

	public List<Long> findObjectsPerClass() {
		CriteriaBuilder builder = em.getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.TYPE);
		Root<Instance> instance = query.from(Instance.class);
		Path<nz.ac.vuw.ecs.rprofs.server.domain.Class> type = instance.get("type");

		query.select(builder.count(instance));
		query.where(builder.isNotNull(type));
		query.groupBy(type);

		return em.createQuery(query).getResultList();
	}

	public List<? extends Event> findEventsWithArgument(Instance i) {
		TypedQuery<Event> q = em.createQuery("select E from Event as E inner join E.args as Args where Args.parameter = :instance", Event.class);
		q.setParameter("instance", i);
		return q.getResultList();
	}
}