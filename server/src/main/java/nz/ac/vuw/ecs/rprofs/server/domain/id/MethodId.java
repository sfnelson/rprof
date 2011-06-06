package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

@SuppressWarnings("serial")
public class MethodId extends AttributeId<Method> {

	public MethodId() {}

	public MethodId(short dataset, int type, short attribute) {
		super(dataset, type, attribute);
	}

	public static MethodId create(Dataset ds, Class type, short mnum) {
		return new MethodId(ds.getId(), type.getId().getIndex(), mnum);
	}
}