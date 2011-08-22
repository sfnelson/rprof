package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.Calendar;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class ContextManager {

	private static ThreadLocal<Dataset> dataset = new ThreadLocal<Dataset>();

	public static Dataset getThreadLocal() {
		return dataset.get();
	}

	public static void setThreadLocal(Dataset dataset) {
		ContextManager.dataset.set(dataset);
	}

	private final Logger log = LoggerFactory.getLogger(ContextManager.class);
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

		log.info("profiler run started at {}", now.getTime());

		Dataset ds = new Dataset(handle, now.getTime(), null, null);
		em.persist(ds);

		ActiveContext c = new ActiveContext();
		c.setDataset(ds);

		log.debug("storing context for dataset {}", ds.getId());
		contexts.put(ds, c);

		return ds;
	}

	public void stopRecording(Dataset ds) {
		Calendar now = Calendar.getInstance();
		contexts.remove(getThreadLocal());
		log.info("profiler run stopped at {}", now.getTime());
	}

	public ActiveContext getContext(Dataset ds) {
		log.debug("retrieving context for dataset {}", ds.getId());
		return contexts.get(ds);
	}
}
