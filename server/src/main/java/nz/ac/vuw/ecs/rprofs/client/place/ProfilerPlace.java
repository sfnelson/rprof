package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nz.ac.vuw.ecs.rprofs.client.request.DatasetRequest;
import nz.ac.vuw.ecs.rprofs.client.request.id.DatasetIdProxy;
import nz.ac.vuw.ecs.rprofs.client.request.id.InstanceIdProxy;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public class ProfilerPlace extends Place implements HasDataset, HasView, HasInstance {

	@Nullable
	DatasetIdProxy dataset;

	@Nullable
	String view;

	@Nullable
	InstanceIdProxy instance;

	public ProfilerPlace() {
	}

	public ProfilerPlace(@Nullable DatasetIdProxy dataset, @Nullable String view,
						 @Nullable InstanceIdProxy instance) {
		this.dataset = dataset;
		this.view = view;
		this.instance = instance;
	}

	@Override
	@Nullable
	public DatasetIdProxy getDatasetId() {
		return dataset;
	}

	@Override
	@Nullable
	public String getView() {
		return view;
	}

	@Override
	@Nullable
	public InstanceIdProxy getInstanceId() {
		return instance;
	}

	@NotNull
	public ProfilerPlace clonePlace() {
		ProfilerPlace place = new ProfilerPlace();
		place.dataset = dataset;
		place.view = view;
		place.instance = instance;
		return place;
	}

	public static class Tokenizer implements PlaceTokenizer<ProfilerPlace> {

		private final Provider<DatasetRequest> requestContext;
		private final PlaceBuilder builder = new PlaceBuilder();

		@Inject
		public Tokenizer(Provider<DatasetRequest> requestContext) {
			this.requestContext = requestContext;
		}

		@Override
		@NotNull
		public ProfilerPlace getPlace(String token) {
			builder.start();
			while (token.indexOf('&') > 0) {
				split(token.substring(0, token.indexOf('&')));
				token = token.substring(token.indexOf('&') + 1);
			}
			split(token);
			return builder.get(null);
		}

		@Override
		@NotNull
		public String getToken(@Nullable ProfilerPlace place) {
			if (place == null) return "";
			StringBuilder sb = new StringBuilder();
			if (place.dataset != null) {
				sb.append("ds=");
				sb.append(place.dataset.getValue());
			}
			if (place.view != null) {
				if (sb.length() != 0) {
					sb.append("&");
				}
				sb.append("view=");
				sb.append(place.view);
			}
			if (place.instance != null) {
				if (sb.length() != 0) {
					sb.append("&");
				}
				sb.append("i=");
				sb.append(place.instance.getValue());
			}
			return sb.toString();
		}

		private void split(String token) {
			if (token.indexOf('=') < 0) return;
			String key = token.substring(0, token.indexOf('='));
			String value = token.substring(token.indexOf('=') + 1);
			if ("ds".equals(key) && value.matches("^[0-9]$")) {
				DatasetIdProxy ds = requestContext.get().create(DatasetIdProxy.class);
				ds.setValue(Long.parseLong(value));
				builder.setDataset(ds);
			}
			if ("view".equals(key) && value.matches("^[a-z]*$")) {
				builder.setView(value);
			}
		}
	}
}
