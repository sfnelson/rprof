package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatasetManager implements DatasetService {

	public interface DatasetBuilder<D extends DatasetBuilder<D>> {
		D setHandle(String handle);

		D setStarted(Date date);

		D setStopped(Date date);

		D setProgram(String program);
	}

	public interface DatasetCreator<D extends DatasetCreator<D>> extends DatasetBuilder<D>, Creator<DatasetId, Dataset> {
	}

	public interface DatasetQuery<D extends DatasetQuery<D>> extends DatasetBuilder<D>, Query<DatasetId, Dataset> {
	}

	public interface DatasetUpdater<D extends DatasetUpdater<D>> extends DatasetBuilder<D>, Updater<DatasetId, Dataset> {
	}

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
	public void stopDataset(Dataset dataset) {
		DatasetUpdater builder = database.getDatasetUpdater();
		builder.setStopped(Calendar.getInstance().getTime());
		builder.update(dataset.getId());
	}

	@Override
	public void setProgram(Dataset dataset, String program) {
		DatasetUpdater builder = database.getDatasetUpdater();
		builder.setProgram(program);
		builder.update(dataset.getId());
	}

	@Override
	public void deleteDataset(Dataset dataset) {
		database.deleteEntity(dataset);
	}
}
