package nz.ac.vuw.ecs.rprofs.client.request.id;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
@ProxyFor(DatasetId.class)
public interface DatasetIdProxy extends ValueProxy, HasId, HasDataset {
}
