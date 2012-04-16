package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface ClazzBuilder<C extends ClazzBuilder<C>> extends Builder<C, ClazzId, Clazz> {
	C setName(String name);

	C setSimpleName(String name);

	C setPackageName(String name);

	C setParent(ClazzId parent);

	C setParentName(String name);

	C setProperties(int properties);

	C setAccess(int access);

	C setInitialized(boolean initialized);
}
