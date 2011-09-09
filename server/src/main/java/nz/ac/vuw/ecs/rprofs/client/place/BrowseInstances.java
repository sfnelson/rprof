package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;


public class BrowseInstances extends ReportPlace<BrowseInstances> {

	public static final String TYPE = "BrowseInstances";
	public static final Tokenizer TOKENIZER = new Tokenizer();

	public BrowseInstances() {
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Tokenizer getTokenizer() {
		return TOKENIZER;
	}

	public static class Tokenizer extends CompositeTokenizer<BrowseInstances> implements PlaceTokenizer<BrowseInstances> {
		@Override
		public BrowseInstances create() {
			return new BrowseInstances();
		}
	}
}