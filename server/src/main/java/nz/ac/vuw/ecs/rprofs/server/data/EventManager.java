package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Locator;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nz.ac.vuw.ecs.rprofs.server.data.util.EventCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.request.EventService;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 11/09/11
 */
public class EventManager extends Locator<Event, EventId> implements EventService {

	private final Database database;

	@Inject
	EventManager(Database database) {
		this.database = database;
	}

	public EventCreator createEvent() {
		return database.getEventCreater();
	}

	@Override
	public List<? extends Event> findEvents(long start, long length, int filter) {
		List<Event> result = Lists.newArrayList();
		Query.Cursor<? extends Event> cursor = database.getEventQuery().setFilter(filter).find(start, length);
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
	}

	@Override
	public long findIndexOf(EventId eventId, int filter) {
		return database.getEventQuery().setBefore(eventId).setFilter(filter).count();
	}

	@Override
	public List<? extends Event> findEventsByInstance(InstanceId instance) {
		List<Event> result = Lists.newArrayList();
		Query.Cursor<? extends Event> cursor = database.getEventQuery().setWithArg(instance).find();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		cursor.close();
		return result;
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
