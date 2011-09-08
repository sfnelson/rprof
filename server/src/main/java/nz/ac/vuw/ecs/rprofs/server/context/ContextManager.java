package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.Calendar;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class ContextManager {

	private static ThreadLocal<DataSet> dataset = new ThreadLocal<DataSet>();

	public static @Nullable DataSet getThreadLocal() {
		return dataset.get();
	}

	public static void setThreadLocal(@Nullable DataSet dataSet) {
		ContextManager.dataset.set(dataSet);
	}

	private final Logger log = LoggerFactory.getLogger(ContextManager.class);
	private final Map<DataSet, ActiveContext> contexts = Collections.newMap();

	@PersistenceContext
	private EntityManager em;

	@Transactional
	public DataSet startRecording() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));

		log.info("profiler run started at {}", now.getTime());

		DataSet ds = new DataSet(handle, now.getTime(), null, null);
		em.persist(ds);

		ActiveContext c = new ActiveContext();
		c.setDataSet(ds);

		log.debug("storing context for dataSet {}", ds.getId());
		contexts.put(ds, c);

		return ds;
	}

	public void stopRecording(DataSet ds) {
		Calendar now = Calendar.getInstance();
		contexts.remove(getThreadLocal());
		log.info("profiler run stopped at {}", now.getTime());
	}

	public ActiveContext getContext(DataSet ds) {
		log.debug("retrieving context for dataSet {}", ds.getId());
		return contexts.get(ds);
	}
}
