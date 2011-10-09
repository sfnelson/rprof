package nz.ac.vuw.ecs.rprofs.client.request;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import nz.ac.vuw.ecs.rprofs.client.request.id.ClazzIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.FieldIdProxy;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;

@ProxyFor(value = Field.class)
public interface FieldProxy extends EntityProxy {

	public FieldIdProxy getId();

	public ClazzIdProxy getOwner();

	public String getName();

	public String getDescription();

	public int getAccess();
}
