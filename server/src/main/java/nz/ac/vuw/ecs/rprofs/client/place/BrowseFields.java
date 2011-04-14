package nz.ac.vuw.ecs.rprofs.client.place;

import nz.ac.vuw.ecs.rprofs.client.place.shared.ReportPlace;

import com.google.gwt.place.shared.PlaceTokenizer;



public class BrowseFields extends ReportPlace<BrowseFields> {

	public static final String TYPE = "BrowseFields";
	public static final Tokenizer TOKENIZER = new Tokenizer();

	public BrowseFields() {}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Tokenizer getTokenizer() {
		return TOKENIZER;
	}

	public static class Tokenizer extends CompositeTokenizer<BrowseFields> implements PlaceTokenizer<BrowseFields> {
		@Override
		public BrowseFields create() {
			return new BrowseFields();
		}
	}
}
