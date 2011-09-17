package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.resources.client.CssResource;

public interface EventStyle extends CssResource {

	String objectAllocated();

	String arrayAllocated();

	String methodEnter();

	String methodExit();

	String methodException();

	String fieldRead();

	String fieldWrite();

	String classWeave();

	String classInit();

	String objectTagged();

	String objectFreed();

	String unknown();

	String eventCell();

	String thread();

	String event();

	String type();

	String deref();

	String method();

	String field();

	String args();

	String hidden();

}
