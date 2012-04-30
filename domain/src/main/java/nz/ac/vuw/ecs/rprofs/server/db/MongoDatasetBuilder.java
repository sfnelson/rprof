package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.Date;

import javax.validation.constraints.NotNull;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.DatasetUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
abstract class MongoDatasetBuilder extends MongoBuilder<MongoDatasetBuilder, DatasetId, Dataset>
		implements DatasetCreator<MongoDatasetBuilder>, DatasetUpdater<MongoDatasetBuilder>, DatasetQuery<MongoDatasetBuilder> {

	@Override
	@NotNull
	public MongoDatasetBuilder init(Dataset dataset) {
		b.append("_id", dataset.getId());
		b.append("version", dataset.getVersion());
		setBenchmark(dataset.getBenchmark());
		setStarted(dataset.getStarted());
		setDatasetHandle(dataset.getDatasetHandle());
		setNumEvents(dataset.getNumEvents());
		if (dataset.getStopped() != null) {
			setStopped(dataset.getStopped());
		}
		if (dataset.getFinished() != null) {
			setFinished(dataset.getFinished());
		}
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setBenchmark(String benchmark) {
		b.append("benchmark", benchmark);
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setStarted(Date date) {
		b.append("started", date);
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setStopped(Date date) {
		b.append("stopped", date);
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setFinished(Date date) {
		b.append("finished", date);
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setDatasetHandle(String handle) {
		b.append("handle", handle);
		return this;
	}

	@Override
	public MongoDatasetBuilder setNumEvents(long events) {
		b.append("events", events);
		return this;
	}

	@Override
	@NotNull
	public Dataset get() {
		Long lid = (Long) b.get("_id");
		if (lid == null) return null;
		DatasetId id = new DatasetId(lid.shortValue());
		int version = b.getInt("version");
		String benchmark = b.getString("benchmark");
		Date started = (Date) b.get("started");
		String handle = b.getString("handle");
		Dataset dataset = new Dataset(id, version, benchmark, started, handle);
		if (b.containsField("stopped")) {
			dataset.setStopped((Date) b.get("stopped"));
		}
		if (b.containsField("finished")) {
			dataset.setStopped((Date) b.get("finished"));
		}
		if (b.containsField("events")) {
			dataset.setNumEvents(b.getLong("events"));
		}
		reset();
		return dataset;
	}
}
