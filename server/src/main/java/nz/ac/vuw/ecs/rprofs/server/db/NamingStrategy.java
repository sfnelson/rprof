package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset.DatasetId;

import org.hibernate.cfg.ImprovedNamingStrategy;

@SuppressWarnings("serial")
public class NamingStrategy extends ImprovedNamingStrategy {

	public static ThreadLocal<DatasetId> currentRun = new ThreadLocal<DatasetId>();

	private DatasetId run;

	public NamingStrategy() {
		run = currentRun.get();
		System.out.println("new naming strategy: " + run);
	}

	@Override
	public String tableName(String tableName) {
		if (currentRun.get() != null) {
			return "run_" + currentRun.get().getHandle() + "_" + tableName;
		}
		else {
			return tableName;
		}
	}
}
