package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.transaction.annotation.Transactional;

public class ContextManager {

	private static ThreadLocal<Dataset> dataset = new ThreadLocal<Dataset>();

	public static Dataset getThreadLocal() {
		return dataset.get();
	}

	public static void setThreadLocal(Dataset dataset) {
		ContextManager.dataset.set(dataset);
	}

	private final Logger log = Logger.getLogger("context");
	private final Map<Dataset, ActiveContext> contexts = Collections.newMap();

	@PersistenceContext
	private EntityManager em;

	@Transactional
	public Dataset startRecording() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));

		log.info("profiler run started at " + now.getTime());

		Dataset ds = new Dataset(handle, now.getTime(), null, null);
		em.persist(ds);

		ActiveContext c = new ActiveContext();
		c.setDataset(ds);

		log.finest("storing context for dataset with id " + ds.getId());
		contexts.put(ds, c);

		return ds;
	}

	public void stopRecording(Dataset ds) {
		Calendar now = Calendar.getInstance();
		contexts.remove(getThreadLocal());
		log.info("profiler run stopped at " + now.getTime());
	}

	public ActiveContext getContext(Dataset ds) {
		log.finest("retrieving context for dataset with id " + ds.getId());
		return contexts.get(ds);
	}
}
