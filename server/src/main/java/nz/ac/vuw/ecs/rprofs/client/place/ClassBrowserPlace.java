package nz.ac.vuw.ecs.rprofs.client.place;

public class ClassBrowserPlace extends ReportPlace<ClassBrowserPlace> {

	public static final String CLASS_BROWSER = "classes";

	public ClassBrowserPlace(DatasetPlace dataset) {
		super(CLASS_BROWSER, dataset);
	}

	@Override
	public ClassBrowserPlace setDataset(DatasetPlace place) {
		return new ClassBrowserPlace(place);
	}

}
