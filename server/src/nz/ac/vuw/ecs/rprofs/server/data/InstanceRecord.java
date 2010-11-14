/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.data;

import nz.ac.vuw.ecs.rprofs.client.data.InstanceInfo;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceRecord extends InstanceInfo<ClassRecord, MethodRecord, FieldRecord> {

	final Context context;
	
	private final long id;
	private final int cnum;
	
	public InstanceRecord(Context context, long id, int cnum) {
		this.context = context;
		this.id = id;
		this.cnum = cnum;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public ClassRecord getType() {
		return context.getClass(cnum);
	}

}
