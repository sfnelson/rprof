package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager.DatasetBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
abstract class MongoDatasetBuilder implements DatasetBuilder, EntityBuilder<Dataset> {

	BasicDBObject b;

	MongoDatasetBuilder() {
		b = new BasicDBObject();
	}

	@Override
	public MongoDatasetBuilder init(BSONObject init) {
		b.putAll(init);
		return this;
	}

	@Override
	@NotNull
	public MongoDatasetBuilder setHandle(String handle) {
		b.append("handle", handle);
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
	public MongoDatasetBuilder setProgram(String program) {
		b.append("program", program);
		return this;
	}

	@Override
	@NotNull
	public DatasetId store() {
		short id = _getId();
		b.append("_id", Long.valueOf(id));
		validate();
		_store(b);
		b = new BasicDBObject();
		return new DatasetId(id);
	}

	@Override
	@NotNull
	public Dataset get() {
		validate();
		DatasetId id = new DatasetId(((Long) b.get("_id")).shortValue());
		String handle = (String) b.get("handle");
		Date started = (Date) b.get("started");
		Dataset dataset = new Dataset(id, handle, started);
		if (b.containsField("stopped")) {
			dataset.setStopped((Date) b.get("stopped"));
		}
		if (b.containsField("program")) {
			dataset.setProgram((String) b.get("program"));
		}
		b = new BasicDBObject();
		return dataset;
	}

	private void validate() {
		assert (b.get("_id") != null);
		assert (b.get("handle") != null);
		assert (b.get("started") != null);
	}

	public abstract short _getId();

	public abstract void _store(DBObject dataset);
}
