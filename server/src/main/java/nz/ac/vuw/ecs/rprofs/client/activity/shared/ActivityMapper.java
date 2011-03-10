package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.activity.ClassReportActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.DatasetActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.EventBrowserActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.FieldBrowserActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InspectorActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InstanceActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.InstanceReportActivity;
import nz.ac.vuw.ecs.rprofs.client.activity.ReportActivity;
import nz.ac.vuw.ecs.rprofs.client.place.ClassBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.DatasetPlace;
import nz.ac.vuw.ecs.rprofs.client.place.EventBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.FieldBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstanceBrowserPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InstancePlace;
import nz.ac.vuw.ecs.rprofs.client.place.ReportPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;

public class ActivityMapper implements com.google.gwt.activity.shared.ActivityMapper {

	private final ProfilerFactory factory;

	public ActivityMapper(ProfilerFactory factory) {
		this.factory = factory;
	}

	@Override
	public Activity getActivity(Place place) {
		Activity result = null;

		if (place instanceof InspectorPlace) {
			result = new InspectorActivity(factory, (InspectorPlace) place);
		}
		else if (place instanceof DatasetPlace) {
			result = new DatasetActivity(factory, (DatasetPlace) place);
		}
		else if (place instanceof ReportPlace) {
			result = getReportActivity((ReportPlace<?>) place);
		}
		else if (place instanceof InstancePlace) {
			result = new InstanceActivity(factory, (InstancePlace) place);
		}

		return result;
	}

	private ReportActivity<?> getReportActivity(ReportPlace<?> place) {
		ReportActivity<?> result = null;

		if (place instanceof ClassBrowserPlace) {
			result = new ClassReportActivity(factory, (ClassBrowserPlace) place);
		}
		else if (place instanceof InstanceBrowserPlace) {
			result = new InstanceReportActivity(factory, (InstanceBrowserPlace) place);
		}
		else if (place instanceof FieldBrowserPlace) {
			result = new FieldBrowserActivity(factory, (FieldBrowserPlace) place);
		}
		else if (place instanceof EventBrowserPlace) {
			result = new EventBrowserActivity(factory, (EventBrowserPlace) place);
		}

		return result;
	}

}
