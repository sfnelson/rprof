package nz.ac.vuw.ecs.rprofs.server;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.server.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.server.data.ProfilerRun;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {

	private static Database db;
	private static Context current;

	static {
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		db = c.getBean(Database.class);
	}

	public static Context getInstance() {
		return current;
	}


	private final ProfilerRun run;

	private Map<Integer, ClassRecord> classes = new HashMap<Integer, ClassRecord>();
	private Map<Long, LogRecord> objects = new HashMap<Long, LogRecord>();

	private long eventId = 0;

	public Context() {
		run = db.createRun();
	}

	public static ProfilerDataSource<?, ?, ?, ?, ?> db() {
		return db;
	}

	public static void start() {
		if (current != null) {
			current.stop();
		}

		current = new Context();

		System.out.println("profiler run started at " + current.run.started);
	}

	public void stop() {
		current = null;

		run.stopped = Calendar.getInstance().getTime();
		db.update(run);

		db.storeClasses(run, classes.values());

		System.out.println("profiler run stopped at " + run.stopped);
	}

	public void storeLogs(List<LogRecord> records) {
		db.storeLogs(run, records);

		for (LogRecord r : records) {
			ClassRecord cls = Context.getInstance().getClass(r.cnum);
			MethodRecord mth = Context.getInstance().getMethod(cls, r.mnum);

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
				if (alloc.cnum != 0) {
					ClassRecord parent = classes.get(alloc.cnum);
					if (parent != null) parent.instances--;
				}
				alloc.cnum = r.cnum;
				alloc.mnum = r.mnum;
				if (cls != null) {
					// TODO shouldn't be able to be null
					cls.instances++;
				}
				db.update(run, alloc);
				break;
			case LogRecord.OBJECT_ALLOCATED:
				objects.put(r.args[0], r);
				break;
			}
		}
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
		mr.id = cr.getMethods().size() + 1;
		cr.getMethods().add(mr.id - 1, mr);
		return mr;
	}

	public FieldRecord createFieldRecord(ClassRecord cr) {
		FieldRecord fr = new FieldRecord();
		fr.parent = cr;
		fr.id = cr.getFields().size() + 1;
		cr.getFields().add(fr.id - 1, fr);
		return fr;
	}

	public void setMainMethod(String name) {
		if (run.program == null) {
			run.program = name;
			db.update(run);
		}
	}

	public ClassRecord getClass(int cnum) {
		return classes.get(cnum);
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

	public long nextEvent() {
		return eventId++;
	}
}
