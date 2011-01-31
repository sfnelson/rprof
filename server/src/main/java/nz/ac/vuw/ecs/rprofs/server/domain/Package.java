package nz.ac.vuw.ecs.rprofs.server.domain;

public class Package {

	private String name;
	private int numClasses;

	public Package(String name, int numClasses) {
		this.name = name;
		this.numClasses = numClasses;
	}

	public String getId() {
		return name;
	}

	public int getVersion() {
		return 1;
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
