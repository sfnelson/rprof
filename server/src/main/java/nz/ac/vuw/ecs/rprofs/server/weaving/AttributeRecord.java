package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.server.domain.Attribute;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;

public interface AttributeRecord {

	Attribute toAttribute(Class cls);
}
