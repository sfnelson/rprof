package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.client.data.RunInfo;

import org.hibernate.cfg.ImprovedNamingStrategy;

@SuppressWarnings("serial")
public class NamingStrategy extends ImprovedNamingStrategy {

	public static ThreadLocal<RunInfo> currentRun = new ThreadLocal<RunInfo>();

	private RunInfo run;

	public NamingStrategy() {
		run = currentRun.get();
		System.out.println("new naming strategy: " + run);
	}

	@Override
	public String tableName(String tableName) {
		if (currentRun.get() != null) {
			return tableName + "_" + currentRun.get().getHandle();
		}
		else {
			return tableName;
		}
	}
}
