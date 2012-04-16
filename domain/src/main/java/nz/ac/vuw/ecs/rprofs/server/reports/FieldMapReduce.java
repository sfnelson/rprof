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

			boolean isDeclaredFinal = (field != null) && field.isFinal();
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

			if (isDeclaredFinal && !isStationary) {
				//System.out.println(field + " is declared final but not stationary");
				//assert false;
			}

			if (isDeclaredFinal && !isConstructed) {
				//System.out.println(field + " is declared final but not constructed");
				//assert false;
			}

			if (isDeclaredFinal && !isFinal) {
				//System.out.println(field + " is declared final but not final");
				//assert false;
			}

			emitter.emit(id, new FieldSummary(id, packageName, name, description,
					isDeclaredFinal, isStationary, isConstructed, isFinal,
					1, info.getReads(), info.getWrites()));
		}
	}

	@Override
	public FieldSummary reduce(FieldSummaryId id, Iterable<FieldSummary> values) {
		String packageName = null;
		String name = null;
		String description = null;
		boolean isDeclaredFinal = true;
		boolean isStationary = true;
		boolean isConstructed = true;
		boolean isFinal = true;
		int instances = 0;
		long reads = 0;
		long writes = 0;

		for (FieldSummary summary : values) {
			packageName = summary.getPackageName();
			name = summary.getName();
			description = summary.getDescription();
			isDeclaredFinal = summary.isDeclaredFinal();
			isStationary &= summary.isStationary();
			isConstructed &= summary.isConstructed();
			isFinal &= summary.isFinal();
			instances += summary.getInstances();
			reads += summary.getReads();
			writes += summary.getWrites();
		}

		return new FieldSummary(id, packageName, name, description,
				isDeclaredFinal, isStationary, isConstructed, isFinal,
				instances, reads, writes);
	}
}
