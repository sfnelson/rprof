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
import nz.ac.vuw.ecs.rprofs.server.reports.FinalFieldReport;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceReportFactory;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceReportGenerator;

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

				InstanceReportFactory<?> factory = new FinalFieldReport.ReportFactory(Context.this);
				InstanceReportGenerator reportGenerator = new InstanceReportGenerator(Context.this, factory);
				reportGenerator.run();

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

	public List<? extends Class> findClasses(String name) {
		List<Class> classes = Collections.newList();
		for (Class c: db.findRecords(Class.class)) {
			if (c.getPackage().equals(name)) {
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

	public List<? extends Event> findEvents(int start, int limit) {
		return db.findEvents(start, limit);
	}
}
