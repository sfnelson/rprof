package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.Calendar;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

public class ContextManager {

	private static final ContextManager instance = new ContextManager();

	public static ContextManager getInstance() {
		return instance;
	}

	private final Map<String, Context> contexts = Collections.newMap();

	public Context getContext(String dataset) {
		Context context;
		if (contexts.containsKey(dataset)) {
			context = contexts.get(dataset);
		}
		else {
			context = createContext(dataset);
			contexts.put(dataset, context);
		}
		return context;
	}

	private Context createContext(String dataset) {
		NamingStrategy.currentRun.set(dataset);

		ApplicationContext c = ContextLoaderListener.getCurrentWebApplicationContext();
		Context context = c.getBean(Context.class);

		NamingStrategy.currentRun.set(null);

		return context;
	}

	private ThreadLocal<Context> current = new ThreadLocal<Context>();

	private ActiveContext active;

	public Context getCurrent() {
		return current.get();
	}

	public Context setCurrent(String dataset) {
		Context c = getContext(dataset);
		current.set(c);
		return c;
	}

	public Context getDefault() {
		return getContext(null);
	}

	public ActiveContext getActive() {
		return active;
	}

	public void startRecording() {
		Context c = getDefault();

		Calendar s = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				s.get(Calendar.YEAR),
				s.get(Calendar.MONTH),
				s.get(Calendar.DATE),
				s.get(Calendar.HOUR),
				s.get(Calendar.MINUTE),
				s.get(Calendar.SECOND));

		Dataset ds = new Dataset(handle, s.getTime(), null, null);

		System.out.println("profiler run started at " + ds.getStarted());

		try {
			c.open();
			c.em().persist(ds);
		}
		finally {
			c.close();
		}

		active = new ActiveContext(createContext(ds.getHandle()), ds);

		contexts.put(ds.getHandle(), active.getContext());
	}

	public void stopRecording() {
		if (active == null) return;

		Dataset ds = active.getDataset();

		Context c = getDefault();
		c.open();
		ds = c.find(Dataset.class, ds.getId());
		ds.setStopped(Calendar.getInstance().getTime());
		c.close();

		System.out.println("profiler run stopped at " + ds.getStopped());

		active = null;
	}
}
