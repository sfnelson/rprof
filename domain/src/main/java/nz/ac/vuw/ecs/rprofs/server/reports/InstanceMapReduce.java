package nz.ac.vuw.ecs.rprofs.server.reports;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import nz.ac.vuw.ecs.rprofs.Context;
import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
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
					result.setType(e.getClazz());
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
					for (int i = 1; i < argList.size(); i++) { // skip this
						id = argList.get(i);
						if (id != null && id.getValue() != 0) {
							result = new Instance(id);
							result.setFirstCollection(e.getId());
							emitter.emit(id, result);
						}
					}
					return;
				}
				break;
		}

		emitter.emit(id, result);
	}

	@Override
	public Instance reduce(InstanceId id, Iterable<Instance> values) {
		Instance result = new Instance(id);

		ClazzId type = null;
		MethodId constructor = null;
		EventId constructorReturn = null;

		EventId firstEquals = null;
		EventId firstHashCode = null;
		EventId firstCollection = null;

		Map<FieldId, Instance.FieldInfo> fields = Maps.newHashMap();

		for (Instance i : values) {
			if (i.getType() != null && i.getConstructor() == null) {
				// type set with tag event
				type = i.getType();
			}
			if (i.getConstructorReturn() != null) {
				if (constructorReturn == null || i.getConstructorReturn().after(constructorReturn)) {
					type = i.getType();
					constructor = i.getConstructor();
					constructorReturn = i.getConstructorReturn();
				}
			}
			if (i.getFirstEquals() != null) {
				if (firstEquals == null || i.getFirstEquals().before(firstEquals)) {
					firstEquals = i.getFirstEquals();
				}
			}
			if (i.getFirstHashCode() != null) {
				if (firstHashCode == null || i.getFirstHashCode().before(firstHashCode)) {
					firstHashCode = i.getFirstHashCode();
				}
			}
			if (i.getFirstCollection() != null) {
				if (firstCollection == null || i.getFirstCollection().before(firstCollection)) {
					firstCollection = i.getFirstCollection();
				}
			}

			for (Instance.FieldInfo field : i.getFields().values()) {
				if (!fields.containsKey(field.getId())) {
					fields.put(field.getId(), field);
				} else {
					Instance.FieldInfo toMerge = fields.get(field.getId());
					if (field.getReads() > 0) {
						if (toMerge.getReads() == 0 || field.getFirstRead().before(toMerge.getFirstRead())) {
							toMerge.setFirstRead(field.getFirstRead());
						}
						if (toMerge.getReads() == 0 || field.getLastRead().after(toMerge.getLastRead())) {
							toMerge.setLastRead(field.getLastRead());
						}
						toMerge.setReads(toMerge.getReads() + field.getReads());
					}
					if (field.getWrites() > 0) {
						if (toMerge.getWrites() == 0 || field.getFirstWrite().before(toMerge.getFirstWrite())) {
							toMerge.setFirstWrite(field.getFirstWrite());
						}
						if (toMerge.getWrites() == 0 || field.getLastWrite().after(toMerge.getLastWrite())) {
							toMerge.setLastWrite(field.getLastWrite());
						}
						toMerge.setWrites(toMerge.getWrites() + field.getWrites());
					}
				}
			}
		}

		if (type != null) result.setType(type);
		if (constructor != null) result.setConstructor(constructor);
		if (constructorReturn != null) result.setConstructorReturn(constructorReturn);
		if (firstEquals != null) result.setFirstEquals(firstEquals);
		if (firstHashCode != null) result.setFirstHashCode(firstHashCode);
		if (firstCollection != null) result.setFirstCollection(firstCollection);
		for (Instance.FieldInfo info : fields.values()) {
			result.addFieldInfo(info.getId(), info);
		}

		return result;
	}
}