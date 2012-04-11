package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.FieldSummary;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 10/04/12
 */
public class FieldSummaryId extends Id<FieldSummaryId, FieldSummary> {

	public FieldSummaryId(FieldId fieldId) {
		super(fieldId != null ? fieldId.getValue() : 0l);
	}

	public FieldSummaryId(long value) {
		super(value);
	}

	public FieldId getFieldId() {
		return new FieldId(getValue());
	}

	@Override
	public Class<FieldSummary> getTargetClass() {
		return FieldSummary.class;
	}
}
