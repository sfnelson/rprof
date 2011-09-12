package nz.ac.vuw.ecs.rprofs.client.ui;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safecss.shared.SafeStyles;

public interface EventStyle extends CssResource {
	SafeStyles objectAllocated();

	SafeStyles arrayAllocated();

	SafeStyles methodEnter();

	SafeStyles methodExit();

	SafeStyles methodException();

	SafeStyles fieldRead();

	SafeStyles fieldWrite();

	SafeStyles classWeave();

	SafeStyles classInit();

	SafeStyles objectTagged();

	SafeStyles objectFreed();

	SafeStyles unknown();

	SafeStyles eventCell();

	SafeStyles thread();

	SafeStyles event();

	SafeStyles type();

	SafeStyles deref();

	SafeStyles method();

	SafeStyles field();

	SafeStyles args();

	SafeStyles hidden();

}
