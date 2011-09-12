package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class Dataset implements DataObject<DatasetId, Dataset>, Comparable<Dataset> {

	@NotNull
	private DatasetId id;

	@NotNull
	private String handle;

	@Nullable
	private String program;

	@NotNull
	private Date started;

	@Nullable
	private Date stopped;

	public Dataset() {
	}

	public Dataset(@NotNull DatasetId id, @NotNull String handle, @NotNull Date started) {
		this.id = id;
		this.handle = handle;
		this.started = started;
	}

	@NotNull
	@Override
	public DatasetId getId() {
		return id;
	}

	@NotNull
	public String getHandle() {
		return handle;
	}

	@Nullable
	public String getProgram() {
		return program;
	}

	public void setProgram(@Nullable String program) {
		this.program = program;
	}

	@NotNull
	public Date getStarted() {
		return started;
	}

	@Nullable
	public Date getStopped() {
		return stopped;
	}

	public void setStopped(@Nullable Date time) {
		this.stopped = time;
	}

	@Override
	public int compareTo(Dataset o) {
		return started.compareTo(o.started);
	}

	@Override
	public String toString() {
		return handle;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Dataset dataset = (Dataset) o;

		return id.equals(dataset.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
