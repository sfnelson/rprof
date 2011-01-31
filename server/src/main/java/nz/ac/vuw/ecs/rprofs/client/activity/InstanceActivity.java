package nz.ac.vuw.ecs.rprofs.client.activity;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.requests.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.requests.EventProxy;
import nz.ac.vuw.ecs.rprofs.client.views.EventView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InstanceActivity extends AbstractActivity implements EventView.Presenter {

	private final ProfilerFactory factory;
	private final InstancePlace place;

	private List<EventProxy> events;
	private boolean started = false;
	private EventView view;

	public InstanceActivity(ProfilerFactory factory, InstancePlace place) {
		this.factory = factory;
		this.place = place;

		if (place.getInstance() == null || place.getDataset() == null) {
			return;
		}

		DatasetRequest rq = factory.getRequestFactory().datasetRequest();
		rq.findEventsByInstance(place.getInstance().getIndex()).using(place.getDataset().getDataset())
		.with("thread", "type", "method", "field", "args")
		.fire(new Receiver<List<EventProxy>>() {
			@Override
			public void onSuccess(List<EventProxy> response) {
				events = response;

				// activity may have started by now
				eventsAvailable();
			}
		});
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(getClass() == obj.getClass())) {
			return false;
		}

		return place.equals(((InstanceActivity) obj).place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		started = true;

		view = factory.getEventView();
		panel.setWidget(view);

		view.clear();

		view.setPresenter(this);
		view.setTitle(place.toString());

		eventsAvailable();
	}

	private void eventsAvailable() {
		if (!started || events == null) return;

		System.out.println("displaying " + events.size() + " events");
		for (EventProxy e: events) {
			view.addEvent(e);
		}
	}

}
