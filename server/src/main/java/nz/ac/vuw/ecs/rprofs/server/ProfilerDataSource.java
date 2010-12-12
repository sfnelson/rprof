/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.LogInfo;
import nz.ac.vuw.ecs.rprofs.client.data.RunData;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ProfilerDataSource<PR extends RunData, LR extends LogInfo> {
	public List<PR> getProfiles();
	public void dropRun(RunData run);
	public int getNumLogs(RunData run, int type, int cls);
	public List<LR> getLogs(RunData run, int offset, int limit, int type, int cls);
}
