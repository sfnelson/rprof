/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.data.Report.Entry;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public interface ReportListener<T> {

	public void statusUpdate(Report.Status status);
	public void dataAvailable(Report.Entry parent, T target, int entries, ReportCallback<T> callback);
	public void handleData(Report.Entry parent, T target, int offset, int limit, int available,
			List<? extends Entry> result, ReportCallback<T> callback);
	
	public interface ReportCallback<T> {
		public void getAvailable(Report.Entry parent, T target);
		public void getData(Report.Entry parent, T target, int offset, int limit);
	}

}
