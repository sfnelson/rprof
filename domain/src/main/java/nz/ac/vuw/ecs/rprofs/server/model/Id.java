package nz.ac.vuw.ecs.rprofs.server.model;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 12/09/11
 */
public interface Id<I extends Id<I, T>, T extends DataObject<I, T>> {

	Class<T> getTargetClass();

	long getValue();

	void setValue(long value);

}
