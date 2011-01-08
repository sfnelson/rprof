/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.reports;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ReportFactory {

	public Report getReport();
	public ReportGenerator createGenerator(Dataset db, Context context);
	
}
