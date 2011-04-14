package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.Context;


public class DatasetReport {

	private int numClasses;
	private int numObjects;
	private Stat objectsPerClass;
	private Stat writesPerClass;
	private Stat writesPerObject;

	public DatasetReport(Context context) {
		this.numClasses = context.findNumClasses();
		this.numObjects = context.findNumObjects();

		this.objectsPerClass = Report.computeStats(context.findObjectsPerClass());
		this.writesPerClass = new Stat(0, 0);
		this.writesPerObject = new Stat(0, 0);
	}

	public int getNumClasses() {
		return numClasses;
	}

	public int getNumObjects() {
		return numObjects;
	}

	public Stat getObjectsPerClass() {
		return objectsPerClass;
	}

	public Stat getWritesPerClass() {
		return writesPerClass;
	}

	public Stat getWritesPerObject() {
		return writesPerObject;
	}

	public Stat getReadsPerClass() {
		return new Stat(0, 0);
	}

	public Stat getReadsPerObject() {
		return new Stat(0, 0);
	}
}
