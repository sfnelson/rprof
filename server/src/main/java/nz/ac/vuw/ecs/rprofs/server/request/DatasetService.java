package nz.ac.vuw.ecs.rprofs.server.request;

import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DatasetId;

import java.util.List;

public interface DatasetService {

	Dataset findDataset(DatasetId id);

	Dataset findDataset(String handle);

	List<Dataset> findAllDatasets();

	void stopDataset(Dataset dataset);

	void deleteDataset(Dataset dataset);

	void setProgram(Dataset dataset, String program);
}
