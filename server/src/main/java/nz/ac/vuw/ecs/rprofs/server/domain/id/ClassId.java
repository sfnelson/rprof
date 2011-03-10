package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class ClassId extends Id<nz.ac.vuw.ecs.rprofs.server.domain.Class> {

	public static final Class<ClassId> TYPE = ClassId.class;

	private static final long mask = 0xFFFFFFFF;

	public ClassId() {}

	public ClassId(short dataset, int index) {
		super((((long) dataset) << 48) | index);
	}

	public ClassId(long classId) {
		super(classId);
	}

	public short getDataset() {
		return (short) ((getId() >>> 48) & 0xFFFF);
	}

	public int getIndex() {
		return (int) (getId() & mask);
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getDataset(), getIndex());
	}
}
