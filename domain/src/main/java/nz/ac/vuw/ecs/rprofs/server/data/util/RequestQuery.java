package nz.ac.vuw.ecs.rprofs.server.data.util;

import nz.ac.vuw.ecs.rprofs.server.domain.Request;
import nz.ac.vuw.ecs.rprofs.server.domain.id.RequestId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/05/12
 */
public interface RequestQuery<Q extends RequestQuery<Q>> extends Query<RequestId, Request>, RequestBuilder<Q> {
}
