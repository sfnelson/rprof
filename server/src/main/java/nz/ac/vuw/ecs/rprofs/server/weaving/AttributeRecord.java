package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;

public interface AttributeRecord {
	Attribute<?> toAttribute(Class cls);
}
