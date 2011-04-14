package nz.ac.vuw.ecs.rprofs.client.place.shared;

import java.util.Map;
import java.util.Map.Entry;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public abstract class CompositePlace<P extends CompositePlace<P>> extends Place {

	protected final Map<String, String> parameters = Collections.newMap();

	public CompositePlace() {}

	public CompositePlace(CompositePlace<?> other) {
		this.parameters.putAll(other.parameters);
	}

	public P clonePlace() {
		P place = getTokenizer().create();
		place.parameters.putAll(parameters);
		return place;
	}

	public abstract CompositeTokenizer<P> getTokenizer();

	public abstract String getType();

	public String getParameter(String key) {
		return parameters.get(key);
	}

	public void setParameter(String key, String value) {
		parameters.put(key, value);
	}

	@Override
	public int hashCode() {
		return parameters.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		CompositePlace<?> p = (CompositePlace<?>) obj;
		return (this.parameters.equals(p.parameters));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> p: parameters.entrySet()) {
			sb.append(p.getKey());
			sb.append('=');
			sb.append(p.getValue());
			sb.append('&');
		}

		String params;
		if (sb.length() == 0) params = "";
		else params = sb.substring(0, sb.length() - 1);

		return getType() + ":" + params;
	}

	public static abstract class CompositeTokenizer<P extends CompositePlace<P>> implements PlaceTokenizer<P> {

		public abstract P create();

		public P getPlace(String token) {
			P place = create();
			while (token.indexOf('&') > 0) {
				split(place, token.substring(0, token.indexOf('&')));
				token = token.substring(token.indexOf('&') + 1);
			}
			split(place, token);
			return place;
		}

		private void split(P place, String token) {
			String key = token.substring(0, token.indexOf('='));
			String value = token.substring(token.indexOf('=') + 1);
			place.parameters.put(key, value);
		}

		@Override
		public String getToken(P place) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> p: place.parameters.entrySet()) {
				sb.append(p.getKey());
				sb.append('=');
				sb.append(p.getValue());
				sb.append('&');
			}
			if (sb.length() == 0) return "";
			else return sb.substring(0, sb.length() - 1);
		}
	}
}
