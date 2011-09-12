package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager.EventBuilder;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
abstract class MongoEventBuilder implements EventBuilder, EntityBuilder<Event> {

	@VisibleForTesting
	BasicDBObject b;

	@VisibleForTesting
	List<Long> args;

	MongoEventBuilder() {
		b = new BasicDBObject();
		args = Lists.newArrayList();
	}

	@Override
	public MongoEventBuilder init(BSONObject values) {
		b.putAll(values);
		return this;
	}

	@Override
	public MongoEventBuilder setId(@NotNull EventId id) {
		b.append("_id", id.longValue());
		return this;
	}

	@Override
	public MongoEventBuilder setThread(InstanceId thread) {
		if (thread != null) b.append("thread", thread.longValue());
		return this;
	}

	@Override
	public MongoEventBuilder setEvent(int event) {
		b.append("event", event);
		return this;
	}

	@Override
	public MongoEventBuilder setClazz(ClazzId clazz) {
		if (clazz != null) b.append("class", clazz.longValue());
		return this;
	}

	@Override
	public MongoEventBuilder setMethod(MethodId method) {
		if (method != null) b.append("method", method.longValue());
		return this;
	}

	@Override
	public MongoEventBuilder setField(FieldId field) {
		if (field != null) b.append("field", field.longValue());
		return this;
	}

	@Override
	public MongoEventBuilder addArg(InstanceId arg) {
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

	@Override
	@SuppressWarnings("unchecked")
	public Event get() {
		EventId id = new EventId((Long) b.get("_id"));
		int type = (Integer) b.get("event");
		Event event = new Event(id, type);
		if (b.containsField("thread")) {
			Long threadRaw = (Long) b.get("thread");
			if (threadRaw != null) {
				InstanceId thread = new InstanceId(threadRaw);
				event.setThread(thread);
			}
		}
		if (b.containsField("class")) {
			Long classRaw = (Long) b.get("class");
			if (classRaw != null) {
				ClazzId clazz = new ClazzId(classRaw);
				event.setClazz(clazz);
			}
		}
		if (b.containsField("method")) {
			Long methodRaw = (Long) b.get("method");
			if (methodRaw != null) {
				MethodId method = new MethodId(methodRaw);
				event.setAttribute(method);
			}
		}
		if (b.containsField("field")) {
			Long fieldRaw = (Long) b.get("field");
			if (fieldRaw != null) {
				FieldId field = new FieldId(fieldRaw);
				event.setAttribute(field);
			}
		}
		if (b.containsField("args")) {
			List<Long> in = (List<Long>) b.get("args");
			if (in != null && !in.isEmpty()) {
				List<InstanceId> out = Lists.newArrayList();
				for (Long arg : in) {
					if (arg != null) out.add(new InstanceId(arg));
					else out.add(null);
				}
				event.setArgs(out);
			}
		}
		return event;
	}

	abstract void _store(DBObject event);
}
