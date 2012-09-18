package nz.ac.vuw.ecs.rprofs.server.db.reports;

public class Stat {

	private float mean;
	private float stddev;

	public Stat(float mean, float stddev) {
		this.mean = mean;
		this.stddev = stddev;
	}

	public float getMean() {
		return mean;
	}

	public float getStdDev() {
		return stddev;
	}

}
