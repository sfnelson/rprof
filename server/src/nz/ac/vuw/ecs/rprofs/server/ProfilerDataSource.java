/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ProfilerDataSource<CR extends ClassRecord<MR, FR>, MR extends MethodRecord,
		FR extends FieldRecord, PR extends ProfilerRun, LR extends LogRecord> {
	public List<CR> getClasses(ProfilerRun run);
	public List<PR> getProfiles();
	public void dropRun(ProfilerRun run);
	public int getNumLogs(ProfilerRun run, int type, int cls);
	public List<LR> getLogs(ProfilerRun run, int offset, int limit, int type, int cls);
}
