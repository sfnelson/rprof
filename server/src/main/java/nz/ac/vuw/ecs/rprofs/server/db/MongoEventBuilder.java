package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;

import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class MongoEventBuilder implements EventManager.EventBuilder {

	private Dataset dataset;
	private EventId id;
	private ObjectId threadId;
	private Integer event;
	private ClassId classId;
	private MethodId methodId;
	private FieldId fieldId;
	private List<ObjectId> args = Lists.newArrayList();

	@Override
	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public EventManager.EventBuilder setDataSet(Dataset ds) {
		this.dataset = ds;
		return this;
	}

	@Override
	public EventId getId() {
		return id;
	}

	@Override
	public EventManager.EventBuilder setId(long id) {
		this.id = EventId.create(dataset, id);
		return this;
	}

	@Override
	public ObjectId getThread() {
		return threadId;
	}

	@Override
	public EventManager.EventBuilder setThread(long thread) {
		if (thread == 0) this.threadId = null;
		else this.threadId = ObjectId.create(dataset, thread);
		return this;
	}

	@Override
	public Integer getEvent() {
		return event;
	}

	@Override
	public EventManager.EventBuilder setEvent(int event) {
		this.event = event;
		return this;
	}

	@Override
	public ClassId getClazz() {
		return classId;
	}

	@Override
	public EventManager.EventBuilder setClazz(int cnum) {
		if (cnum == 0) this.classId = null;
		else this.classId = ClassId.create(dataset, cnum);
		return this;
	}

	@Override
	public MethodId getMethod() {
		return methodId;
	}

	@Override
	public EventManager.EventBuilder setMethod(short mnum) {
		if (mnum == 0) this.methodId = null;
		else this.methodId = MethodId.create(dataset, classId, mnum);
		this.fieldId = null;
		return this;
	}

	@Override
	public FieldId getField() {
		return fieldId;
	}

	@Override
	public EventManager.EventBuilder setField(short fnum) {
		if (fnum == 0) this.fieldId = null;
		else this.fieldId = FieldId.create(dataset,  classId, fnum);
		this.methodId = null;
		return this;
	}

	@Override
	public List<ObjectId> getArgs() {
		return args;
	}

	@Override
	public EventManager.EventBuilder clearArgs() {
		this.args.clear();
		return this;
	}

	@Override
	public EventManager.EventBuilder addArg(long arg) {
		if (arg == 0) this.args.add(null);
		else this.args.add(ObjectId.create(dataset, arg));
		return this;
	}

	public DBObject toDBObject() {
		BasicDBObjectBuilder b = new BasicDBObjectBuilder();
		b.add("_id", id.longValue());
		b.add("event", event);
		if (threadId != null) b.add("thread", threadId.longValue());
		if (classId != null) b.add("class", classId.longValue());
		if (methodId != null) b.add("method", methodId.longValue());
		if (fieldId != null) b.add("field", fieldId.longValue());
		if (!args.isEmpty()) {
			List<Long> args = Lists.newArrayList();
			for (ObjectId o: this.args) {
				args.add(o.longValue());
			}
			b.add("args", args);
		}
		return b.get();
	}
}
