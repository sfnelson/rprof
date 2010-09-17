package nz.ac.vuw.ecs.rprofs.server;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.reports.InstanceReportGenerator;
import nz.ac.vuw.ecs.rprofs.server.reports.ReportGenerator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {

	private static Database db;

	static {
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		db = c.getBean(Database.class);
	}
	
	private static Map<String, Context> contexts = Collections.newMap();
	private static ActiveContext current;

	public static ActiveContext getCurrent() {
		return current;
	}
	
	public static Collection<ProfilerRun> getRuns() {
		return db.getProfiles();
	}
	
	public static void dropRun(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		if (contexts.containsKey(run.handle)) {
			contexts.get(run.handle).dispose();
		}
		db.dropRun(run);
	}
	
	public static Context getInstance(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run) {
		if (!contexts.containsKey(run.handle)) {
			contexts.put(run.handle, new Context(new ProfilerRun(run)));
		}
		
		return contexts.get(run.handle);
	}
	
	public static void start() {
		if (current != null) {
			current.stop();
		}

		current = new ActiveContext();
		contexts.put(current.run.handle, current);
	}
	
	protected final ProfilerRun run;
	protected Map<Integer, ClassRecord> classes;
	
	public Context(ProfilerRun run) {
		this.run = run;
	}
	
	private void dispose() {
		contexts.remove(run.handle);
		
		if (instanceReport != null) {
			instanceReport.dispose();
		}
	}

	public ClassRecord getClass(int cnum) {
		if (classes == null || db.getNumClasses(run) != classes.size()) {
			initClasses();
		}
		
		return classes.get(cnum);
	}

	public Collection<ClassRecord> getClasses() {
		if (classes == null || db.getNumClasses(run) != classes.size()) {
			initClasses();
		}
		
		return Collections.immutable(classes.values());
	}
	
	private void initClasses() {
		classes = Collections.newMap();
		for (ClassRecord cr: db.getClasses(run)) {
			classes.put(cr.id, cr);
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
	
	public int getNumLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run2, int type) {
		return db.getNumLogs(run2, type);
	}
	
	public List<LogRecord> getLogs(nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun run2,
			int offset, int limit, int type) {
		return db.getLogs(run2, offset, limit, type);
	}
	
	private Map<Report, ReportGenerator> reports = Collections.newMap();
	public ReportGenerator getReport(Report report) {
		if (!reports.containsKey(report)) {
			reports.put(report, ReportGenerator.create(report, run, db)); 
		}
		return reports.get(report);
	}
	
	private InstanceReportGenerator instanceReport;
	public InstanceReportGenerator getInstanceReport() {
		if (instanceReport == null) {
			instanceReport = new InstanceReportGenerator(run, db);
		}
		return instanceReport;
	}
	
	public static class ActiveContext extends Context {
		
		private long eventId = 0;
		private Map<Long, LogRecord> objects;
		
		public ActiveContext() {
			super(db.createRun());
			
			classes = Collections.newMap();
			objects = Collections.newMap();
			
			System.out.println("profiler run started at " + current.run.started);
		}

		public void stop() {
			current = null;

			run.stopped = Calendar.getInstance().getTime();
			db.update(run);

			List<ClassRecord> c = new ArrayList<ClassRecord>();
			c.addAll(this.classes.values());
			db.storeClasses(run, c);

			System.out.println("profiler run stopped at " + run.stopped);
		}
		
		public void storeLogs(List<LogRecord> records) {
			List<LogRecord> remove = Collections.newList();
			List<LogRecord> updates = Collections.newList();
			
			for (LogRecord r : records) {
				ClassRecord cls = getClass(r.cnum);
				MethodRecord mth = getMethod(cls, r.mnum);

				switch (r.event) {
				case LogRecord.METHOD_ENTER:
					if (mth == null || !mth.isMain()) break;
					setMainMethod(cls.name);
					break;
				case LogRecord.METHOD_RETURN:
					if (mth == null || !mth.isInit()) break;
				case LogRecord.OBJECT_TAGGED:
					LogRecord alloc = objects.get(r.args[0]);
					if (alloc == null) break;
					alloc.cnum = r.cnum;
					alloc.mnum = r.mnum;
					updates.add(alloc);
					break;
				case LogRecord.OBJECT_ALLOCATED:
					objects.put(r.args[0], r);
					remove.add(r);
					break;
				case LogRecord.OBJECT_FREED:
					objects.remove(r.args[0]);
					break;
				}
			}
			
			records.removeAll(remove);
			
			db.storeLogs(run, records);
			db.storeLogs(run, updates);
		}
		
		public ClassRecord createClassRecord() {
			ClassRecord cr = new ClassRecord();
			cr.id = ++run.numClasses;
			classes.put(cr.id, cr);
			return cr;
		}
		
		public MethodRecord createMethodRecord(ClassRecord cr) {
			MethodRecord mr = new MethodRecord();
			mr.parent = cr;
			cr.getMethods().add(mr);
			mr.id = cr.getMethods().size();
			return mr;
		}

		public FieldRecord createFieldRecord(ClassRecord cr) {
			FieldRecord fr = new FieldRecord();
			fr.parent = cr;
			cr.getFields().add(fr);
			fr.id = cr.getFields().size();
			return fr;
		}
		
		public void setMainMethod(String name) {
			if (run.program == null) {
				run.program = name;
				db.update(run);
			}
		}
		
		public long nextEvent() {
			return eventId++;
		}
	}
}
