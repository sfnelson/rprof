package nz.ac.vuw.ecs.rprofs.client;

import nz.ac.vuw.ecs.rprofs.client.place.BrowseClasses;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseEvents;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseFields;
import nz.ac.vuw.ecs.rprofs.client.place.BrowseInstances;
import nz.ac.vuw.ecs.rprofs.client.place.ShowDataset;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({
	ShowDataset.Tokenizer.class,
	BrowseClasses.Tokenizer.class,
	BrowseEvents.Tokenizer.class,
	BrowseFields.Tokenizer.class,
	BrowseInstances.Tokenizer.class
})
public interface HistoryMapper extends PlaceHistoryMapper {

}
