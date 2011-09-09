package nz.ac.vuw.ecs.rprofs.server.context;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Map;

public class ContextManager {

	private static ThreadLocal<Dataset> dataset = new ThreadLocal<Dataset>();

	@Nullable
	public static Dataset getThreadLocal() {
		return dataset.get();
	}

	public static void setThreadLocal(@NotNull Dataset dataset) {
		ContextManager.dataset.set(dataset);
	}

	public static void clearThreadLocal() {
		ContextManager.dataset.set(null);
	}

	private final Logger log = LoggerFactory.getLogger(ContextManager.class);
	private final Map<Dataset, ActiveContext> contexts = Collections.newMap();

	@Autowired
	private Database database;

	public Dataset startRecording() {
		Dataset ds = database.createDataset();

		log.info("profiler run started at {}", ds.getStarted());

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

		database.setStopped(ds, now.getTime());
	}

	public ActiveContext getContext(Dataset ds) {
		log.debug("retrieving context for dataset {}", ds.getId());
		return contexts.get(ds);
	}
}
