package nz.ac.vuw.ecs.rprofs.client.request.id;

import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.ValueProxy;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 13/09/11
 */
@ProxyFor(EventId.class)
public interface EventIdProxy extends ValueProxy, HasId, HasDataset {

	long getEventIndex();

	short getEventUpper();

	int getEventLower();

}
