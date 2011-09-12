package nz.ac.vuw.ecs.rprofs.client.activity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import nz.ac.vuw.ecs.rprofs.client.activity.shared.AbstractInspectorActivity;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseEvents;
import nz.ac.vuw.ecs.rprofs.client.request.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.request.EventRequest;
import nz.ac.vuw.ecs.rprofs.client.request.InstanceProxy;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;

import java.util.List;

public class InspectEventsActivity
		extends AbstractInspectorActivity<BrowseEvents>
		implements EventView.Presenter {

	private final AsyncDataProvider<EventProxy> provider;

	private final Provider<EventRequest> er;

	private final EventView view;
	private int filter = EventProxy.ALL;
	private int previous = 0;

	private EventProxy first;

	@Inject
	public InspectEventsActivity(Provider<EventRequest> er, EventView view) {
		this.er = er;
		this.view = view;

		provider = new EventProvider();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view.setPresenter(this);

		view.setFilter(filter);

		panel.setWidget(view);

		er.get().findThreads()
				.with("type")
				.fire(new Receiver<List<InstanceProxy>>() {
					@Override
					public void onSuccess(List<InstanceProxy> response) {
						view.setThreads(response);
					}
				});
	}

	@Override
	public AsyncDataProvider<EventProxy> getDataProvider() {
		return provider;
	}

	@Override
	public void getAvailable() {
		er.get().findNumEvents(filter)
				.fire(new Receiver<Long>() {
					@Override
					public void onSuccess(Long response) {
						view.setAvailable(response.intValue());
					}
				});
	}

	@Override
	public void toggleFilter(int filter) {
		this.filter ^= filter;

		view.setFilter(this.filter);
		update();
	}

	@Override
	public void clearFilter() {
		if (filter == EventProxy.ALL) {
			filter = previous;
		} else {
			previous = filter;
			filter = EventProxy.ALL;
		}

		view.setFilter(filter);
		update();
	}

	private void update() {
		EventRequest rq = er.get();
		if (first != null) {
			rq.findIndexOf(first.getId(), filter).to(new Receiver<Long>() {
				@Override
				public void onSuccess(Long response) {
					view.setFirst(response.intValue());
				}
			});
		}
		rq.findNumEvents(filter).to(new Receiver<Long>() {
			@Override
			public void onSuccess(Long response) {
				view.setAvailable(response.intValue());
			}
		});
		rq.fire();
	}

	private class EventProvider extends AsyncDataProvider<EventProxy> {

		@Override
		protected void onRangeChanged(final HasData<EventProxy> container) {
			final Range r = container.getVisibleRange();

			GWT.log("requested events " + r.getStart() + " to " + (r.getStart() + r.getLength()));
			er.get().findEvents(r.getStart(), r.getLength(), filter)
					.with("thread", "type", "method", "field", "args", "args.index", "args.type")
					.fire(new Receiver<List<EventProxy>>() {
						@Override
						public void onSuccess(List<EventProxy> result) {
							System.out.println("received " + result.size() + " events");
							if (!result.isEmpty()) {
								first = result.get(0);
							}
							container.setRowData(r.getStart(), result);
						}
					});
		}
	}
}
