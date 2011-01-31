package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Package;

public class InstanceReportGenerator implements Runnable {

	private final InstanceReportFactory factory;
	private final Dataset dataset;

	public InstanceReportGenerator(Dataset dataset, InstanceReportFactory factory) {
		this.dataset = dataset;
		this.factory = factory;
	}

	public void run() {
		for (Package pkg: dataset.findPackages()) {
			processPackage(pkg);
		}
	}

	public void processPackage(Package pkg) {
		for (Class cls: dataset.findClasses(pkg.getName())) {
			System.out.println("processing " + cls.getName());
			processClass(cls);
		}
	}

	public void processClass(Class cls) {
		for (Instance i: dataset.findInstances(cls.getIndex())) {
			processInstance(i);
		}
	}

	public void processInstance(Instance instance) {
		InstanceReport report = factory.generateReport(instance);
		report.run();
		factory.processResults(report);
	}
}
