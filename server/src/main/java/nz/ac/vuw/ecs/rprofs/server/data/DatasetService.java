package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.db.Datastore;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Package;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;

import com.google.gwt.requestfactory.shared.Locator;

public class DatasetService extends Locator<Dataset, Long> {

	private Datastore getDatasetStore() {
		return ContextManager.getInstance().database;
	}

	private Context getDomainStore(Dataset dataset) {
		return ContextManager.getInstance().getContext(dataset);
	}

	@Override
	public Dataset create(java.lang.Class<? extends Dataset> clazz) {
		return new Dataset();
	}

	@Override
	public Dataset find(java.lang.Class<? extends Dataset> clazz, Long id) {
		return getDatasetStore().findRecord(Dataset.class, id.shortValue());
	}

	@Override
	public java.lang.Class<Dataset> getDomainType() {
		return Dataset.class;
	}

	@Override
	public Long getId(Dataset dataset) {
		return (long) dataset.getId();
	}

	@Override
	public java.lang.Class<Long> getIdType() {
		return Long.class;
	}

	@Override
	public Integer getVersion(Dataset dataset) {
		return dataset.getVersion();
	}

	public void stopDataset(Dataset dataset) {
		ContextManager.getInstance().stop(dataset);
	}

	public void deleteDataset(Dataset dataset) {
		ContextManager.getInstance().delete(dataset);
	}

	public List<? extends Dataset> findAllDatasets() {
		return getDatasetStore().findRecords(Dataset.class);
	}

	public List<? extends Instance> findInstances(Dataset dataset, long classId) {
		Context c = getDomainStore(dataset);
		return c.findInstances(new ClassId(classId));
	}

	public Dataset findDataset(String handle) {
		Context c = ContextManager.getInstance().getContext(handle);
		return c.getDataset();
	}

	public List<? extends Package> findPackages(Dataset dataset) {
		Context c = getDomainStore(dataset);
		return c.findPackages();
	}
}
