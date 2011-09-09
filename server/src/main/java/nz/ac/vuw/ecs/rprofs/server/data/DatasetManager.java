package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.db.Database;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

public class DatasetManager implements DatasetService {

	@Autowired
	private Database database;

	@Autowired
	private ContextManager cm;

	@Override
	public List<Dataset> findAllDatasets() {
		return database.getDatasets();
	}

	@Override
	public Dataset findDataset(String handle) {
		return database.getDataset(handle);
	}

	@Override
	public void stopDataset(String dataset) {
		Dataset ds = findDataset(dataset);

		ds.setStopped(Calendar.getInstance().getTime());

		cm.stopRecording(ds);
	}

	@Override
	public Dataset setProgram(Dataset dataset, String program) {
		return database.setProgram(dataset, program);
	}

	@Override
	public void deleteDataset(String handle) {
		database.dropDataset(findDataset(handle));
	}
}
