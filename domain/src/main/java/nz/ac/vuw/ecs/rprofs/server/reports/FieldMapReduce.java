package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.util.FieldQuery;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public class FieldMapReduce implements MapReduce<Instance, FieldSummaryId, FieldSummary> {

	private final FieldQuery<?> fields;

	public FieldMapReduce(FieldQuery<?> fields) {
		this.fields = fields;
	}

	@Override
	public void map(Instance instance, Emitter<FieldSummaryId, FieldSummary> emitter) {

		for (Instance.FieldInfo info : instance.getFields().values()) {
			FieldSummaryId id = new FieldSummaryId(info.getId());

			Field field = fields.find(info.getId());
			String className = field != null ? field.getOwnerName() : null;
			if (className != null) {
				className = className.replace('/', '.');
			}
			String packageName = null;
			if (className != null && className.contains(".")) {
				packageName = className.substring(0, className.lastIndexOf('.'));
			}
			String name = field != null ? className + '.' + field.getName() : null;
			String description = field != null ? field.getDescription() : null;

			boolean isFinal = (field != null) && field.isFinal();
			EventId firstRead = info.getFirstRead();
			EventId lastWrite = info.getLastWrite();
			EventId constructor = instance.getConstructorReturn();
			boolean isStationary = firstRead == null || lastWrite == null || lastWrite.before(firstRead);
			boolean isConstructed = constructor != null && (lastWrite == null || lastWrite.before(constructor));

			emitter.emit(id, new FieldSummary(id, packageName, name, description, isFinal, isStationary, isConstructed, 1));
		}
	}

	@Override
	public FieldSummary reduce(FieldSummaryId id, Iterable<FieldSummary> values) {
		String packageName = null;
		String name = null;
		String description = null;
		boolean isFinal = true;
		boolean isStationary = true;
		boolean isConstructed = true;
		int instances = 0;

		for (FieldSummary summary : values) {
			packageName = summary.getPackageName();
			name = summary.getName();
			description = summary.getDescription();
			isFinal &= summary.isFinal();
			isStationary &= summary.isStationary();
			isConstructed &= summary.isConstructed();
			instances += summary.getInstances();
		}

		return new FieldSummary(id, packageName, name, description, isFinal, isStationary, isConstructed, instances);
	}
}
