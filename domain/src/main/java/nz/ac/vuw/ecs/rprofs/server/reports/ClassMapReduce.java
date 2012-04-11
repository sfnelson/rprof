package nz.ac.vuw.ecs.rprofs.server.reports;

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
public class ClassMapReduce implements MapReduce<Instance, ClassSummaryId, ClassSummary> {

	private final ClazzQuery<?> classes;

	public ClassMapReduce(ClazzQuery<?> classes) {
		this.classes = classes;
	}

	@Override
	public void map(Instance instance, Emitter<ClassSummaryId, ClassSummary> emitter) {
		ClazzId type = instance.getType();
		Clazz clazz = type != null ? classes.find(type) : null;
		String className = clazz != null ? clazz.getName() : null;
		if (className != null) {
			className = className.replace('/', '.');
		}
		String packageName = null;
		if (className != null && className.contains(".")) {
			packageName = className.substring(0, className.lastIndexOf('.'));
		}

		EventId constructor = instance.getConstructorReturn();

		Set<FieldId> mutable = Sets.newHashSet();
		EventId firstRead = null;
		EventId lastWrite = null;

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
			if (read != null && write != null) {
				if (read.before(write)) {
					mutable.add(fid);
				}
			}
		}

		EventId equals = instance.getFirstEquals();
		if (equals != null && instance.getFirstHashCode() != null
				&& equals.after(instance.getFirstHashCode())) {
			equals = instance.getFirstHashCode();
		}

		EventId collection = instance.getFirstCollection();

		ClassSummaryId id = new ClassSummaryId(type);
		ClassSummary result = new ClassSummary(id, className, packageName, lastWrite, constructor, firstRead,
				equals, collection, instance.getFields(), mutable);
		emitter.emit(id, result);
	}

	@Override
	public ClassSummary reduce(ClassSummaryId id, Iterable<ClassSummary> values) {
		ClassSummary result = new ClassSummary(id);

		for (ClassSummary r : values) {
			result.append(r);
		}

		return result;
	}
}
