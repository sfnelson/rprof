package nz.ac.vuw.ecs.rprofs.server.context;

import java.util.Calendar;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class ContextManager {

	private static final ContextManager instance = new ContextManager();

	public static ContextManager getInstance() {
		return instance;
	}

	private final Map<String, Context> contexts = Collections.newMap();

	private final DatasetManager datasets = new DatasetManager();

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

		ApplicationContext c;

		WebApplicationContext parent = ContextLoaderListener.getCurrentWebApplicationContext();

		if (dataset == null) {
			c = parent;
		}
		else {
			XmlWebApplicationContext child = new XmlWebApplicationContext();
			child.setServletContext(parent.getServletContext());
			child.refresh();
			c = child;
		}

		Context context = c.getBean(Context.class);

		NamingStrategy.currentRun.set(null);

		return context;
	}

	private ThreadLocal<Context> current = new ThreadLocal<Context>();

	private ActiveContext active;

	public Context getCurrent() {
		return current.get();
	}

	public void setCurrent(Context context) {
		current.set(context);
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

		datasets.add(ds);

		active = new ActiveContext(createContext(ds.getHandle()), ds);

		contexts.put(ds.getHandle(), active.getContext());
	}

	public void stopRecording() {
		if (active == null) return;

		Dataset ds = datasets.findDataset(active.getDataset().getId());
		ds.setStopped(Calendar.getInstance().getTime());

		System.out.println("profiler run stopped at " + ds.getStopped());

		active = null;
	}
}
