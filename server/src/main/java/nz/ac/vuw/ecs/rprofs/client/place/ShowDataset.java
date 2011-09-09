package nz.ac.vuw.ecs.rprofs.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import nz.ac.vuw.ecs.rprofs.client.place.shared.CompositePlace;
import nz.ac.vuw.ecs.rprofs.client.place.shared.HasDataset;


public class ShowDataset extends CompositePlace<ShowDataset> implements HasDataset {

	public static final String TYPE = "ShowDataset";
	public static final Tokenizer TOKENIZER = new Tokenizer();

	ShowDataset() {
	}

	public ShowDataset(String dataset) {
		super();
		setParameter("ds", dataset);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public Tokenizer getTokenizer() {
		return TOKENIZER;
	}

	@Override
	public String getDatasetHandle() {
		return getParameter("ds");
	}

	public static class Tokenizer extends CompositeTokenizer<ShowDataset> implements PlaceTokenizer<ShowDataset> {
		@Override
		public ShowDataset create() {
			return new ShowDataset();
		}
	}

}
