package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

public class Dataset implements DataObject<DatasetId, Dataset>, Comparable<Dataset> {

	@NotNull
	private DatasetId id;

	@NotNull
	private Integer version;

	@NotNull
	private String benchmark;

	@Nullable
	private String handle;

	@NotNull
	private Date started;

	@Nullable
	private Date stopped;

	public Dataset() {
	}

	public Dataset(@NotNull DatasetId id, @NotNull String benchmark, @NotNull Date started, @NotNull String handle) {
		this.id = id;
		this.version = 0;
		this.benchmark = benchmark;
		this.handle = handle;
		this.started = started;
	}

	public Dataset(@NotNull DatasetId id, @NotNull Integer version, @NotNull String benchmark, @NotNull Date started,
				   @NotNull String handle) {
		this.id = id;
		this.version = version;
		this.benchmark = benchmark;
		this.handle = handle;
		this.started = started;
	}

	@NotNull
	@Override
	public DatasetId getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	@NotNull
	public String getDatasetHandle() {
		return handle;
	}

	@NotNull
	public String getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(@Nullable String benchmark) {
		this.benchmark = benchmark;
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
