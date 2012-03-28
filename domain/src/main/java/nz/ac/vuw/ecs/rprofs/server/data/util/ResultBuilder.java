package nz.ac.vuw.ecs.rprofs.server.data.util;

import java.util.Map;

import nz.ac.vuw.ecs.rprofs.server.domain.Result;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ResultId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public interface ResultBuilder<I extends ResultBuilder<I>> extends Builder<I, ResultId, Result> {

	I setId(ResultId id);

	I setClassName(String className);

	I setPackageName(String packageName);

	I setNumObjects(int numObjects);

	I setTotals(int[] totals);

	I setCounts(int[] counts);

	I setFields(Map<FieldId, Result.FieldInfo> fields);
}
