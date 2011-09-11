package nz.ac.vuw.ecs.rprofs.server.data;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatasetManager implements DatasetService {

	public interface DatasetBuilder {
		DatasetBuilder setHandle(String handle);

		DatasetBuilder setStarted(Date date);

		DatasetBuilder setStopped(Date date);

		DatasetBuilder setProgram(String program);

		DataSetId store();
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

		DatasetBuilder builder = database.getDatasetBuilder();
		builder.setHandle(handle);
		builder.setStarted(now.getTime());
		DataSetId id = builder.store();

		return database.findEntity(Dataset.class, id);
	}

	@Override
	public List<Dataset> findAllDatasets() {
		return database.findEntities(Dataset.class);
	}

	@Override
	public Dataset findDataset(String handle) {
		List<Dataset> ds = database.findEntities(Dataset.class, handle);
		assert (ds.size() == 1);
		return ds.get(0);
	}

	@Override
	public void stopDataset(Dataset dataset) {
		DatasetBuilder builder = database.getDatasetUpdater(dataset);
		builder.setStopped(Calendar.getInstance().getTime());
		builder.store();
	}

	@Override
	public void setProgram(Dataset dataset, String program) {
		DatasetBuilder builder = database.getDatasetUpdater(dataset);
		builder.setProgram(program);
		builder.store();
	}

	@Override
	public void deleteDataset(Dataset dataset) {
		database.deleteEntity(dataset);
	}
}
