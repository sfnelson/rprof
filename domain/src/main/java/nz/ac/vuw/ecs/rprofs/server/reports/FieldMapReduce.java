package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.server.data.util.FieldQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.FieldSummaryUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldSummaryId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public class FieldMapReduce implements MapReduceFinish<Instance, FieldSummaryId, FieldSummary, FieldSummaryUpdater<?>> {

	private final FieldQuery<?> fields;

	public FieldMapReduce(FieldQuery<?> fields) {
		this.fields = fields;
	}

	@Override
	public void map(Instance instance, Emitter<FieldSummaryId, FieldSummary> emitter) {

		for (Instance.FieldInfo info : instance.getFields().values()) {
			FieldSummaryId id = new FieldSummaryId(info.getId());

			EventId firstRead = info.getFirstRead();
			EventId lastWrite = info.getLastWrite();
			EventId constructor = instance.getConstructorReturn();

			// no reads, no writes, or last write is before last read
			boolean isStationary = firstRead == null || lastWrite == null || lastWrite.before(firstRead);

			// no writes, or last write was before constructor ended.
			boolean isConstructed = (lastWrite == null)
					|| (constructor != null && lastWrite.before(constructor));

			boolean isFinal = (lastWrite == null)
					|| (constructor != null && lastWrite.before(constructor) && info.getWrites() <= 1);

			emitter.emit(id, new FieldSummary(id, isStationary, isConstructed, isFinal,
					1, info.getReads(), info.getWrites()));
		}
	}

	@Override
	public FieldSummary reduce(FieldSummaryId id, Iterable<FieldSummary> values) {
		boolean isStationary = true;
		boolean isConstructed = true;
		boolean isFinal = true;
		int instances = 0;
		long reads = 0;
		long writes = 0;

		for (FieldSummary summary : values) {
			isStationary &= summary.isStationary();
			isConstructed &= summary.isConstructed();
			isFinal &= summary.isFinal();
			instances += summary.getInstances();
			reads += summary.getReads();
			writes += summary.getWrites();
		}

		return new FieldSummary(id, isStationary, isConstructed, isFinal,
				instances, reads, writes);
	}

	@Override
	public void finish(FieldSummaryId id, FieldSummaryUpdater<?> updater) {
		FieldId fid = new FieldId(id.getValue());
		Field field = fields.find(fid);

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

		boolean isDeclaredFinal = (field != null) && field.isFinal();

		updater.setPackageName(packageName)
				.setName(name)
				.setDescription(description)
				.setDeclaredFinal(isDeclaredFinal)
				.update(id);
	}
}
