package nz.ac.vuw.ecs.rprofs.server.model;


public interface DataObject<T extends DataObject<T, Id>, Id> {
	public Id getId();
	public Long getRpcId();
	public Integer getVersion();
}
