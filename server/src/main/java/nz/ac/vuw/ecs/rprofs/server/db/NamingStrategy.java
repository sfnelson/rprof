package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import org.hibernate.cfg.ImprovedNamingStrategy;

@SuppressWarnings("serial")
public class NamingStrategy extends ImprovedNamingStrategy {

	public static ThreadLocal<Dataset> currentRun = new ThreadLocal<Dataset>();

	private Dataset run;

	public NamingStrategy() {
		run = currentRun.get();
		System.out.println("new naming strategy: " + run);
	}

	@Override
	public String tableName(String tableName) {
		if (run != null) {
			return "run_" + run.getHandle() + "_" + tableName;
		}
		else {
			return tableName;
		}
	}
}
