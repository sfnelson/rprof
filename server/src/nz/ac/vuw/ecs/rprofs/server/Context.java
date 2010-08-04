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

	private static Context context;

	public static Context getInstance() {
		if (context == null) {
			context = new Context();
		}

		return context;
	}
	
	private Database db;
	private ProfilerRun current;
	
	private Map<Integer, ClassRecord> classes = new HashMap<Integer, ClassRecord>();

	public Context() {
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		db = c.getBean(Database.class);
	}

	public Database db() {
		return db;
	}

	public void start() {
		if (current != null) {
			stop();
		}
		
		ProfilerRun run = db.createRun();

		System.out.println("profiler run started at " + run.started);		

		current = run;
	}

	public void stop() {
		ProfilerRun run = current;
		current = null;
		
		run.stopped = Calendar.getInstance().getTime();
		context.db.update(run);
		
		System.out.println("profiler run stopped at " + run.stopped);
	}

	public void storeLogs(List<LogRecord> records) {
		db.storeLogs(current, records);
	}

	public ClassRecord createClassRecord() {
		ClassRecord cr = new ClassRecord();
		cr.id = current.numClasses++;
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
	
	public void storeClassRecord(ClassRecord cr) {
		db.storeClass(current, cr);
	}

	public void setMainMethod(String name) {
		if (current.program == null) {
			current.program = name;
			db.update(current);
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
