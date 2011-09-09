package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class ClassId extends Id<Clazz> {

	public static ClassId create(Dataset dataset, int cnum) {
		return new ClassId(dataset.getId().indexValue(), cnum);
	}

	public static final Class<ClassId> TYPE = ClassId.class;

	private static final long mask = 0xFFFFFFFF;

	public ClassId() {
	}

	public ClassId(short dataset, int index) {
		super((((long) dataset) << 48) | index);
	}

	public ClassId(long classId) {
		super(classId);
	}

	public short datasetValue() {
		return (short) ((longValue() >>> 48) & 0xFFFF);
	}

	public int indexValue() {
		return longValue().intValue();
	}

	@Override
	public String toString() {
		return String.format("%d.%d", datasetValue(), indexValue());
	}
}
