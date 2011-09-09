package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;


public class BrowseClasses extends ReportPlace<BrowseClasses> {

	public static final String TYPE = "BrowseClasses";
	public static final Tokenizer TOKENIZER = new Tokenizer();

	public BrowseClasses() {
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Tokenizer getTokenizer() {
		return TOKENIZER;
	}

	public static class Tokenizer extends CompositeTokenizer<BrowseClasses> implements PlaceTokenizer<BrowseClasses> {
		@Override
		public BrowseClasses create() {
			return new BrowseClasses();
		}
	}
}
