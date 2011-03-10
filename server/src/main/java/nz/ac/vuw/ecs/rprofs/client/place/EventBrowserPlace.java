package nz.ac.vuw.ecs.rprofs.client.place;

public class EventBrowserPlace extends ReportPlace<EventBrowserPlace> {

	public static final String EVENT_BROWSER = "events";

	public EventBrowserPlace(DatasetPlace dataset) {
		super(EVENT_BROWSER, dataset);
	}

	@Override
	public EventBrowserPlace setDataset(DatasetPlace place) {
		return new EventBrowserPlace(place);
	}

}
