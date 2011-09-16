package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;

import java.util.List;

public interface DatasetService {

	Dataset findDataset(DatasetId id);

	Dataset findDataset(String handle);

	List<? extends Dataset> findAllDatasets();

	void stopDataset(DatasetId dataset);

	void deleteDataset(DatasetId dataset);

	void setProgram(DatasetId dataset, String program);
}
