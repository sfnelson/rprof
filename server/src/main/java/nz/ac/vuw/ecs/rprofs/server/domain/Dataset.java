package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import com.google.gwt.user.client.rpc.IsSerializable;


@Entity
@Table(name = "profiler_runs")
@NamedQueries({
	@NamedQuery(name = "findDataset", query = "select D from Dataset D where D.handle = :handle"),
	@NamedQuery(name = "allDatasets", query = "select D from Dataset D"),
	@NamedQuery(name = "deleteDataset", query = "delete Dataset D where D = :dataset")
})
public class Dataset implements DataObject<Dataset, Short>, IsSerializable, Comparable<Dataset> {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Short id;

	@Version
	private Integer version;

	private String handle;
	private String program;
	private Date started;
	private Date stopped;

	public Dataset() {}

	public Dataset(String handle, Date started, Date stopped, String program) {
		this.handle = handle;
		this.started = started;
		this.stopped = stopped;
		this.program = program;
	}

	public Short getId() {
		return id;
	}

	public Long getRpcId() {
		return new Long(id);
	}

	public Integer getVersion() {
		return version;
	}

	public String getHandle() {
		return handle;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public Date getStarted() {
		return started;
	}

	public Date getStopped() {
		return stopped;
	}

	public void setStopped(Date time) {
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
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return id.equals(((Dataset) obj).id);
	}

	@Override
	public int hashCode() {
		return id;
	}
}
