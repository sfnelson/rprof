package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;

import nz.ac.vuw.ecs.rprofs.client.LogListener.LogCallback;
import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;
import nz.ac.vuw.ecs.rprofs.client.events.ClassListEvent;
import nz.ac.vuw.ecs.rprofs.client.events.ClassListHandler;
import nz.ac.vuw.ecs.rprofs.client.events.EventFactory;
import nz.ac.vuw.ecs.rprofs.client.events.InstanceEvent;
import nz.ac.vuw.ecs.rprofs.client.events.InstanceHandler;
import nz.ac.vuw.ecs.rprofs.client.events.ProfilerRunEvent;
import nz.ac.vuw.ecs.rprofs.client.events.ProfilerRunHandler;
import nz.ac.vuw.ecs.rprofs.client.events.ReportListEvent;
import nz.ac.vuw.ecs.rprofs.client.events.ReportListHandler;
import nz.ac.vuw.ecs.rprofs.client.history.HistoryManager;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Inspector implements EntryPoint, HasHandlers {

	private static Inspector instance;
	public static Inspector getInstance() {
		return instance;
	}
	
	{
		instance = this;
	}
	
	private final InspectorServiceAsync inspector = GWT.create(InspectorService.class);
	private final HandlerManager manager = new HandlerManager(this);

	public void onModuleLoad() {
		
		FrameLayout layout = new FrameLayout(FrameLayout.MAX_HEIGHT | FrameLayout.HIDE_BOTTOM, 15, 50, Unit.PCT);
		layout.setTop(new ProfilerRunsPane(this));
		layout.setCenter(new InspectorPanel(layout));
		layout.setBottom(new InstanceInspector(layout));
		RootPanel.get().add(layout);
		
		getProfilerRuns();
		getReports();

		new Timer() {
			public void run() {
				getProfilerRuns();
			}
		}.scheduleRepeating(5000);

		HistoryManager.getInstance().forceUpdate();
	}

	private EventFactory<ArrayList<ProfilerRun>> profilerRunCallback =
		new EventFactory<ArrayList<ProfilerRun>>(this, "Failed to perform run callback!") {
		public ProfilerRunEvent createEvent(ArrayList<ProfilerRun> result) {
			return new ProfilerRunEvent(result);
		}
	};
	
	private void getProfilerRuns() {
		inspector.getProfilerRuns(profilerRunCallback);
	}

	public void dropProfilerRun(ProfilerRun run) {
		inspector.dropProfilerRun(run, profilerRunCallback);
	}

	public void stopProfilerRun(ProfilerRun run) {
		inspector.stopProfilerRun(run, profilerRunCallback);
	}

	public HandlerRegistration addProfilerRunHandler(ProfilerRunHandler handler) {
		return manager.addHandler(ProfilerRunEvent.getType(), handler);
	}
	
	private EventFactory<ArrayList<Report>> reportListCallback = 
			new EventFactory<ArrayList<Report>>(this, "Failed to retrieve report list") {
		@Override
		public ReportListEvent createEvent(ArrayList<Report> result) {
			return new ReportListEvent(result);
		}
	};
	
	private void getReports() {
		inspector.getReports(reportListCallback);
	}
	
	public HandlerRegistration addReportListHandler(ReportListHandler handler) {
		return manager.addHandler(ReportListEvent.getType(), handler);
	}
	
	private EventFactory<ArrayList<ClassData>> classListCallback =
		new EventFactory<ArrayList<ClassData>>(this, "unable to retrieve class list") {
			@Override
			public GwtEvent<? extends EventHandler> createEvent(
					ArrayList<ClassData> result) {
				return new ClassListEvent(result);
			}
		};
	
	public void getClasses(ProfilerRun run) {
		inspector.getClasses(run, classListCallback);
	}
	
	public void addClassListHandler(ClassListHandler handler) {
		manager.addHandler(ClassListEvent.getType(), handler);
	}

	private EventFactory<InstanceData> instanceInformationCallback =
		new EventFactory<InstanceData>(this, "unable to get instance information") {
			@Override
			public GwtEvent<? extends EventHandler> createEvent(InstanceData result) {
				return new InstanceEvent(result);
			}
		};
		
	public void getInstanceInformation(ProfilerRun run, long id) { 
		inspector.getInstanceInformation(run, id, instanceInformationCallback);
	}
	
	public void addInstanceHandler(InstanceHandler handler) {
		manager.addHandler(InstanceEvent.getType(), handler);
	}

	private class LogRequestCallback implements LogCallback {
		private final ProfilerRun run;
		private final int type;
		private final int cls;
		private final LogListener listener;

		public LogRequestCallback(ProfilerRun run, int type, int cls, LogListener listener) {
			this.run = run;
			this.type = type;
			this.cls = cls;
			this.listener = listener;
		}

		@Override
		public void doRequest(final int offset, final int limit) {
			inspector.getLogs(run, type, cls, offset, limit, new AsyncCallback<ArrayList<LogData>>() {
				@Override
				public void onFailure(Throwable caught) {
					ErrorPanel.showMessage("Failed to retrieve logs!", caught);
				}
				@Override
				public void onSuccess(ArrayList<LogData> result) {
					listener.logsAvailable(type, cls, offset, limit, result, LogRequestCallback.this);
				}
			});
		}
	}

	public void getLogs(final ProfilerRun run, final int type, final int cls, final LogListener listener) {
		if (run == null) return;
		final LogCallback callback = new LogRequestCallback(run, type, cls, listener);
		inspector.getNumLogs(run, type, cls, new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve logs!", caught);
			}
			public void onSuccess(Integer result) {
				listener.logsAvailable(type, cls, result, callback);
			}
		});
	}

	private class ReportCallback<T> implements ReportListener.ReportCallback<T> {

		private final Report report;
		private final ProfilerRun run;
		private final ReportListener<T> listener;
		
		private int available;

		public ReportCallback(Report report, ProfilerRun run, ReportListener<T> listener) {
			this.report = report;
			this.run = run;
			this.listener = listener;
		}

		@Override
		public void getData(final Report.Entry parent, final T target, final int offset, final int limit) {
			inspector.getReportData(report, run, parent, offset, limit, new AsyncCallback<ArrayList<? extends Report.Entry>>() {
				@Override
				public void onFailure(Throwable caught) {
					ErrorPanel.showMessage("Failed to retrieve entries for " + report.title + "!", caught);
				}
				@Override
				public void onSuccess(ArrayList<? extends Report.Entry> result) {
					listener.handleData(parent, target, offset, limit, available, result, ReportCallback.this);
				}
			});
		}

		@Override
		public void getAvailable(final Report.Entry parent, final T target) {
			inspector.getReportData(report, run, parent, new AsyncCallback<Integer>() {
				@Override
				public void onFailure(Throwable caught) {
					ErrorPanel.showMessage("Failed to retrieve entry info for " + report.title + "!", caught);
				}
				@Override
				public void onSuccess(Integer result) {
					available = result;
					listener.dataAvailable(parent, target, result, ReportCallback.this);
				}
			});
		}

	}

	public <T> void getReportData(ProfilerRun run, Report report, Report.Entry key, T target, ReportListener<T> listener) {
		if (run == null) return;
		new ReportCallback<T>(report, run, listener).getAvailable(key, target);
	}

	public <T> void getReportStatus(final ProfilerRun run, final Report report, final ReportListener<T> listener) {
		if (run == null) return;
		inspector.getReportStatus(report, run, new AsyncCallback<Report.Status>() {
			@Override
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve status for " + report.title + " report!", caught);
			}
			@Override
			public void onSuccess(Report.Status result) {
				listener.statusUpdate(result);
			}
		});
	}
	
	public <T> void generateReport(final ProfilerRun run, final Report report, final ReportListener<T> listener) {
		if (run == null) return;
		inspector.generateReport(report, run, new AsyncCallback<Report.Status>() {
			@Override
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to generate " + report.title + " report!", caught);
			}
			@Override
			public void onSuccess(Report.Status result) {
				listener.statusUpdate(result);
			}
		});
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		manager.fireEvent(event);
	}
}
