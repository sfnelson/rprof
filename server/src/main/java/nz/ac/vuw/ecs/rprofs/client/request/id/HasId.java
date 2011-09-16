package nz.ac.vuw.ecs.rprofs.client.request.id;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public interface HasId {
	long getValue();

	void setValue(long value);
}
