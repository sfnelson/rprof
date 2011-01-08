package nz.ac.vuw.ecs.rprofs.client.places;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.places.InspectorPlace.InspectorPlaceTokenizer;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class ProfilerPlaceHistoryMapper implements PlaceHistoryMapper {

	private InspectorPlaceTokenizer tokenizer = new InspectorPlaceTokenizer();

	@Override
	public Place getPlace(String token) {
		return tokenizer.getPlace(token);
	}

	@Override
	public String getToken(Place place) {
		if (place instanceof InspectorPlace) {
			return tokenizer.getToken((InspectorPlace) place);
		}

		return "";
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