package nz.ac.vuw.ecs.rprofs.server;


import java.util.Calendar;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

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
		return cr;
	}
	
	public void storeClassRecord(ClassRecord cr) {
		db.storeClass(current, cr);
	}
}
