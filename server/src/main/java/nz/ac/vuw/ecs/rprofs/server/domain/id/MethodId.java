package nz.ac.vuw.ecs.rprofs.server.domain.id;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
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

	public static MethodId create(DataSet ds, Clazz type, short mnum) {
		return new MethodId(ds.getId(), type.getId().indexValue(), mnum);
	}
}
