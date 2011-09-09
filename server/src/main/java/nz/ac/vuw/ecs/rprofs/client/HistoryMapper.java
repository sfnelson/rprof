package nz.ac.vuw.ecs.rprofs.client;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import nz.ac.vuw.ecs.rprofs.client.place.*;

@WithTokenizers({
		ShowDataset.Tokenizer.class,
		BrowseClasses.Tokenizer.class,
		BrowseEvents.Tokenizer.class,
		BrowseFields.Tokenizer.class,
		BrowseInstances.Tokenizer.class
})
public interface HistoryMapper extends PlaceHistoryMapper {

}
