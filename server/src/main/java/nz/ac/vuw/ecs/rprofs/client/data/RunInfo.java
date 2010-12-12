package nz.ac.vuw.ecs.rprofs.client.data;

import java.util.Date;

public abstract class RunInfo {

	public abstract String getProgram();
	public abstract Date getStarted();
	public abstract Date getStopped();
	public abstract String getHandle();

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass().equals(this.getClass())) {
			return ((RunInfo) o).getHandle().equals(getHandle());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getHandle().hashCode();
	}

	public RunData toRPC() {
		return new RunData(getProgram(), getStarted(), getStopped(), getHandle());
	}
}
