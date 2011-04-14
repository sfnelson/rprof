package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.Datastore;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldWriteRecord;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Package;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class Context {

	protected final Dataset dataset;
	protected final Datastore db;

	protected Context(Dataset dataset) {
		this.dataset = dataset;

		NamingStrategy.currentRun.set(dataset);
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");
		db = c.getBean(Datastore.class);
		NamingStrategy.currentRun.set(null);
	}

	public void stop() {
		setStopped();

		new Thread() {
			@Override
			public void run() {
				System.out.println("generating reports started at " + Calendar.getInstance().getTime());

				//TODO generate reports here

				System.out.println("generating reports stopped at " + Calendar.getInstance().getTime());
			}
		}.start();
	}

	protected void setStopped() {
		Datastore database = ContextManager.getInstance().database;

		Dataset dataset = database.findRecord(Dataset.class, this.dataset.getId());
		dataset.setStopped(Calendar.getInstance().getTime());
		database.updateDataset(dataset);

		System.out.println("profiler run stopped at " + dataset.getStopped());
	}

	protected void setProgram(String program) {
		Datastore database = ContextManager.getInstance().database;

		final Dataset dataset = database.findRecord(Dataset.class, this.dataset.getId());
		dataset.setProgram(program);
		database.updateDataset(dataset);
	}

	protected void close() {
		db.close();
	}

	public List<? extends Package> findPackages() {
		Map<String, Package> packages = Collections.newMap();
		for (Class c: db.findRecords(Class.class)) {
			String name = c.getPackage();
			Package p = packages.get(name);
			if (p == null) {
				p = new Package(dataset.getHandle(), name, 1);
				packages.put(name, p);
			}
			else {
				p.incrementClasses();
			}
		}
		return new ArrayList<Package>(packages.values());
	}

	public int findNumClasses() {
		return db.findNumRecords(Class.class);
	}

	public int findNumObjects() {
		return db.findNumRecords(Instance.class);
	}

	public List<? extends Class> findClasses(String pkg) {
		List<Class> classes = Collections.newList();
		for (Class c: db.findRecords(Class.class)) {
			if (c.getPackage().equals(pkg)) {
				classes.add(c);
			}
		}
		return classes;
	}

	public List<? extends Instance> findInstances(ClassId id) {
		Class cls = db.findRecord(Class.class, id);
		if (cls != null) {
			return db.findInstancesByType(cls);
		}
		return Collections.newList();
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void storeReports(Collection<FieldWriteRecord> results) {
		db.storeRecords(results);
	}

	public Long findNumEvents(int filter) {
		return db.findNumEvents(filter);
	}

	public List<? extends Event> findEvents(int start, int limit, int filter) {
		return db.findEvents(start, limit, filter);
	}

	public List<Long> findObjectsPerClass() {
		return db.findObjectsPerClass();
	}

	public Long findEventIndex(EventId id, int filter) {
		return db.findEventIndex(id, filter);
	}

	public List<? extends Event> findEventsWithArgument(Instance i) {
		return db.findEventsWithArgument(i);
	}
}
