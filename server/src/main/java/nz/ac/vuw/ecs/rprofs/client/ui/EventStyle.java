package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.resources.client.CssResource;

public interface EventStyle extends CssResource {
	public String objectAllocated();
	public String arrayAllocated();
	public String methodEnter();
	public String methodExit();
	public String methodException();
	public String fieldRead();
	public String fieldWrite();
	public String classWeave();
	public String classInit();
	public String objectTagged();
	public String objectFreed();

	public String thread();
	public String event();
	public String type();
	public String deref();
	public String method();
	public String field();
	public String args();
	public String hidden();
}
