package nz.ac.vuw.ecs.rprofs.client.place.shared;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.ProfilerFactory;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace;
import nz.ac.vuw.ecs.rprofs.client.place.InspectorPlace.InspectorPlaceTokenizer;
import nz.ac.vuw.ecs.rprofs.client.shared.Collections;

import com.google.gwt.place.shared.Place;

public class InspectorPlaceHistoryMapper implements com.google.gwt.place.shared.PlaceHistoryMapper {

	private final InspectorPlaceTokenizer tokenizer = new InspectorPlaceTokenizer();
	private final PlaceController pc;

	public InspectorPlaceHistoryMapper(ProfilerFactory factory) {
		pc = factory.getPlaceController();
	}

	@Override
	public Place getPlace(String token) {
		return tokenizer.getPlace(token);
	}

	@Override
	public String getToken(Place place) {

		InspectorPlace p;

		if (place instanceof InspectorPlace) {
			p = (InspectorPlace) place;
		}
		else {
			p = pc.getCurrent().setPlace(place);
		}

		return tokenizer.getToken(p);
	}

	public static List<String> tokenize(String input, char separator) {
		List<String> tokens = Collections.newList();

		int start = 0;
		int current = 0;

		while (current <= input.length()) {
			char c = (current < input.length()) ? input.charAt(current) : separator;
			if (c == separator) {
				tokens.add(input.substring(start, current));
				start = current + 1;
			}
			current++;
		}

		return tokens;
	}
}