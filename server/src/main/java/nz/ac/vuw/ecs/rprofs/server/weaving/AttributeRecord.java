package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

public interface AttributeRecord {
	Attribute<?> toAttribute(Clazz cls);
}
