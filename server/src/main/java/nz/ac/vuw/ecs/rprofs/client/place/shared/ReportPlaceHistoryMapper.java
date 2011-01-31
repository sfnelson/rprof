package nz.ac.vuw.ecs.rprofs.client.place.shared;

import nz.ac.vuw.ecs.rprofs.client.place.ClassBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.FieldBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstanceBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class ReportPlaceHistoryMapper implements PlaceHistoryMapper {

	private static final ReportPlaceHistoryMapper INSTANCE = new ReportPlaceHistoryMapper();

	public static ReportPlaceHistoryMapper instance() {
		return INSTANCE;
	}

	@Override
	public ReportPlace<?> getPlace(String token) {
		if (token.equals(ClassBrowserPlace.CLASS_BROWSER)) {
			return new ClassBrowserPlace(null);
		}
		else if (token.equals(InstanceBrowserPlace.INSTANCE_BROWSER)) {
			return new InstanceBrowserPlace(null);
		}
		else if (token.equals(FieldBrowserPlace.FIELD_BROWSER)) {
			return new FieldBrowserPlace(null);
		}
		else {
			return null;
		}
	}

	@Override
	public String getToken(Place place) {
		return place.toString();
	}

}
