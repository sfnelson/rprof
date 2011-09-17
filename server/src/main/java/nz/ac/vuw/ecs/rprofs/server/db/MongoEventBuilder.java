package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
abstract class MongoEventBuilder extends MongoBuilder<MongoEventBuilder, EventId, Event>
		implements EventCreator<MongoEventBuilder>, EventUpdater<MongoEventBuilder>, EventQuery<MongoEventBuilder> {

	@Override
	public MongoEventBuilder setId(@NotNull EventId id) {
		b.append("_id", id.getValue());
		return this;
	}

	@Override
	public MongoEventBuilder setThread(InstanceId thread) {
		if (thread != null) b.append("thread", thread.getValue());
		return this;
	}

	@Override
	public MongoEventBuilder setEvent(int event) {
		b.append("event", event);
		return this;
	}

	@Override
	public MongoEventBuilder setClazz(ClazzId clazz) {
		if (clazz != null) b.append("class", clazz.getValue());
		return this;
	}

	@Override
	public MongoEventBuilder setMethod(MethodId method) {
		if (method != null) b.append("method", method.getValue());
		return this;
	}

	@Override
	public MongoEventBuilder setField(FieldId field) {
		if (field != null) b.append("field", field.getValue());
		return this;
	}

	@Override
	public MongoEventBuilder setFilter(int filter) {
		b.append("$where", "(this.event & " + filter + ") != 0");
		return this;
	}

	@Override
	public MongoEventBuilder setWithArg(InstanceId instanceId) {
		if (instanceId != null) {
			b.append("args", instanceId.getValue());
		}
		return this;
	}

	@Override
	public MongoEventBuilder setBefore(EventId eventId) {
		if (eventId != null) {
			b.append("_id", new BasicDBObject("$lt", eventId.getValue()));
		}
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public MongoEventBuilder addArg(InstanceId arg) {
		List<Long> args;
		if (b.containsField("args")) {
			args = (List<Long>) b.get("args");
		} else {
			args = Lists.newArrayList();
		}
		List<Long> newArgs = Lists.newArrayList(args);
		newArgs.add(arg == null ? null : arg.getValue());
		b.put("args", newArgs);
		return this;
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
					if (arg == null || arg == 0) out.add(new InstanceId(0)); // todo should return null
					else out.add(new InstanceId(arg));
				}
				event.setArgs(out);
			}
		}
		return event;
	}
}
