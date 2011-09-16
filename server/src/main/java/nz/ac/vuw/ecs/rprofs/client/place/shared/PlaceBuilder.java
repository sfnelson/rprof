package nz.ac.vuw.ecs.rprofs.client.place.shared;

import com.google.common.collect.Maps;
import com.google.gwt.place.shared.Place;
import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class PlaceBuilder {
	private Map<String, Object> properties = Maps.newHashMap();

	@NotNull
	public static PlaceBuilder create() {
		return new PlaceBuilder();
	}

	@NotNull
	public PlaceBuilder start() {
		properties.clear();
		return this;
	}

	@NotNull
	public PlaceBuilder setDataset(@Nullable DatasetIdProxy dataset) {
		properties.put("ds", dataset);
		return this;
	}

	@NotNull
	public PlaceBuilder setView(@Nullable String view) {
		properties.put("view", view);
		return this;
	}

	@NotNull
	public PlaceBuilder setInstance(@Nullable InstanceIdProxy instance) {
		properties.put("i", instance);
		return this;
	}

	@NotNull
	public ProfilerPlace get(@Nullable Place current) {
		ProfilerPlace base;
		if (current == null || !(current instanceof ProfilerPlace)) {
			base = new ProfilerPlace();
		} else {
			base = ((ProfilerPlace) current).clone();
		}
		if (properties.containsKey("ds")) {
			base.dataset = (DatasetIdProxy) properties.get("ds");
		}
		if (properties.containsKey("view")) {
			base.view = (String) properties.get("view");
		}
		if (properties.containsKey("i")) {
			base.instance = (InstanceIdProxy) properties.get("i");
		}
		return base;
	}
}