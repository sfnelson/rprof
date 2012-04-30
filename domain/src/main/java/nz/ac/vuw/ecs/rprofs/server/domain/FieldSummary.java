package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public class FieldSummary implements DataObject<FieldSummaryId, FieldSummary> {

	@NotNull
	private FieldSummaryId id;

	@Nullable
	private String packageName;

	@Nullable
	private String name;

	@Nullable
	private String description;

	private boolean isDeclaredFinal;

	private boolean isFinal;

	private boolean isStationary;

	private boolean isConstructed;

	private int instances;

	private long reads;

	private long writes;

	public FieldSummary(FieldSummaryId id, boolean isStationary, boolean isConstructed, boolean isFinal,
						int instances, long reads, long writes) {
		this.id = id;
		this.isFinal = isFinal;
		this.isStationary = isStationary;
		this.isConstructed = isConstructed;
		this.instances = instances;
		this.reads = reads;
		this.writes = writes;
	}

	@Override
	public FieldSummaryId getId() {
		return id;
	}

	public boolean isDeclaredFinal() {
		return isDeclaredFinal;
	}

	public void setDeclaredFinal(boolean declaredFinal) {
		isDeclaredFinal = declaredFinal;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public boolean isStationary() {
		return isStationary;
	}

	public void setStationary(boolean stationary) {
		isStationary = stationary;
	}

	public boolean isConstructed() {
		return isConstructed;
	}

	public void setConstructed(boolean constructed) {
		isConstructed = constructed;
	}

	public int getInstances() {
		return instances;
	}

	public void setInstances(int instances) {
		this.instances = instances;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	@Nullable
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(@Nullable String packageName) {
		this.packageName = packageName;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public long getReads() {
		return reads;
	}

	public void setReads(long reads) {
		this.reads = reads;
	}

	public long getWrites() {
		return writes;
	}

	public void setWrites(long writes) {
		this.writes = writes;
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		return id.equals(((FieldSummary) obj).id);
	}

	public int hashCode() {
		return FieldSummary.class.hashCode() ^ id.hashCode();
	}
}
