package nz.ac.vuw.ecs.rprofs.server.data;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.server.Database;
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
	protected Map<Integer, ClassRecord> classIdMap;
	protected Map<String, ClassRecord> classNameMap;
	
	public Context(ProfilerRun run) {
		this.run = run;
	}
	
	public ProfilerRun getRun() {
		return run;
	}
	
	private void dispose() {
		contexts.remove(run.handle);
		
		for (ReportGenerator r: reports.values()) {
			r.dispose();
		}
	}

	public ClassRecord getClass(int cnum) {
		if (classIdMap == null || db.getNumClasses(run) != classIdMap.size()) {
			initClasses();
		}
		
		return classIdMap.get(cnum);
	}

	public ClassRecord getClass(String fqn) {
		if (classIdMap == null || db.getNumClasses(run) != classIdMap.size()) {
			initClasses();
		}
		
		return classNameMap.get(fqn);
	}

	public Collection<ClassRecord> getClasses() {
		if (classIdMap == null || db.getNumClasses(run) != classIdMap.size()) {
			initClasses();
		}
		
		return Collections.immutable(classIdMap.values());
	}
	
	protected void initClasses() {
		classIdMap = Collections.newMap();
		classNameMap = Collections.newMap();
		
		for (ClassRecord cr: db.getClasses(run, this)) {
			classIdMap.put(cr.getId(), cr);
			classNameMap.put(cr.getName(), cr);
		}
		
		db.getFields(run, this);
		db.getMethods(run, this);
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
	
	public int getNumLogs(int type, int cls) {
		return db.getNumLogs(run, type, cls);
	}
	
	public List<LogRecord> getLogs(int offset, int limit, int type, int cls) {
		return db.getLogs(run, offset, limit, type, cls);
	}
	
	private Map<Report, ReportGenerator> reports = Collections.newMap();
	public ReportGenerator getReport(Report report) {
		if (!reports.containsKey(report)) {
			reports.put(report, ReportGenerator.create(report, db, this)); 
		}
		return reports.get(report);
	}
	
	public InstanceData getInstanceInformation(long id) {
		int count = db.getNumLogs(run, LogRecord.OBJECT_ALLOCATED, id);
		if (count != 0) {
			LogRecord alloc = db.getLogs(run, count - 1, 1, LogRecord.OBJECT_ALLOCATED, id).get(0);
			ClassRecord cr = getClass(alloc.getClassNumber());
			MethodRecord mr = cr.getMethods().get(alloc.getMethodNumber() - 1);
			InstanceRecord info = new InstanceRecord(id, cr, mr);
			
			count = db.getNumLogs(run, LogRecord.FIELDS, id);
			info.events = db.getLogs(run, 0, count, LogRecord.FIELDS, id);
			return info.toRPC();
		}
		else {
			return null;
		}
	}
	
	public static class ActiveContext extends Context {
		
		private long eventId = 0;
		private Map<Long, LogRecord> objects;
		
		public ActiveContext() {
			super(db.createRun());
			System.out.println("new context");
			
			classIdMap = Collections.newMap();
			classNameMap = Collections.newMap();
			objects = Collections.newMap();
			
			System.out.println("profiler run started at " + run.started);
		}
		
		@Override
		protected void initClasses() {
			// don't do anything for active contexts, we don't want to pull from an empty database.
		}

		public void stop() {
			current = null;

			run.stopped = Calendar.getInstance().getTime();
			db.update(run);

			List<ClassRecord> c = new ArrayList<ClassRecord>();
			c.addAll(getClasses());
			db.storeClasses(run, c);

			System.out.println("profiler run stopped at " + run.stopped);
		}
		
		public void storeLogs(List<LogRecord> records) {
			List<LogRecord> remove = Collections.newList();
			List<LogRecord> updates = Collections.newList();
			
			for (LogRecord r : records) {
				ClassRecord cls = getClass(r.getClassNumber());
				MethodRecord mth = getMethod(cls, r.getMethodNumber());

				switch (r.getEvent()) {
				case LogRecord.METHOD_ENTER:
					if (mth == null || !mth.isMain()) break;
					setMainMethod(cls.getName());
					break;
				case LogRecord.METHOD_RETURN:
					if (mth == null || !mth.isInit()) break;
				case LogRecord.OBJECT_TAGGED:
					LogRecord alloc = objects.get(r.getArguments()[0]);
					if (alloc == null) break;
					alloc.cnum = r.cnum;
					alloc.mnum = r.mnum;
					updates.add(alloc);
					break;
				case LogRecord.OBJECT_ALLOCATED:
					objects.put(r.getArguments()[0], r);
					remove.add(r);
					break;
				case LogRecord.OBJECT_FREED:
					objects.remove(r.getArguments()[0]);
					break;
				}
			}
			
			records.removeAll(remove);
			
			db.storeLogs(run, records);
			db.storeLogs(run, updates);
		}
		
		public ClassRecord createClassRecord() {
			ClassRecord cr = new ClassRecord(this, ++run.numClasses);
			classIdMap.put(cr.getId(), cr);
			return cr;
		}
		
		public void initClassRecord(ClassRecord cr, int version, int access, String name, String signature,
				String superName, String[] interfaces) {
			if (cr.context == this) {
				cr.init(version, access, name, signature, superName, interfaces);
				classNameMap.put(name, cr);
			}
			else {
				throw new RuntimeException("class record not owned by this context");
			}
		}
		
		public FieldRecord createFieldRecord(ClassRecord cr) {
			return new FieldRecord(cr, cr.fields.size() + 1);
		}
		
		public void initFieldRecord(FieldRecord fr, int access, String name, String desc) {
			if (fr.parent.context == this) {
				fr.init(access, name, desc);
			}
			else {
				throw new RuntimeException("field record not owned by this context");
			}
		}
		
		public MethodRecord createMethodRecord(ClassRecord cr) {
			return new MethodRecord(cr, cr.methods.size() + 1);
		}
		
		public void initMethodRecord(MethodRecord mr, int access, String name, String desc,
				String signature, String[] exceptions) {
			if (mr.parent.context == this) {
				mr.init(access, name, desc, signature, exceptions);
			}
			else {
				throw new RuntimeException("method record not owned by this context");
			}
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
