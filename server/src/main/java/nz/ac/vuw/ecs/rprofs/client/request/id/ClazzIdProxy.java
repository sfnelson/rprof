package nz.ac.vuw.ecs.rprofs.client.request.id;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.server.data.IdLocator;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
@ProxyFor(value = ClazzId.class, locator = IdLocator.class)
public interface ClazzIdProxy extends EntityProxy {
}
