package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.domain.Instance;

public interface InstanceReportFactory<R extends InstanceReport> {

	public R generateReport(Instance instance);
	public void processResults(R report);

}
