package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.StringTokenizer;

import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class Report {

	public static Report findReport(String id) {
		StringTokenizer tok = new StringTokenizer(id, "_");

		assert(tok.countTokens() == 2);

		Context context = ContextManager.getInstance().getContext(tok.nextToken());
		return ReportFactory.getInstance().createReport(context, tok.nextToken());
	}

	Context context;
	String name;

	public Report() {}

	public Report(Context context, String name) {
		this.context = context;
		this.name = name;
	}

	public String getId() {
		return context.getDataset().getHandle() + "_" + name;
	}

	public int getVersion() {
		return 0;
	}

	public Dataset getDataset() {
		return context.getDataset();
	}

	public String getReport() {
		return name;
	}
}
