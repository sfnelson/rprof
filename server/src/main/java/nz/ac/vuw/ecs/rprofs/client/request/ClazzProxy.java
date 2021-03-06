package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;

@ProxyFor(value = Clazz.class, locator = ClassManager.class)
public interface ClazzProxy extends EntityProxy {

	public ClazzIdProxy getId();

	public String getName();

	public ClazzIdProxy getParent();

	public String getParentName();

	public int getProperties();

}
