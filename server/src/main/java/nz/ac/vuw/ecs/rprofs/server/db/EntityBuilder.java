package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import org.bson.BSONObject;

import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public interface EntityBuilder<T extends DataObject<?, ?>> {

	EntityBuilder<T> init(@NotNull BSONObject init);

	T get();

}
