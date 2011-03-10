package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.EventBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.EventRequest;
import nz.ac.vuw.ecs.rprofs.client.views.EventReportView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class EventBrowserActivity extends ReportActivity<EventBrowserPlace> implements EventReportView.Presenter {

	private final AsyncDataProvider<EventProxy> provider;

	private EventReportView view;

	public EventBrowserActivity(ProfilerFactory factory, EventBrowserPlace place) {
		super(factory, place);

		provider = new EventProvider();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = getFactory().getEventReportView();
		view.setPresenter(this);

		panel.setWidget(view);
	}

	@Override
	public AsyncDataProvider<EventProxy> getDataProvider() {
		return provider;
	}

	private class EventProvider extends AsyncDataProvider<EventProxy> {

		@Override
		protected void onRangeChanged(final HasData<EventProxy> container) {
			final Range r = container.getVisibleRange();

			EventRequest rq = getFactory().getRequestFactory().eventRequest();
			rq.findEvents(getDataset().getHandle(), r.getStart(), r.getLength()).fire(new Receiver<List<EventProxy>>() {
				@Override
				public void onSuccess(List<EventProxy> result) {
					container.setRowData(r.getStart(), result);
				}
			});
		}

	}
}
