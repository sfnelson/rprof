package nz.ac.vuw.ecs.rprofs.server.reports;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.weaving.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
@Configurable
public class InstanceMapReduce implements MapReduce<Event, InstanceId, Instance> {

	@VisibleForTesting
	@Autowired
	Context context;

	@VisibleForTesting
	@Autowired
	ClassManager classes;

	private final Dataset dataset;

	public InstanceMapReduce(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public void map(Event e, Emitter<InstanceId, Instance> emitter) {
		List<InstanceId> args = e.getArgs();
		if (args == null || args.isEmpty() || e.getFirstArg() == null || e.getFirstArg().getValue() == 0l) return;

		InstanceId id = e.getFirstArg();
		Field field = null;
		Method method = null;
		context.setDataset(dataset);
		if (e.getField() != null) {
			field = classes.getField(e.getField());
		} else if (e.getMethod() != null) {
			method = classes.getMethod(e.getMethod());
		}
		context.clear();

		Instance result = new Instance(id);
		Instance.FieldInfo info;
		switch (e.getEvent()) {
			case Event.FIELD_READ:
				info = new Instance.FieldInfo(field);
				info.setReads(1);
				info.setFirstRead(e.getId());
				info.setLastRead(e.getId());
				result.addFieldInfo(field.getId(), info);
				break;
			case Event.FIELD_WRITE:
				info = new Instance.FieldInfo(field);
				info.setWrites(1);
				info.setFirstWrite(e.getId());
				info.setLastWrite(e.getId());
				result.addFieldInfo(field.getId(), info);
				break;
			case Event.METHOD_RETURN:
			case Event.METHOD_EXCEPTION:
				if (MethodUtils.isInit(method)) {
					result.setType(method.getOwner());
					result.setConstructor(method.getId());
				}
				break;
			case Event.CLASS_INITIALIZED:
				return; // don't care about classes.
			case Event.OBJECT_TAGGED:
				result.setType(e.getClazz());
				break;
			case Event.METHOD_ENTER:
				if (MethodUtils.isEquals(method)) {
					result.setFirstEquals(e.getId());
				} else if (MethodUtils.isHashCode(method)) {
					result.setFirstHashCode(e.getId());
				}
				break;
		}

		emitter.emit(id, result);
	}

	@Override
	public Instance reduce(Long id, List<Instance> values) {
		Instance result = new Instance(new InstanceId(id));

		ClazzId type = null;
		MethodId constructor = null;
		EventId constructorReturn = null;

		EventId firstEquals = null;
		EventId firstHashCode = null;

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
		for (Instance.FieldInfo info : fields.values()) {
			result.addFieldInfo(info.getId(), info);
		}

		return result;
	}
}