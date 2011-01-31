package nz.ac.vuw.ecs.rprofs.client.place;

public class FieldBrowserPlace extends ReportPlace<FieldBrowserPlace> {

	public static final String FIELD_BROWSER = "fields";

	public FieldBrowserPlace(DatasetPlace dataset) {
		super(FIELD_BROWSER, dataset);
	}

	@Override
	public FieldBrowserPlace setDataset(DatasetPlace place) {
		return new FieldBrowserPlace(place);
	}

}
