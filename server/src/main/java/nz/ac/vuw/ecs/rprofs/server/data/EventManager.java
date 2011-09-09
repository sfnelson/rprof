package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventManager implements EventService {

	public interface EventBuilder {
		Dataset getDataset();

		EventBuilder setDataSet(Dataset ds);

		EventId getId();

		EventBuilder setId(long id);

		ObjectId getThread();

		EventBuilder setThread(long thread);

		Integer getEvent();

		EventBuilder setEvent(int event);

		ClassId getClazz();

		EventBuilder setClazz(int cnum);

		MethodId getMethod();

		EventBuilder setMethod(short mnum);

		FieldId getField();

		EventBuilder setField(short fnum);

		List<ObjectId> getArgs();

		EventBuilder clearArgs();

		EventBuilder addArg(long arg);
	}

	@Autowired
	private Database database;

	@Override
	public List<? extends Event> findEvents(Integer start, Integer length, Integer filter) {
		return null;
	}

	@Override
	public List<? extends Event> findEventsByInstance(Long instance) {
		return null;
	}

	@Override
	public Long findNumEvents(Integer filter) {
		return null;
	}

	@Override
	public Long findIndexOf(Long eventId, Integer filter) {
		return null;
	}

	@Override
	public Long findNumThreads() {
		return null;
	}

	@Override
	public List<Instance> findThreads() {
		return null;
	}
}
