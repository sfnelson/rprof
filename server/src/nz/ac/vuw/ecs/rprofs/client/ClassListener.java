package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

public interface ClassListener {

	public void classesChanged(List<ClassRecord<MethodRecord, FieldRecord>> cr);
	
}
