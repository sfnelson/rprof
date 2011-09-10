package nz.ac.vuw.ecs.rprofs.server.model;


import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public interface DataObject<T extends DataObject<T, Id>, Id> {
	public Id getId();

	public Long getRpcId();

	public Integer getVersion();

	public void visit(DomainVisitor visitor);

	public interface DomainVisitor {
		public void visitDataset(Dataset ds);
	}
}
