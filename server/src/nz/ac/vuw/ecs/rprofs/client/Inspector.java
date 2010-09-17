package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.LogListener.LogCallback;
import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.data.Report;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Inspector implements EntryPoint {

	final InspectorServiceAsync inspector = GWT.create(InspectorService.class);

	private InspectorPanel panel;
	private ProfilerRun currentRun;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		instance = this;

		RootPanel.get().add(new ErrorPanel());

		panel = new InspectorPanel(new ProfilerRunsPane(this));
		
		inspector.getReports(new AsyncCallback<ArrayList<Report>>() {
			@Override
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Unable to get report list from server");
			}
			@Override
			public void onSuccess(ArrayList<Report> result) {
				for (Report r: result) {
					panel.addView(new ReportView(Inspector.this, r));
				}
				panel.addView(new LogView(Inspector.this));
			}
		});
		RootPanel.get().add(panel);

		refreshRuns();

		new Timer() {
			public void run() {
				refreshRuns();
			}
		}.scheduleRepeating(15000);
	}

	private void refreshRuns() {
		inspector.getProfilerRuns(new AsyncCallback<ArrayList<ProfilerRun>>() {
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve run information!");
			}
			public void onSuccess(ArrayList<ProfilerRun> result) {
				for (ProfilerRunListener l: runListeners) {
					l.profilerRunsAvailable(result);
				}
			}
		});
	}

	public void setProfilerRun(ProfilerRun run) {
		currentRun = run;

		panel.refresh();
	}

	private final Set<ProfilerRunListener> runListeners = new HashSet<ProfilerRunListener>();

	public void addProfilerRunListener(ProfilerRunListener l) {
		runListeners.add(l);
	}

	private final Set<ClassListener> classListeners = new HashSet<ClassListener>();

	public void addClassListener(ClassListener l) {
		classListeners.add(l);
	}

	public void getClasses() {
		if (currentRun == null) {
			return;
		}
		inspector.getClasses(currentRun, new AsyncCallback<ArrayList<ClassRecord<MethodRecord, FieldRecord>>>() {
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve classes!");
			}

			public void onSuccess(ArrayList<ClassRecord<MethodRecord, FieldRecord>> result) {
				for (ClassListener l: classListeners) {
					l.classesChanged(result);
				}
			}
		});
	}

	private static Inspector instance;
	public static Inspector getInstance() {
		return instance;
	}

	public void dropProfilerRun(ProfilerRun run) {
		inspector.dropProfilerRun(run, new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to drop profiler run!");
			}
			public void onSuccess(Void result) {
				Inspector.getInstance().refreshRuns();
			}
		});
	}

	private class LogRequestCallback implements LogCallback {
		private final ProfilerRun run;
		private final int type;
		private final LogListener listener;

		public LogRequestCallback(ProfilerRun run, int type, LogListener listener) {
			this.run = run;
			this.type = type;
			this.listener = listener;
		}

		@Override
		public void doRequest(final int offset, final int limit) {
			inspector.getLogs(run, type, offset, limit, new AsyncCallback<ArrayList<LogRecord>>() {
				@Override
				public void onFailure(Throwable caught) {
					ErrorPanel.showMessage("Failed to retrieve logs!");
				}
				@Override
				public void onSuccess(ArrayList<LogRecord> result) {
					listener.logsAvailable(type, offset, limit, result, LogRequestCallback.this);
				}
			});
		}
	}

	public void getLogs(final int type, final LogListener listener) {
		final ProfilerRun run = currentRun;
		if (run == null) return;
		final LogCallback callback = new LogRequestCallback(run, type, listener);
		inspector.getNumLogs(currentRun, type, new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve logs!");
			}
			public void onSuccess(Integer result) {
				listener.logsAvailable(type, result, callback);
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
					ErrorPanel.showMessage("Failed to retrieve entries for " + report.name + "!");
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
					ErrorPanel.showMessage("Failed to retrieve entry info for " + report.name + "!");
				}
				@Override
				public void onSuccess(Integer result) {
					available = result;
					listener.dataAvailable(parent, target, result, ReportCallback.this);
				}
			});
		}

	}

	public <T> void getReportData(Report report, Report.Entry key, T target, ReportListener<T> listener) {
		final ProfilerRun run = currentRun;
		if (run == null) return;
		new ReportCallback<T>(report, run, listener).getAvailable(key, target);
	}

	public <T> void getReportStatus(final Report report, final ReportListener<T> listener) {
		final ProfilerRun run = currentRun;
		if (run == null) return;
		inspector.getReportStatus(report, run, new AsyncCallback<Report.Status>() {
			@Override
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to retrieve status for " + report.name + " report!");
			}
			@Override
			public void onSuccess(Report.Status result) {
				listener.statusUpdate(result);
			}
		});
	}
	
	public <T> void generateReport(final Report report, final ReportListener<T> listener) {
		final ProfilerRun run = currentRun;
		if (run == null) return;
		inspector.generateReport(report, run, new AsyncCallback<Report.Status>() {
			@Override
			public void onFailure(Throwable caught) {
				ErrorPanel.showMessage("Failed to generate " + report.name + " report!");
			}
			@Override
			public void onSuccess(Report.Status result) {
				listener.statusUpdate(result);
			}
		});
	}
}
