package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class MethodId extends AttributeId<Method> {

	public MethodId() {}

	public MethodId(short dataset, int type, short attribute) {
		super(dataset, type, attribute);
	}

	public MethodId(Long id) {
		super(id);
	}

	public static MethodId create(Dataset ds, Clazz type, short mnum) {
		return new MethodId(ds.getId().indexValue(), type.getId().indexValue(), mnum);
	}

	public static MethodId create(Dataset ds, ClassId type, short mnum) {
		return new MethodId(ds.getId().indexValue(), type.indexValue(), mnum);
	}
}
