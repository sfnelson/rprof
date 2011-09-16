package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.Prefix;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ProfilerPlace;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 16/09/11
 */
public class ProfilerPlaceFactory {

	private final ProfilerPlace.Tokenizer profilerPlaceTokenizer;

	@Inject
	public ProfilerPlaceFactory(ProfilerPlace.Tokenizer profilerPlaceTokenizer) {
		this.profilerPlaceTokenizer = profilerPlaceTokenizer;
	}

	@Prefix("inspect")
	public ProfilerPlace.Tokenizer getProfilerPlaceTokenizer() {
		return profilerPlaceTokenizer;
	}
}
