package nz.ac.vuw.ecs.rprofs.client;

import java.util.Collection;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;

public interface ClassListener {

	public void classesChanged(Collection<ClassRecord<MethodRecord, FieldRecord>> result);
	
}
