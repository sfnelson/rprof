package nz.ac.vuw.ecs.rprofs.server.data.util;

import java.util.Map;

import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassSummaryId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public interface ClassSummaryBuilder<I extends ClassSummaryBuilder<I>> extends Builder<I, ClassSummaryId, ClassSummary> {

	I setId(ClassSummaryId id);

	I setClassName(String className);

	I setPackageName(String packageName);

	I setNumObjects(int numObjects);

	I setNumFullyImmutable(int numFullyImmutable);

	I setNumFullyMutable(int numFullyMutable);

	I setEqCol(int[] eqcol);

	I setEq(int[] eq);

	I setCol(int[] col);

	I setNone(int[] none);

	I setFields(Map<FieldId, ClassSummary.FieldInfo> fields);
}
