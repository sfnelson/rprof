package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;

import javax.validation.constraints.NotNull;

public class EventId extends Id<EventId, Event> {

	@NotNull
	public static EventId create(Dataset ds, long id) {
		return new EventId(ds.getId().getDatasetIndex(), id);
	}

	private static final long mask = 0xFFFFFFFFFFFFl; // 48 bit mask

	public EventId() {
	}

	@Override
	public Class<Event> getTargetClass() {
		return Event.class;
	}

	public EventId(long eventId) {
		super(eventId);
	}

	public EventId(short dataset, long event) {
		super((((long) dataset) << 48) | (event & mask));
	}

	public short getDatasetIndex() {
		return (short) (getValue() >>> 48);
	}

	public long getEventIndex() {
		return getValue() & mask;
	}

	public short getEventUpper() {
		return (short) ((getEventIndex() >>> 32) & 0xFFFF);
	}

	public int getEventLower() {
		return (int) (getEventIndex() & 0xFFFFFFFFl);
	}

	public boolean before(EventId other) {
		return getValue() < other.getValue();
	}

	public boolean after(EventId other) {
		return getValue() > other.getValue();
	}

	@Override
	public String toString() {
		return String.format("%d:%d.%d", getDatasetIndex(), getEventUpper(), getEventLower());
	}
}
