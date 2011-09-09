package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;


public class BrowseEvents extends ReportPlace<BrowseEvents> {

	public static final String TYPE = "BrowseEvents";
	public static final Tokenizer TOKENIZER = new Tokenizer();

	public BrowseEvents() {
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Tokenizer getTokenizer() {
		return TOKENIZER;
	}

	public static class Tokenizer extends CompositeTokenizer<BrowseEvents> implements PlaceTokenizer<BrowseEvents> {
		@Override
		public BrowseEvents create() {
			return new BrowseEvents();
		}
	}
}
