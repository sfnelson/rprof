package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.views.DatasetListView;
import nz.ac.vuw.ecs.rprofs.client.views.ProfilerAppView;
import nz.ac.vuw.ecs.rprofs.client.views.impl.DatasetPanel;
import nz.ac.vuw.ecs.rprofs.client.views.impl.InspectorWidget;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class ProfilerModule extends AbstractGinModule {
	@Override
	protected void configure() {
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);

		bind(ProfilerAppView.class).to(InspectorWidget.class).in(Singleton.class);
		bind(DatasetListView.class).to(DatasetPanel.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public PlaceController getPlaceController(EventBus bus) {
		return new PlaceController(bus);
	}

	@Provides
	@Singleton
	public RequestFactory getRequestFactory(EventBus bus) {
		RequestFactory rf = GWT.create(RequestFactory.class);
		rf.initialize(bus);
		return rf;
	}

	@Provides
	public DatasetRequest getDatasetRequest(RequestFactory rf) {
		return rf.datasetRequest();
	}

	@Provides
	public ClassRequest getClassRequest(RequestFactory rf) {
		return rf.classRequest();
	}

	@Provides
	public MethodRequest getMethodRequest(RequestFactory rf) {
		return rf.methodRequest();
	}

	@Provides
	public FieldRequest getFieldRequest(RequestFactory rf) {
		return rf.fieldRequest();
	}

	@Provides
	public InstanceRequest getInstanceRequest(RequestFactory rf) {
		return rf.instanceRequest();
	}
}
