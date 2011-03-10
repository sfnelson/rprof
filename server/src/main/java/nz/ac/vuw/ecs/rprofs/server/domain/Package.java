package nz.ac.vuw.ecs.rprofs.server.domain;

public class Package {

	private String dataset;
	private String name;
	private int numClasses;

	public Package() {}

	public Package(String dataset, String name, int numClasses) {
		this.dataset = dataset;
		this.name = name;
		this.numClasses = numClasses;
	}

	public String getDataset() {
		return dataset;
	}

	public String getName() {
		return name;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public void incrementClasses() {
		numClasses++;
	}
}
