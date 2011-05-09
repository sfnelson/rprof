package nz.ac.vuw.ecs.rprofs.client.activity.shared;

import nz.ac.vuw.ecs.rprofs.client.Factory;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;

import com.google.gwt.activity.shared.AbstractActivity;

public abstract class AbstractInspectorActivity<T extends ReportPlace<T>> extends AbstractActivity {

	private final Factory factory;
	private final T place;

	public AbstractInspectorActivity(Factory factory, T place) {
		this.factory = factory;
		this.place = place;
	}

	protected Factory getFactory() {
		return factory;
	}

	protected T getPlace() {
		return place;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(this.getClass() == obj.getClass())) return false;
		return place.equals(((AbstractInspectorActivity<?>) obj).place);
	}

	@Override
	public int hashCode() {
		return place.hashCode();
	}

}
