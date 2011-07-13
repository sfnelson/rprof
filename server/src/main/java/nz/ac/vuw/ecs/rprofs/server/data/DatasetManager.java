package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DatasetManager implements DatasetService {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ContextManager cm;

	@Override
	public List<Dataset> findAllDatasets() {
		return em.createNamedQuery("allDatasets", Dataset.class).getResultList();
	}

	@Override
	public Dataset findDataset(String handle) {
		TypedQuery<Dataset> q = em.createNamedQuery("findDataset", Dataset.class);
		q.setParameter("handle", handle);
		List<Dataset> ds = q.getResultList();
		if (ds.isEmpty()) return null;
		else return ds.get(0);
	}

	@Override
	@Transactional
	public void stopDataset(String dataset) {
		Dataset ds = findDataset(dataset);

		ds.setStopped(Calendar.getInstance().getTime());

		cm.stopRecording(ds);
	}

	@Transactional
	public Dataset setStopped(Dataset dataset, Date stopped) {
		Dataset ds = em.find(Dataset.class, dataset.getId());
		ds.setStopped(dataset.getStopped());
		return ds;
	}

	@Transactional
	public Dataset setProgram(Dataset dataset, String program) {
		Dataset ds = em.find(Dataset.class, dataset.getId());
		ds.setProgram(program);
		return ds;
	}

	@Override
	@Transactional
	public void deleteDataset(String handle) {
		Dataset ds = findDataset(handle);

		if (ds == null) return;

		em.createNamedQuery("deleteArguments").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteEvents").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteInstances").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteFields").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteMethods").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteClasses").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteDataset").setParameter("dataset", ds).executeUpdate();
	}
}
