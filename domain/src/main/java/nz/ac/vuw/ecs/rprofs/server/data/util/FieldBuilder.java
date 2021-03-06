package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 14/09/11
 */
public interface FieldBuilder<F extends FieldBuilder<F>> extends Builder<F, FieldId, Field> {
	F setName(String name);

	F setDescription(String name);

	F setAccess(int access);

	F setOwner(ClazzId owner);

	F setOwnerName(String owner);
}
