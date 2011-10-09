package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.MethodIdProxy;
import nz.ac.vuw.ecs.rprofs.server.data.MethodManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

@ProxyFor(value = Method.class, locator = MethodManager.class)
public interface MethodProxy extends EntityProxy {

	public MethodIdProxy getId();

	public String getName();

	public String getDescription();

	public int getAccess();

	public ClazzIdProxy getOwner();

}
