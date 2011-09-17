package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.HasId;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 17/09/11
 */
public interface DataProvider {
	boolean hasThread(InstanceIdProxy thread);

	int getThreadIndex(InstanceIdProxy thread);

	int getNumThreads();

	<I extends HasId<T>, T extends EntityProxy> boolean hasEntity(I id);

	<I extends HasId<T>, T extends EntityProxy> T getEntity(I id);

}
