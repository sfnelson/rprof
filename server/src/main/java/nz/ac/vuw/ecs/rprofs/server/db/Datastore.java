package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class Datastore {

	@PersistenceContext
	public EntityManager em;

	Datastore() {}

	public List<Dataset> getDatasets() {
		return em.createNamedQuery("findDatasets", Dataset.class).getResultList();
	}

	public Dataset updateDataset(Dataset dataset) {
		return em.merge(dataset);
	}

	public void deleteDataset(Dataset dataset) {
		dataset = em.find(Dataset.class, dataset.getId());
		em.remove(dataset);
	}

	public void deleteDatastore(Dataset dataset) {
		String handle = dataset.getDatasetId().getId();
		em.createNativeQuery("drop table if exists run_" + handle + "_args cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_classes cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_events cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_fields cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_instances cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_methods cascade;").executeUpdate();
		em.createNativeQuery("drop table if exists run_" + handle + "_profiler_runs cascade;").executeUpdate();
		em.flush();
	}

	public void close() {
		em.close();
	}

	public <T> T findRecord(Class<T> type, Object primaryKey) {
		return em.find(type, primaryKey);
	}

	public <T> List<? extends T> findRecords(Class<T> type) {
		return em.createQuery("select R from " + type.getSimpleName() + " R", type).getResultList();
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

	public <T> void updateRecord(T record) {
		em.merge(record);
	}

	public <T> void updateRecords(Iterable<? extends T> records) {
		for (T record: records) {
			em.merge(record);
		}
	}
}