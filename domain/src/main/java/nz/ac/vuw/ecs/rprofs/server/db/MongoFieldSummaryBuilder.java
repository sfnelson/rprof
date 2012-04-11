package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.FieldSummaryCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldSummaryQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldSummaryUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public abstract class MongoFieldSummaryBuilder extends MongoBuilder<MongoFieldSummaryBuilder, FieldSummaryId, FieldSummary>
		implements FieldSummaryCreator<MongoFieldSummaryBuilder>, FieldSummaryQuery<MongoFieldSummaryBuilder>, FieldSummaryUpdater<MongoFieldSummaryBuilder> {

	@Override
	public MongoFieldSummaryBuilder init(FieldSummary value) {
		reset();
		setId(value.getId());
		setPackageName(value.getPackageName());
		setName(value.getName());
		setDescription(value.getDescription());
		setFinal(value.isFinal());
		setStationary(value.isStationary());
		setConstructed(value.isConstructed());
		setInstances(value.getInstances());
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setId(FieldSummaryId id) {
		b.put("_id", id.getValue());
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setName(String name) {
		b.put("name", name);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setDescription(String description) {
		b.put("description", description);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setPackageName(String name) {
		b.put("package", name);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setFinal(boolean isFinal) {
		b.put("final", isFinal);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setStationary(boolean isStationary) {
		b.put("stationary", isStationary);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setConstructed(boolean isConstructed) {
		b.put("constructed", isConstructed);
		return this;
	}

	@Override
	public MongoFieldSummaryBuilder setInstances(int instances) {
		b.put("instances", instances);
		return this;
	}

	@Override
	public FieldSummary get() {
		FieldSummaryId id = new FieldSummaryId((Long) b.get("_id"));
		String packageName = b.getString("package");
		String name = b.getString("name");
		String description = b.getString("description");
		boolean isFinal = b.getBoolean("final");
		boolean isStationary = b.getBoolean("stationary");
		boolean isConstructed = b.getBoolean("constructed");
		int instances = b.getInt("instances");
		return new FieldSummary(id, packageName, name, description, isFinal, isStationary, isConstructed, instances);
	}
}
