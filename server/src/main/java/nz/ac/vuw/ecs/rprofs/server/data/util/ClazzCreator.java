package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface ClazzCreator<C extends ClazzCreator<C>> extends ClazzBuilder<C>, Creator<ClazzId, Clazz> {
	FieldCreator<?> addField();

	MethodCreator<?> addMethod();
}
