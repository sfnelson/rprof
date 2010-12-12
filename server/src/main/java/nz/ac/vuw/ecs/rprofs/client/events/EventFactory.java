/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import nz.ac.vuw.ecs.rprofs.client.ErrorPanel;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public abstract class EventFactory<ReturnType> implements AsyncCallback<ReturnType> {
	
	private final HasHandlers source;
	private final String errorMessage;

	public EventFactory(HasHandlers source, String errorMessage) {
		this.source = source;
		this.errorMessage = errorMessage;
	}
	
	public abstract GwtEvent<? extends EventHandler> createEvent(ReturnType result);

	@Override
	public void onFailure(Throwable caught) {
		ErrorPanel.showMessage(errorMessage, caught);
	}

	@Override
	public void onSuccess(ReturnType result) {
		source.fireEvent(createEvent(result));
	}

}
