package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import nz.ac.vuw.ecs.rprofs.client.place.HasView;

public abstract class AbstractInspectorActivity extends AbstractActivity {

	private HasView place;

	public AbstractInspectorActivity() {
	}

	public Activity setPlace(HasView place) {
		this.place = place;
		return this;
	}

	protected HasView getPlace() {
		return place;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;

		AbstractInspectorActivity a = (AbstractInspectorActivity) obj;

		return place.equals(a.place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

}
