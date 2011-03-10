package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Method;

@SuppressWarnings("serial")
public class MethodId extends AttributeId<Method> {

	public MethodId() {}

	public MethodId(short dataset, int type, short attribute) {
		super(dataset, type, attribute);
	}
}
