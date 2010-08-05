package nz.ac.vuw.ecs.rprofs.server;


import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.server.data.MethodRecord;

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

	public Context() {
		run = db.createRun();
	}

	public static Database db() {
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
	}

	public ClassRecord createClassRecord() {
		ClassRecord cr = new ClassRecord();
		cr.id = run.numClasses++;
		classes.put(cr.id, cr);
		return cr;
	}
	
	public MethodRecord createMethodRecord(ClassRecord cr) {
		MethodRecord mr = new MethodRecord();
		mr.parent = cr;
		mr.id = cr.getMethods().size();
		cr.getMethods().add(mr.id, mr);
		return mr;
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
		return cls.getMethods().get(mnum);
	}
}
