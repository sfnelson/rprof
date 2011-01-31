package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.StringTokenizer;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public class Report {

	public static Report findReport(String id) {
		StringTokenizer tok = new StringTokenizer(id, "_");

		assert(tok.countTokens() == 2);

		Dataset dataset = Dataset.findDataset(tok.nextToken());
		return ReportFactory.getInstance().createReport(dataset, tok.nextToken());
	}

	Dataset dataset;
	String name;

	public Report() {}

	public Report(Dataset dataset, String name) {
		this.dataset = dataset;
		this.name = name;
	}

	public String getId() {
		return dataset.getHandle() + "_" + name;
	}

	public int getVersion() {
		return 0;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public String getReport() {
		return name;
	}
}
