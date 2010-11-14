/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.core.client.GWT;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class DataManager {
	
	private static final DataManager instance = GWT.create(DataManager.class);
	
	public static DataManager getInstance() {
		return instance;
	}
	
	private ClassData getClassData(int cnum) {
		return null; // TODO complete
	}
	
	private MethodData getMethodData(int cnum, int mnum) {
		return null; // TODO complete
	}
	
	private LogData event;
	
	void visitLogEvent(LogData record, LogInfo.Visitor<InstanceData, ClassData, MethodData, FieldData> visitor) {
		event = record;
		
		switch (record.getEvent()) {
		case LogData.OBJECT_ALLOCATED:
			this.event = record;
			visitor.visitObjectAllocatedEvent(oa);
			break;
		}
	}
	
	private class EventBase {}
	
	private ObjectAllocated oa = new ObjectAllocated();
	private class ObjectAllocated extends EventBase implements LogData.ObjectAllocated {
		@Override
		public InstanceData getInstance() {
			return new InstanceData(event.args[0], getClassData(event.cnum));
		}

		@Override
		public MethodData getConstructor() {
			if (event.mnum != 0) {
				return getMethodData(event.cnum, event.mnum);
			}
			else {
				return null;
			}
		}
	};
}
