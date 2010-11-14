/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.LogInfo;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ProfilerDataSource<PR extends ProfilerRun, LR extends LogInfo> {
	public List<PR> getProfiles();
	public void dropRun(ProfilerRun run);
	public int getNumLogs(ProfilerRun run, int type, int cls);
	public List<LR> getLogs(ProfilerRun run, int offset, int limit, int type, int cls);
}
