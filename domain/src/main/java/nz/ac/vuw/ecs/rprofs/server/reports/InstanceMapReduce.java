package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
public class InstanceMapReduce implements MapReduce<Event, InstanceId, Instance> {

	private final Logger log = LoggerFactory.getLogger(InstanceMapReduce.class);

	private final Database database;

	private final Dataset dataset;

	public InstanceMapReduce(Dataset dataset, Database database) {
		this.dataset = dataset;
		this.database = database;
	}

	@Override
	public void map(Event e, Emitter<InstanceId, Instance> emitter) {
		List<InstanceId> args = e.getArgs();

		// we used to return if ID was 0, but it's useful to know what happens to untagged objects
		if (args == null || args.isEmpty()) return;

		InstanceId id = e.getFirstArg();

		Context.setDataset(dataset);
		Clazz clazz = null;
		Method method = null;
		if (e.getMethod() != null) {
			method = database.findEntity(e.getMethod());
			if (method != null) {
				clazz = database.findEntity(method.getOwner());
			}
		}
		Context.clear();

		Instance result = new Instance(id);
		Instance.FieldInfo info;
		switch (e.getEvent()) {
			case Event.FIELD_READ:
				info = new Instance.FieldInfo(e.getField());
				info.setReads(1);
				info.setFirstRead(e.getId());
				info.setLastRead(e.getId());
				result.addFieldInfo(e.getField(), info);
				break;
			case Event.FIELD_WRITE:
				info = new Instance.FieldInfo(e.getField());
				info.setWrites(1);
				info.setFirstWrite(e.getId());
				info.setLastWrite(e.getId());
				result.addFieldInfo(e.getField(), info);
				break;
			case Event.METHOD_RETURN:
			case Event.METHOD_EXCEPTION:
				if (MethodUtils.isInit(method)) {
					result.setConstructor(e.getMethod());
					result.setConstructorReturn(e.getId());
				}
				break;
			case Event.CLASS_INITIALIZED:
				Context.setDataset(dataset);
				database.getClazzUpdater()
						.setInitialized(true)
						.update(e.getClazz());
				Context.clear();
				break;
			case Event.OBJECT_TAGGED:
			case Event.OBJECT_ALLOCATED:
				result.setType(e.getClazz());
				break;
			case Event.METHOD_ENTER:
				assert method != null;
				if (MethodUtils.isEquals(method)) {
					result.setFirstEquals(e.getId());
				} else if (MethodUtils.isHashCode(method)) {
					result.setFirstHashCode(e.getId());
				} else if (clazz != null && (clazz.getProperties() & Clazz.COLLECTION) != 0) {
					List<InstanceId> argList = e.getArgs();
					// skip 'this', only one event per object
					Set<InstanceId> argSet = Sets.newHashSet(argList.subList(1, argList.size()));
					for (InstanceId i : argSet) {
						if (i != null && i.getValue() != 0) {
							result = new Instance(i);
							result.setFirstCollection(e.getId());
							emitter.store(i, result);
						}
					}
					return;
				}
				break;
		}

		emitter.store(id, result);
	}

	private static boolean setIfBefore(EventId a, EventId b) {
		if (b == null) return false; // nothing to do
		if (a == null) return true; // nothing to compare to
		assert (!a.equals(b));
		if (b.before(a)) return true;
		return false;
	}

	private static boolean setIfAfter(EventId a, EventId b) {
		if (b == null) return false; // nothing to do
		if (a == null) return true; // nothing to compare to
		assert (!a.equals(b));
		if (b.after(a)) return true;
		return false;
	}

	@Override
	public Instance reduce(InstanceId id, Iterable<Instance> values) {
		Instance result = new Instance(id);

		for (Instance i : values) {
			if (i.getType() != null) {
				assert (result.getType() == null); // type should only be set once per object
				result.setType(i.getType());
			}
			if (setIfAfter(result.getConstructorReturn(), i.getConstructorReturn())) {
				result.setConstructor(i.getConstructor());
				result.setConstructorReturn(i.getConstructorReturn());
			}
			if (setIfBefore(result.getFirstEquals(), i.getFirstEquals())) {
				result.setFirstEquals(i.getFirstEquals());
			}
			if (setIfBefore(result.getFirstHashCode(), i.getFirstHashCode())) {
				result.setFirstHashCode(i.getFirstHashCode());
			}
			if (setIfBefore(result.getFirstCollection(), i.getFirstCollection())) {
				result.setFirstCollection(i.getFirstCollection());
			}

			for (Instance.FieldInfo field : i.getFields().values()) {
				Instance.FieldInfo toMerge = result.getFieldInfo(field.getId());
				if (toMerge == null) {
					toMerge = field;
				} else {
					if (setIfBefore(toMerge.getFirstRead(), field.getFirstRead()))
						toMerge.setFirstRead(field.getFirstRead());
					if (setIfAfter(toMerge.getLastRead(), field.getLastRead()))
						toMerge.setLastRead(field.getLastRead());
					toMerge.addReads(field.getReads());

					if (setIfBefore(toMerge.getFirstWrite(), field.getFirstWrite()))
						toMerge.setFirstWrite(field.getFirstWrite());
					if (setIfAfter(toMerge.getLastWrite(), field.getLastWrite()))
						toMerge.setLastWrite(field.getLastWrite());
					toMerge.addWrites(field.getWrites());
				}
				result.addFieldInfo(toMerge.getId(), toMerge);
			}
		}

		return result;
	}
}