package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DatasetManager implements DatasetService {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private ContextManager cm;

	@Override
	public List<DataSet> findAllDatasets() {
		return em.createNamedQuery("allDatasets", DataSet.class).getResultList();
	}

	@Override
	public DataSet findDataset(String handle) {
		TypedQuery<DataSet> q = em.createNamedQuery("findDataset", DataSet.class);
		q.setParameter("handle", handle);
		List<DataSet> ds = q.getResultList();
		if (ds.isEmpty()) return null;
		else return ds.get(0);
	}

	@Override
	@Transactional
	public void stopDataset(String dataset) {
		DataSet ds = findDataset(dataset);

		ds.setStopped(Calendar.getInstance().getTime());

		cm.stopRecording(ds);
	}

	@Transactional
	public DataSet setStopped(DataSet dataSet, Date stopped) {
		DataSet ds = em.find(DataSet.class, dataSet.getId());
		ds.setStopped(dataSet.getStopped());
		return ds;
	}

	@Transactional
	public DataSet setProgram(DataSet dataSet, String program) {
		DataSet ds = em.find(DataSet.class, dataSet.getId());
		ds.setProgram(program);
		return ds;
	}

	@Override
	@Transactional
	public void deleteDataset(String handle) {
		DataSet ds = findDataset(handle);

		if (ds == null) return;

		em.createNamedQuery("deleteArguments").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteInstances").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteFields").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteMethods").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteClasses").setParameter("dataset", ds).executeUpdate();
		em.createNamedQuery("deleteDataset").setParameter("dataset", ds).executeUpdate();

		// @TODO delete events from mongodb
	}
}
