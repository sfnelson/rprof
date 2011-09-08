package nz.ac.vuw.ecs.rprofs.server.domain.id;

import javax.persistence.Embeddable;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;

@SuppressWarnings("serial")
@Embeddable
public class ClassId extends Id<Clazz> {

	public static ClassId create(DataSet dataSet, int cnum) {
		return new ClassId(dataSet.getId(), cnum);
	}

	public static final Class<ClassId> TYPE = ClassId.class;

	private static final long mask = 0xFFFFFFFF;

	public ClassId() {}

	public ClassId(short dataset, int index) {
		super((((long) dataset) << 48) | index);
	}

	public ClassId(long classId) {
		super(classId);
	}

	public short datasetValue() {
		return (short) ((getId() >>> 48) & 0xFFFF);
	}

	public int indexValue() {
		return getId().intValue();
	}

	@Override
	public String toString() {
		return String.format("%d.%d", datasetValue(), indexValue());
	}
}
