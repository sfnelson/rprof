package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Package;

public class InstanceReportGenerator implements Runnable {

	private final InstanceReportFactory factory;
	private final Context data;

	public InstanceReportGenerator(Context data, InstanceReportFactory factory) {
		this.data = data;
		this.factory = factory;
	}

	public void run() {
		for (Package pkg: data.findPackages()) {
			processPackage(pkg);
		}
	}

	public void processPackage(Package pkg) {
		for (Class cls: data.findClasses(pkg.getName())) {
			System.out.println("processing " + cls.getName());
			processClass(cls);
		}
	}

	public void processClass(Class cls) {
		for (Instance i: data.findInstances(cls.getId())) {
			processInstance(i);
		}
	}

	public void processInstance(Instance instance) {
		InstanceReport report = factory.generateReport(instance);
		report.run();
		factory.processResults(report);
	}
}
