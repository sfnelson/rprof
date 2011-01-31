package nz.ac.vuw.ecs.rprofs.client.place;

public class InstanceBrowserPlace extends ReportPlace<InstanceBrowserPlace> {

	public static final String INSTANCE_BROWSER = "instances";

	public InstanceBrowserPlace(DatasetPlace dataset) {
		super(INSTANCE_BROWSER, dataset);
	}

	@Override
	public InstanceBrowserPlace setDataset(DatasetPlace place) {
		return new InstanceBrowserPlace(place);
	}

}
