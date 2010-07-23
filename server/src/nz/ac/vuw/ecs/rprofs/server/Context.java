package nz.ac.vuw.ecs.rprofs.server;


import java.util.Calendar;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Context {

	Database db;
	ProfilerRun current;

	public Context() {
		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		db = c.getBean(Database.class);
	}

	public Database db() {
		return db;
	}

	public ProfilerRun current() {
		return current;
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
		context.db().update(run);
		
		System.out.println("profiler run stopped at " + run.stopped);

		context.db().storeClasses(run, Weaver.classes);
		Weaver.classes.clear();
	}

	private static Context context;

	public static Context getInstance() {
		if (context == null) {
			context = new Context();
		}

		return context;
	}
}
