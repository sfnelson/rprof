package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import com.google.web.bindery.requestfactory.shared.Locator;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Calendar;
import java.util.List;

@Configurable
public class DatasetManager extends Locator<Dataset, DatasetId> implements DatasetService {

	@VisibleForTesting
	@Autowired(required = true)
	Database database;

	public Dataset createDataset() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));

		DatasetId id = database.getDatasetCreator()
				.setHandle(handle)
				.setStarted(now.getTime())
				.store();

		return database.findEntity(id);
	}

	@Override
	public Dataset create(Class<? extends Dataset> type) {
		return new Dataset();
	}

	@Override
	public Class<Dataset> getDomainType() {
		return Dataset.class;
	}

	@Override
	public Class<DatasetId> getIdType() {
		return DatasetId.class;
	}

	@Override
	public DatasetId getId(Dataset dataset) {
		return dataset.getId();
	}

	@Override
	public Dataset find(Class<? extends Dataset> type, DatasetId id) {
		return findDataset(id);
	}

	@Override
	public Integer getVersion(Dataset dataset) {
		return dataset.getVersion();
	}

	@Override
	public List<? extends Dataset> findAllDatasets() {
		return database.getDatasetQuery().find();
	}

	@Override
	public Dataset findDataset(String handle) {
		List<? extends Dataset> ds = database.getDatasetQuery().setHandle(handle).find();
		assert (ds.size() == 1);
		return ds.get(0);
	}

	@Override
	public Dataset findDataset(DatasetId id) {
		return database.findEntity(id);
	}

	@Override
	public void stopDataset(DatasetId dataset) {
		database.getDatasetUpdater()
				.setStopped(Calendar.getInstance().getTime())
				.update(dataset);
	}

	@Override
	public void setProgram(DatasetId dataset, String program) {
		database.getDatasetUpdater()
				.setProgram(program)
				.update(dataset);
	}

	@Override
	public void deleteDataset(DatasetId id) {
		Dataset dataset = database.findEntity(id);
		database.deleteEntity(dataset);
	}
}
