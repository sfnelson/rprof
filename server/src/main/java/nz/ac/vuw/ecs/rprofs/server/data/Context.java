package nz.ac.vuw.ecs.rprofs.server.data;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

public class Context {

	private static Map<DatasetId, Context> contexts = Collections.newMap();
	private static ActiveContext current;

	public static ActiveContext getCurrent() {
		return current;
	}

	public static Collection<? extends DatasetId> getDatasets() {
		ArrayList<DatasetId> ids = Collections.newList();
		for (Dataset d: Dataset.findAllDatasets()) {
			ids.add(d.getDatasetId());
		}
		return ids;
	}

	public static void dropDataset(DatasetId id) {
		if (contexts.containsKey(id)) {
			Context c = contexts.get(id);
			c.dispose();
			Dataset.deleteDataset(c.db);
		}
	}

	public static Context getInstance(DatasetId id) {
		if (!contexts.containsKey(id)) {
			contexts.put(id, new Context(Dataset.getDataset(id)));
		}

		return contexts.get(id);
	}

	public static void start() {
		if (current != null) {
			stop();
		}

		Dataset record = Dataset.createDataset();
		current = new ActiveContext(record);
		contexts.put(current.db.getDatasetId(), current);

		System.out.println("profiler run started at " + record.getStarted());
	}

	public static void stop() {
		ActiveContext c = current;
		if (c == null) return;
		current = null;

		c.db = Dataset.setStopped(c.db, Calendar.getInstance().getTime());

		System.out.println("profiler run stopped at " + c.db.getStopped());
	}

	protected Dataset db;

	public Context(Dataset data) {
		db = data;
	}

	public DatasetId getDataset() {
		return db.getDatasetId();
	}

	private void dispose() {
		contexts.remove(db.getDatasetId());

		/*for (ReportGenerator r: reports.values()) {
			r.dispose();
		}*/
	}

	public Collection<? extends Class> getClasses() {
		return db.getClasses();
	}

	public int getNumLogs(int type, ClassId cls) {
		return db.getNumLogs(type, cls);
	}

	public List<? extends Event> getLogs(int offset, int limit, int type, ClassId cls) {
		return db.getLogs(offset, limit, type, cls);
	}

	//	private Map<Report, ReportGenerator> reports = Collections.newMap();
	//	public ReportGenerator getReport(Report report) {
	//		if (!reports.containsKey(report)) {
	//			reports.put(report, ReportGenerator.create(report, db, this));
	//		}
	//		return reports.get(report);
	//	}

	public Instance getInstanceInformation(InstanceId instance) {
		int count = db.getNumLogs(Event.OBJECT_ALLOCATED, instance);
		if (count != 0) {
			Event alloc = db.getLogs(count - 1, 1, Event.OBJECT_ALLOCATED, instance).get(0);
			Class type = alloc.getType();
			Method method = (Method) alloc.getAttribute();
			List<? extends Event> events = db.getLogs(Event.FIELDS, instance);
			return new Instance(instance, type, method, events);
		}
		else {
			return null;
		}
	}
}
