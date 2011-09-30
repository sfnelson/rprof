package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.util.InstanceCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.InstanceQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.InstanceUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public abstract class MongoInstanceBuilder extends MongoBuilder<MongoInstanceBuilder, InstanceId, Instance>
		implements InstanceCreator<MongoInstanceBuilder>, InstanceUpdater<MongoInstanceBuilder>,
		InstanceQuery<MongoInstanceBuilder> {

	@Override
	public MongoInstanceBuilder init(Instance value) {
		reset();
		setId(value.getId());
		if (value.getType() != null) setType(value.getType());
		if (value.getConstructor() != null) setConstructor(value.getConstructor());
		if (value.getConstructorReturn() != null) setConstructorReturn(value.getConstructorReturn());
		if (value.getFirstEquals() != null) setFirstEquals(value.getFirstEquals());
		if (value.getFirstHashCode() != null) setFirstHashCode(value.getFirstHashCode());
		for (Instance.FieldInfo info : value.getFields().values()) {
			addFieldInfo(info);
		}
		return this;
	}

	@Override
	public MongoInstanceBuilder setId(InstanceId id) {
		b.append("_id", id.getValue());
		return this;
	}

	@Override
	public MongoInstanceBuilder setType(ClazzId type) {
		b.append("type", type.getValue());
		return this;
	}

	@Override
	public MongoInstanceBuilder setConstructor(MethodId constructor) {
		b.append("constructor", constructor.getValue());
		return this;
	}

	@Override
	public MongoInstanceBuilder setConstructorReturn(EventId constructorReturn) {
		b.append("constructorReturn", constructorReturn.getValue());
		return this;
	}

	@Override
	public MongoInstanceBuilder setFirstEquals(EventId equals) {
		b.append("firstEquals", equals.getValue());
		return this;
	}

	@Override
	public MongoInstanceBuilder setFirstHashCode(EventId hashCode) {
		b.append("firstHashCode", hashCode.getValue());
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public MongoInstanceBuilder addFieldInfo(Instance.FieldInfo info) {
		BasicDBObject i = new BasicDBObject("_id", info.getId().getValue());
		i.append("name", info.getName());
		i.append("reads", info.getReads());
		if (info.getReads() > 0) {
			i.append("firstRead", info.getFirstRead().getValue());
			i.append("lastRead", info.getLastRead().getValue());
		}
		i.append("writes", info.getWrites());
		if (info.getWrites() > 0) {
			i.append("firstWrite", info.getFirstWrite().getValue());
			i.append("lastWrite", info.getLastWrite().getValue());
		}

		List<BasicDBObject> fields;
		if (b.containsField("fields")) {
			fields = (List<BasicDBObject>) b.get("fields");
		} else {
			fields = Lists.newArrayList();
			b.put("fields", fields);
		}
		fields.add(i);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	Instance get() {
		assert (b.containsField("_id"));
		InstanceId id = new InstanceId((Long) b.get("_id"));

		Instance i = new Instance(id);

		if (b.containsField("type")) {
			i.setType(new ClazzId((Long) b.get("type")));
		}

		if (b.containsField("constructor")) {
			i.setType(new ClazzId((Long) b.get("constructor")));
		}

		if (b.containsField("constructorReturn")) {
			i.setConstructorReturn(new EventId((Long) b.get("constructorReturn")));
		}

		if (b.containsField("firstEquals")) {
			i.setFirstEquals(new EventId((Long) b.get("firstEquals")));
		}

		if (b.containsField("firstHashCode")) {
			i.setFirstHashCode(new EventId((Long) b.get("firstHashCode")));
		}

		if (b.containsField("fields")) {
			for (DBObject field : (List<DBObject>) b.get("fields")) {
				FieldId fieldId = new FieldId((Long) field.get("_id"));
				String name = (String) field.get("name");
				Instance.FieldInfo f = new Instance.FieldInfo(fieldId, name);
				f.setReads((Integer) field.get("reads"));
				if (f.getReads() > 0) {
					f.setFirstRead(new EventId((Long) field.get("firstRead")));
					f.setLastRead(new EventId((Long) field.get("lastRead")));
				}
				f.setWrites((Integer) field.get("writes"));
				if (f.getWrites() > 0) {
					f.setFirstWrite(new EventId((Long) field.get("firstWrite")));
					f.setLastWrite(new EventId((Long) field.get("lastWrite")));
				}
				i.addFieldInfo(fieldId, f);
			}
		}

		return i;
	}
}
