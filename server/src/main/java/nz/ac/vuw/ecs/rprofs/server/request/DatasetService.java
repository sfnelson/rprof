package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;

public interface DatasetService {
	DataSet findDataset(String handle);
	List<DataSet> findAllDatasets();
	void stopDataset(String dataset);
	void deleteDataset(String dataset);
	DataSet setProgram(DataSet dataSet, String program);
}
