package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface MethodBuilder<M extends MethodBuilder<M>> extends Builder<M, MethodId, Method> {
	M setName(String name);

	M setDescription(String name);

	M setAccess(int access);

	M setOwner(ClazzId owner);

	M setOwnerName(String owner);
}
