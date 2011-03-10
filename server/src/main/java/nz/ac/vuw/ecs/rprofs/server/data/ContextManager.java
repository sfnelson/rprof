package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.Calendar;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.Datastore;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ContextManager {

	private static final ContextManager instance = new ContextManager();

	public static ContextManager getInstance() {
		return instance;
	}

	private final Map<Short, Context> contexts;

	final Datastore database;

	private ContextManager() {
		NamingStrategy.currentRun.set(null);

		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		database = c.getBean(Datastore.class);
		contexts = Collections.newMap();
	}

	public Context getContext(Dataset dataset) {
		if (dataset == null) {
			return null;
		}

		return getContext(dataset.getId());
	}

	public Context getContext(short id) {
		if (contexts.containsKey(id)) {
			return contexts.get(id);
		}

		Dataset dataset = database.findRecord(Dataset.class, id);
		if (dataset == null) {
			return null;
		}

		Context context = new Context(dataset);
		contexts.put(dataset.getId(), context);
		return context;
	}

	public Context getContext(String handle) {
		Dataset ds = database.findDatasetByHandle(handle);
		if (ds == null) {
			return null;
		}

		return getContext(ds);
	}

	private ActiveContext current;

	public ActiveContext getCurrent() {
		return current;
	}

	public ActiveContext start() {
		Calendar s = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				s.get(Calendar.YEAR),
				s.get(Calendar.MONTH),
				s.get(Calendar.DATE),
				s.get(Calendar.HOUR),
				s.get(Calendar.MINUTE),
				s.get(Calendar.SECOND));

		Dataset dataset = new Dataset(handle, s.getTime(), null, null);
		dataset = database.storeRecord(dataset);

		current = new ActiveContext(dataset);
		contexts.put(dataset.getId(), current);

		return current;
	}

	public void stop(Dataset dataset) {
		Context c = getContext(dataset);
		if (c == null) {
			return;
		}

		if (c == current) {
			current = null;
		}

		c.stop();
	}

	public void delete(Dataset dataset) {
		Context c = contexts.remove(dataset.getId());
		database.deleteDataset(dataset);
		database.deleteDatastore(dataset);
		c.close();
	}
}
