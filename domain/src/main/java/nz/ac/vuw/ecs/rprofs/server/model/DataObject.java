package nz.ac.vuw.ecs.rprofs.server.model;

public interface DataObject<I extends Id<I, T>, T extends DataObject<I, T>> {

	public I getId();

}
