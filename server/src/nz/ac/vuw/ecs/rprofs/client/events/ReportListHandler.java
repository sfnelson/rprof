/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.events;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.event.shared.EventHandler;

public interface ReportListHandler extends EventHandler {
	public void onReportsAvailable(List<Report> reports);
}