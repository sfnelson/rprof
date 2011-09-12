package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;

import javax.validation.constraints.NotNull;

public class EventId extends Id<EventId, Event> {

	@NotNull
	public static EventId create(Dataset ds, long id) {
		return new EventId(ds.getId().indexValue(), id);
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

	public short datasetValue() {
		return (short) (longValue() >>> 48);
	}

	public long eventValue() {
		return longValue() & mask;
	}

	public short upperValue() {
		return (short) ((eventValue() >>> 32) & 0xFFFF);
	}

	public int lowerValue() {
		return (int) (eventValue() & 0xFFFFFFFFl);
	}

	@Override
	public String toString() {
		return String.format("%d:%d", datasetValue(), eventValue());
	}
}
