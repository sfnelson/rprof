package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.Id;

public interface DataObject<T extends DataObject<T>> {
	public Id<T> getId();
	public Integer getVersion();
}
