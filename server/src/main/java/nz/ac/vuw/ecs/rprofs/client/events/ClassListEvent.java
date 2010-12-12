/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.ArrayList;
import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.ClassData;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ClassListEvent extends GwtEvent<ClassListHandler> {

	private static final Type<ClassListHandler> TYPE = new Type<ClassListHandler>();
	
	private final List<ClassData> classes;
	
	public ClassListEvent(ArrayList<ClassData> classes) {
		this.classes = Collections.immutable(classes);
	}
	
	@Override
	protected void dispatch(ClassListHandler handler) {
		handler.onClassListAvailable(classes);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ClassListHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<ClassListHandler> getType() {
		return TYPE;
	}

}
