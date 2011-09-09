package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;

public abstract class AbstractInspectorActivity<T extends ReportPlace<T>> extends AbstractActivity {

	private T place;

	public AbstractInspectorActivity() {
	}

	public Activity setPlace(T place) {
		this.place = place;
		return this;
	}

	protected T getPlace() {
		return place;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;

		AbstractInspectorActivity<?> a = (AbstractInspectorActivity<?>) obj;

		return place.equals(a.place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

}
