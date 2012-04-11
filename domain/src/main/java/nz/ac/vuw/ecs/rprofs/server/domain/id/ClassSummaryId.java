package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public class ClassSummaryId extends Id<ClassSummaryId, ClassSummary> {

	public ClassSummaryId(ClazzId clazzId) {
		super(clazzId != null ? clazzId.getValue() : 0l);
	}

	public ClassSummaryId(long value) {
		super(value);
	}

	public ClazzId getClazzId() {
		return new ClazzId(getValue());
	}

	@Override
	public Class<ClassSummary> getTargetClass() {
		return ClassSummary.class;
	}
}
