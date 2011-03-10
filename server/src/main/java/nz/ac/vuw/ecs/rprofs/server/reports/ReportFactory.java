package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.Context;

public class ReportFactory {

	private static final ReportFactory instance = new ReportFactory();

	public static ReportFactory getInstance() {
		return instance;
	}

	public Report createReport(Context context, String report) {
		return new Report(context, report);
	}
}
