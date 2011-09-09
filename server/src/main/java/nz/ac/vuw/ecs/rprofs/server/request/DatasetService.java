package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

import java.util.List;

public interface DatasetService {
	Dataset findDataset(String handle);

	List<Dataset> findAllDatasets();

	void stopDataset(String dataset);

	void deleteDataset(String dataset);

	Dataset setProgram(Dataset dataset, String program);
}
