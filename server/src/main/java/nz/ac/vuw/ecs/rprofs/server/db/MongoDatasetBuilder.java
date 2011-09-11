package nz.ac.vuw.ecs.rprofs.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager.DatasetBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;

import java.util.Date;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
abstract class MongoDatasetBuilder implements DatasetBuilder {

	BasicDBObject b;

	MongoDatasetBuilder() {
		b = new BasicDBObject();
	}

	@Override
	public DatasetBuilder setHandle(String handle) {
		b.append("handle", handle);
		return this;
	}

	@Override
	public DatasetBuilder setStarted(Date date) {
		b.append("started", date);
		return this;
	}

	@Override
	public DatasetBuilder setStopped(Date date) {
		b.append("stopped", date);
		return this;
	}

	@Override
	public DatasetBuilder setProgram(String program) {
		b.append("program", program);
		return this;
	}

	@Override
	public DataSetId store() {
		short id = _getId();
		b.append("_id", id);
		_store(b);
		b = new BasicDBObject();
		return new DataSetId(id);
	}

	public abstract short _getId();

	public abstract void _store(DBObject dataset);
}
