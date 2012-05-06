package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Request;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
public interface RequestUpdater<U extends RequestUpdater<U>> extends Updater<U, RequestId, Request>, RequestBuilder<U> {
}
