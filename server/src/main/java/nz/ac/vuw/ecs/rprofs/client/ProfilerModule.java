package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.gwt.client.RequestBatcher;
import nz.ac.vuw.ecs.rprofs.client.place.ProfilerPlaceFactory;
import nz.ac.vuw.ecs.rprofs.client.request.*;
import nz.ac.vuw.ecs.rprofs.client.views.*;
import nz.ac.vuw.ecs.rprofs.client.views.impl.*;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9/09/11
 */
public class ProfilerModule extends AbstractGinModule {
	@Override
	protected void configure() {
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);

		bind(ReportSelectorView.class).to(ReportSelectionPanel.class).in(Singleton.class);
		bind(ProfilerAppView.class).to(InspectorWidget.class).in(Singleton.class);
		bind(DatasetListView.class).to(DatasetPanel.class).in(Singleton.class);
		bind(EventView.class).to(EventPanel.class).in(Singleton.class);
		bind(ReportView.class).to(ReportPanel.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public PlaceController getPlaceController(EventBus bus) {
		return new PlaceController(bus);
	}

	@Provides
	@Singleton
	public HistoryMapper getHistoryMapper(ProfilerPlaceFactory factory) {
		HistoryMapper hm = GWT.create(HistoryMapper.class);
		hm.setFactory(factory);
		return hm;
	}

	@Provides
	@Singleton
	public RequestFactory getRequestFactory(EventBus bus, DatasetAwareRequestTransport transport) {
		RequestFactory rf = GWT.create(RequestFactory.class);
		rf.initialize(bus, transport);
		return rf;
	}

	@Provides
	@Singleton
	public RequestBatcher<RequestFactory, DatasetRequest> getRequestBatcher(RequestFactory rf) {
		return new RequestBatcher<RequestFactory, DatasetRequest>(rf) {
			@Override
			protected DatasetRequest createContext(RequestFactory rf) {
				return rf.datasetRequest();
			}
		};
	}

	@Provides
	public DatasetRequest getDatasetRequest(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.getRequestFactory().datasetRequest();
	}

	@Provides
	public ClazzRequest getClassRequest(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.get().append(r.getRequestFactory().classRequest());
	}

	@Provides
	public MethodRequest getMethodRequest(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.get().append(r.getRequestFactory().methodRequest());
	}

	@Provides
	public FieldRequest getFieldRequest(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.get().append(r.getRequestFactory().fieldRequest());
	}

	@Provides
	public InstanceRequest getInstanceRequest(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.get().append(r.getRequestFactory().instanceRequest());
	}

	@Provides
	public EventRequest getEventReqeust(RequestBatcher<RequestFactory, DatasetRequest> r) {
		return r.get().append(r.getRequestFactory().eventRequest());
	}
}
