package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
@Configurable
public class EventManager extends Locator<Event, EventId> implements EventService {

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public EventCreator createEvent() {
		return database.getEventCreater();
	}

	@Override
	public List<? extends Event> findEvents(int start, int length, int filter) {
		return database.getEventQuery().setFilter(filter).find(start, length);
	}

	@Override
	public long findIndexOf(EventId eventId, int filter) {
		return database.getEventQuery().setBefore(eventId).setFilter(filter).count();
	}

	@Override
	public List<? extends Event> findEventsByInstance(InstanceId instance) {
		return database.getEventQuery().setWithArg(instance).find();
	}

	@Override
	public long findNumEvents(int filter) {
		return database.getEventQuery().setFilter(filter).count();
	}

	@Override
	public long findNumThreads() {
		return database.countThreads();
	}

	@Override
	public List<? extends InstanceId> findThreads() {
		return database.findThreads();
	}

	@Override
	public Event create(Class<? extends Event> type) {
		return new Event();
	}

	@Override
	public Event find(Class<? extends Event> aClass, EventId eventId) {
		return database.findEntity(eventId);
	}

	@Override
	public Class<Event> getDomainType() {
		return Event.class;
	}

	@Override
	public EventId getId(Event event) {
		return event.getId();
	}

	@Override
	public Class<EventId> getIdType() {
		return EventId.class;
	}

	@Override
	public Object getVersion(Event event) {
		return 1;
	}
}
