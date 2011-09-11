package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager.EventBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
abstract class MongoEventBuilder implements EventBuilder {

	@VisibleForTesting
	BasicDBObject b;

	@VisibleForTesting
	List<Long> args;

	MongoEventBuilder() {
		b = new BasicDBObject();
		args = Lists.newArrayList();
	}

	@Override
	public EventBuilder setId(@NotNull EventId id) {
		b.append("_id", id.longValue());
		return this;
	}

	@Override
	public EventBuilder setThread(ObjectId thread) {
		if (thread != null) b.append("thread", thread.longValue());
		return this;
	}

	@Override
	public EventBuilder setEvent(int event) {
		b.append("event", event);
		return this;
	}

	@Override
	public EventBuilder setClazz(ClassId clazz) {
		if (clazz != null) b.append("class", clazz.longValue());
		return this;
	}

	@Override
	public EventBuilder setMethod(MethodId method) {
		if (method != null) b.append("method", method.longValue());
		return this;
	}

	@Override
	public EventBuilder setField(FieldId field) {
		if (field != null) b.append("field", field.longValue());
		return this;
	}

	@Override
	public EventBuilder addArg(ObjectId arg) {
		args.add(arg == null ? null : arg.longValue());
		return this;
	}

	@Override
	public void store() {
		if (!args.isEmpty()) {
			b.append("args", Lists.newArrayList(args));
		}

		_store(b);

		b = new BasicDBObject();
		args.clear();
	}

	abstract void _store(DBObject event);
}
