/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ProfilerRunEvent extends GwtEvent<ProfilerRunHandler> {

	private static final Type<ProfilerRunHandler> TYPE = new Type<ProfilerRunHandler>();
	
	private final List<ProfilerRun> runs;
	
	public ProfilerRunEvent(ArrayList<ProfilerRun> runs) {
		this.runs = runs;
	}
	
	@Override
	protected void dispatch(ProfilerRunHandler handler) {
		handler.profilerRunsAvailable(runs);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ProfilerRunHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<ProfilerRunHandler> getType() {
		return TYPE;
	}

}
