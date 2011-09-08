package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.Event;

@SuppressWarnings("serial")
@Embeddable
public class EventId extends Id<Event> {

	private static final long mask = 0xFFFFFFFFFFFFl; // 48 bit mask

	public EventId() {}

	public EventId(long eventId) {
		super(eventId);
	}

	public EventId(short dataset, long event) {
		super((((long) dataset) << 48) | (event & mask));
	}

	public short datasetValue() {
		return (short) (eventValue() >>> 48);
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
