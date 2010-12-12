package nz.ac.vuw.ecs.rprofs.server.data;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.ClassInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceInfo;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.RunInfo;
import nz.ac.vuw.ecs.rprofs.server.Database;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.reports.ReportGenerator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {

	private static Database database;

	static {
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		database = c.getBean(Database.class);
	}

	private static Map<String, Context> contexts = Collections.newMap();
	private static ActiveContext current;

	public static ActiveContext getCurrent() {
		return current;
	}

	public static Collection<RunRecord> getRuns() {
		return database.getProfiles();
	}

	public static void dropRun(RunInfo run) {
		if (contexts.containsKey(run.getHandle())) {
			Context c = contexts.get(run.getHandle());
			c.dispose();
			database.deleteRun(c.run);
			c.db.close();
		}
	}

	public static Context getInstance(RunInfo run) {
		if (!contexts.containsKey(run.getHandle())) {
			contexts.put(run.getHandle(), new Context(new RunRecord(run)));
		}

		NamingStrategy.currentRun.set(run);

		return contexts.get(run.getHandle());
	}

	public static void start() {
		if (current != null) {
			stop();
		}

		RunRecord record = database.createRun();
		current = new ActiveContext(record);
		contexts.put(current.run.getHandle(), current);

		System.out.println("profiler run started at " + record.getStarted());
	}

	public static void stop() {
		Context c = current;
		if (c == null) return;
		current = null;

		c.run.setStopped(Calendar.getInstance().getTime());
		database.updateRun(c.run);

		List<ClassRecord> classes = new ArrayList<ClassRecord>();
		classes.addAll(c.getClasses());
		c.db.saveClasses(c.run, classes);

		System.out.println("profiler run stopped at " + c.run.getStopped());
	}

	protected final RunRecord run;
	protected Map<Integer, ClassRecord> classIdMap;
	protected Map<String, ClassRecord> classNameMap;

	protected Database db;

	public Context(RunRecord run) {
		NamingStrategy.currentRun.set(run);

		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		db = c.getBean(Database.class);

		this.run = run;
	}

	public RunRecord getRun() {
		return run;
	}

	private void dispose() {
		contexts.remove(run.getHandle());

		for (ReportGenerator r: reports.values()) {
			r.dispose();
		}
	}

	public ClassRecord getClass(int cnum) {
		initClasses();

		return classIdMap.get(cnum);
	}

	public ClassRecord getClass(String fqn) {
		initClasses();

		return classNameMap.get(fqn);
	}

	public Collection<ClassRecord> getClasses() {
		initClasses();

		return Collections.immutable(classIdMap.values());
	}

	protected void initClasses() {
		if (classIdMap == null || db.getClasses().size() != classIdMap.size()) {
			classIdMap = Collections.newMap();
			classNameMap = Collections.newMap();

			for (ClassRecord cr: db.getClasses()) {
				classIdMap.put(cr.getId(), cr);
				classNameMap.put(cr.getName(), cr);
			}
		}
	}

	public MethodRecord getMethod(ClassRecord cls, int mnum) {
		if (cls == null) {
			return null;
		}
		if (mnum <= 0 || mnum > cls.getMethods().size()) {
			return null;
		}
		return cls.getMethods().get(mnum - 1);
	}

	public int getNumLogs(int type, ClassInfo cls) {
		return db.getNumLogs(type, cls);
	}

	public List<LogRecord> getLogs(int offset, int limit, int type, ClassInfo cls) {
		return db.getLogs(offset, limit, type, cls);
	}

	private Map<Report, ReportGenerator> reports = Collections.newMap();
	public ReportGenerator getReport(Report report) {
		if (!reports.containsKey(report)) {
			reports.put(report, ReportGenerator.create(report, db, this));
		}
		return reports.get(report);
	}

	public ExtendedInstanceData getInstanceInformation(InstanceInfo instance) {
		int count = db.getNumLogs(LogRecord.OBJECT_ALLOCATED, instance);
		if (count != 0) {
			LogRecord alloc = db.getLogs(count - 1, 1, LogRecord.OBJECT_ALLOCATED, instance).get(0);
			ClassRecord cr = getClass(alloc.getClassNumber());
			MethodRecord mr = cr.getMethods().get(alloc.getMethodNumber() - 1);
			ExtendedInstanceRecord info = new ExtendedInstanceRecord(this, instance.getId(), cr, mr);

			count = db.getNumLogs(LogRecord.FIELDS, instance);
			info.events = db.getLogs(0, count, LogRecord.FIELDS, instance);
			return info.toRPC();
		}
		else {
			return null;
		}
	}
}
