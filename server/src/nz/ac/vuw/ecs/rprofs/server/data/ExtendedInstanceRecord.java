/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.ExtendedInstanceInfo;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class ExtendedInstanceRecord extends InstanceRecord
implements ExtendedInstanceInfo<ClassRecord, MethodRecord, FieldRecord, InstanceRecord, LogRecord> {

	private final int mnum;	
	List<LogRecord> events;
	
	public ExtendedInstanceRecord(Context context, long id, int cnum, int mnum) {
		super(context, id, cnum);
		this.mnum = mnum;
		events = Collections.newList();
	}

	public ExtendedInstanceRecord(Context context, long id, ClassRecord cr, MethodRecord mr) {
		this(context, id, cr.getId(), mr.getId());
	}

	@Override
	public LogRecord[] getEvents() {
		return events.toArray(new LogRecord[0]);
	}
	
	@Override
	public MethodRecord getConstructor() {
		return context.getMethod(getType(), mnum);
	}
	
	public ExtendedInstanceData toRPC() {
		
		LogData[] events = new LogData[this.events.size()];
		for (int i = 0; i < events.length; i++) {
			events[i] = this.events.get(i).toRPC();
		}
		
		return new ExtendedInstanceData(getId(), getType().toRPC(), getConstructor().toRPC(), events);
	}
}
