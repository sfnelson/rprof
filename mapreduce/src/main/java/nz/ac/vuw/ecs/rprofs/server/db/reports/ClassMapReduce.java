package nz.ac.vuw.ecs.rprofs.server.db.reports;

import java.util.Set;

import com.google.common.collect.Sets;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClazzQuery;
import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassSummaryId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class ClassMapReduce implements MapReduceFinish<Instance, ClassSummaryId, ClassSummary> {

	private final ClazzQuery<?> classes;

	public ClassMapReduce(ClazzQuery<?> classes) {
		this.classes = classes;
	}

	@Override
	public void map(Instance instance, Emitter<ClassSummaryId, ClassSummary> emitter) {
		ClazzId type = instance.getType();

		EventId constructor = instance.getConstructorReturn();

		Set<FieldId> mutable = Sets.newHashSet();
		EventId firstRead = null;
		EventId lastWrite = null;

		boolean isFullyImmutable = true;
		boolean isFullyMutable = true;

		for (FieldId fid : instance.getFields().keySet()) {
			Instance.FieldInfo field = instance.getFields().get(fid);
			EventId read = field.getFirstRead();
			if (read != null) {
				if (firstRead == null || firstRead.after(read)) {
					firstRead = read;
				}
			}
			EventId write = field.getLastWrite();
			if (write != null) {
				if (lastWrite == null || lastWrite.before(write)) {
					lastWrite = write;
				}
			}
			if (read != null && write != null && read.before(write)) {
				mutable.add(fid);
				isFullyImmutable = false;
			} else {
				isFullyMutable = false;
			}
		}

		if (isFullyImmutable) isFullyMutable = false;

		EventId equals = instance.getFirstEquals();
		if (equals == null) equals = instance.getFirstHashCode();
		else if (instance.getFirstHashCode() != null && equals.after(instance.getFirstHashCode())) {
			equals = instance.getFirstHashCode();
		}

		EventId collection = instance.getFirstCollection();

		ClassSummaryId id = new ClassSummaryId(type);
		ClassSummary result = new ClassSummary(id, isFullyImmutable, isFullyMutable,
				lastWrite, constructor, firstRead, equals, collection,
				instance.getFields(), mutable);
		emitter.store(id, result);
	}

	@Override
	public ClassSummary reduce(ClassSummaryId id, ClassSummary o1, ClassSummary o2) {
		o1.append(o2);
		return o1;
	}

	@Override
	public void finish(ClassSummaryId id, ClassSummary value) {
		if (id == null) return;
		ClazzId cid = new ClazzId(id.getValue());
		Clazz clazz = classes.find(cid);
		String className = clazz != null ? clazz.getName() : null;
		if (className != null) {
			className = className.replace('/', '.');
		}
		String packageName = null;
		if (className != null && className.contains(".")) {
			packageName = className.substring(0, className.lastIndexOf('.'));
		}

		value.setClassName(className);
		value.setPackageName(packageName);
	}
}
