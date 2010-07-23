package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;

public interface ClassListener {

	public void classesChanged(List<ClassRecord> cr);
	
}
