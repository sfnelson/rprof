package nz.ac.vuw.ecs.rprofs.server.db;

import org.hibernate.cfg.ImprovedNamingStrategy;

@SuppressWarnings("serial")
public class NamingStrategy extends ImprovedNamingStrategy {

	public static ThreadLocal<String> currentRun = new ThreadLocal<String>();

	private String dataset;

	public NamingStrategy() {
		dataset = currentRun.get();
		System.out.println("new naming strategy: " + dataset);
	}

	@Override
	public String tableName(String tableName) {
		if (dataset != null) {
			return "run_" + dataset + "_" + tableName;
		}
		else {
			return tableName;
		}
	}
}
