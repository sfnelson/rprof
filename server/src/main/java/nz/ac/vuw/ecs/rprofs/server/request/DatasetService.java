package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

public interface DatasetService {
	Dataset findDataset(String handle);
	List<Dataset> findAllDatasets();
	void stopDataset(String dataset);
	void deleteDataset(String dataset);
}
