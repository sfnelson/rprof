/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceInfo;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceRecord extends InstanceInfo<ClassRecord, MethodRecord, FieldRecord, LogRecord> {

	private final long id;
	private final ClassRecord type;
	private final MethodRecord constructor;
	
	List<LogRecord> events;
	
	public InstanceRecord(long id, ClassRecord type, MethodRecord constructor) {
		this.id = id;
		this.type = type;
		this.constructor = constructor;
		events = Collections.newList();
	}

	@Override
	public LogRecord[] getEvents() {
		return events.toArray(new LogRecord[0]);
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public ClassRecord getType() {
		return type;
	}
	
	@Override
	public MethodRecord getConstructor() {
		return constructor;
	}
}
