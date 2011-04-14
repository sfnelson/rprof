package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.request.EventRequest;

public class EventService extends AbstractService implements EventRequest {

	@Override
	public List<? extends Event> findEvents(String dataset, int start, int length, int filter) {
		return context(dataset).findEvents(start, length, filter);
	}

	@Override
	public List<? extends Event> findEventsByInstance(long instance) {
		ObjectId id = new ObjectId(instance);
		Instance i = context(id.getDataset()).db.findRecord(Instance.class, id);
		return context(id.getDataset()).findEventsWithArgument(i);
	}

	@Override
	public Long findNumEvents(String dataset, int filter) {
		System.out.println("using " + context(dataset).getDataset().getId());
		return context(dataset).findNumEvents(filter);
	}

	@Override
	public Long findIndexOf(long eventId, int filter) {
		try {
			EventId id = new EventId(eventId);
			System.out.println(id);
			return context(id.getDataset()).findEventIndex(id, filter);
		}
		catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
