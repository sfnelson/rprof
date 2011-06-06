package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.request.ReportService;

public class ReportManager implements ReportService {

	public DatasetReport getDatasetReport() {
		return new DatasetReport();
	}

	public static Stat computeStats(List<Long> series) {
		float mean;
		float stddev;

		long sum = 0;
		for (Long v: series) {
			sum += v;
		}

		mean = ((float) sum) / series.size();

		double var = 0;
		for (Long v: series) {
			double d = v - mean;
			var += (d * d);
		}
		var = var / series.size();

		stddev = (float) Math.sqrt(var);

		return new Stat(mean, stddev);
	}
}