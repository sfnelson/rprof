/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceEvent extends GwtEvent<InstanceHandler> {

	private static final Type<InstanceHandler> TYPE = new Type<InstanceHandler>();
	
	private final InstanceData info;
	
	public InstanceEvent(InstanceData result) {
		this.info = result;
	}
	
	public InstanceData getValue() {
		return info;
	}
	
	@Override
	protected void dispatch(InstanceHandler handler) {
		handler.onInstanceEvent(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<InstanceHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<InstanceHandler> getType() {
		return TYPE;
	}

}