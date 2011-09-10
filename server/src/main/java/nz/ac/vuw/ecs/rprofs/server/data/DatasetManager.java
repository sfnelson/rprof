package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatasetManager implements DatasetService {

	@Autowired
	private Database database;

	public Dataset createDataset() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));

		return database.createEntity(Dataset.class, handle, now.getTime());
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
	public void stopDataset(String dataset) {
		Dataset ds = findDataset(dataset);
		ds.setStopped(Calendar.getInstance().getTime());
		database.updateEntity(ds);
	}

	@Override
	public Dataset setProgram(Dataset dataset, String program) {
		dataset.setProgram(program);
		return database.updateEntity(dataset);
	}

	public Dataset setStopped(Dataset dataset, Date stopped) {
		dataset.setStopped(stopped);
		return database.updateEntity(dataset);
	}

	@Override
	public void deleteDataset(String handle) {
		database.deleteEntity(findDataset(handle));
	}
}
