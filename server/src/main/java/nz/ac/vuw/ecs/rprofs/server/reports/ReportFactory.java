package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class ReportFactory {

	private static final ReportFactory instance = new ReportFactory();

	public static ReportFactory getInstance() {
		return instance;
	}

	public Report createReport(Dataset dataset, String report) {
		return new Report(dataset, report);
	}
}
