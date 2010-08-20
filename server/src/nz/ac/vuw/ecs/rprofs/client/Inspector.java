package nz.ac.vuw.ecs.rprofs.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.rprofs.client.data.ClassRecord;
import nz.ac.vuw.ecs.rprofs.client.data.FieldRecord;
import nz.ac.vuw.ecs.rprofs.client.data.LogRecord;
import nz.ac.vuw.ecs.rprofs.client.data.MethodRecord;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
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

		panel = new InspectorPanel(new ProfilerRunsPane(this));
		panel.addView(new PackageList(this));
		panel.addView(new LogViewer(this));
		RootPanel.get().add(panel);

		refreshRuns();
		
		new Timer() {
			public void run() {
				refreshRuns();
			}
		}.scheduleRepeating(15000);
	}

	private void refreshRuns() {
		inspector.getProfilerRuns(new AsyncCallback<List<ProfilerRun>>() {
			public void onFailure(Throwable caught) {
				RootPanel.get().add(new Label("Failed to retrieve run information!"));
			}
			public void onSuccess(List<ProfilerRun> result) {
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
		inspector.getClasses(currentRun, new AsyncCallback<List<ClassRecord<MethodRecord, FieldRecord>>>() {
			public void onFailure(Throwable caught) {
				RootPanel.get().add(new Label("Failed to retrieve classes!"));
			}

			public void onSuccess(List<ClassRecord<MethodRecord, FieldRecord>> result) {
				for (ClassListener l: classListeners) {
					l.classesChanged(result);
				}
			}
		});
	}
	
	private final Set<LogListener> logListeners = new HashSet<LogListener>();
	public void addEventListener(LogListener l) {
		logListeners.add(l);
	}

	private static Inspector instance;
	public static Inspector getInstance() {
		return instance;
	}

	public void dropProfilerRun(ProfilerRun run) {
		inspector.dropProfilerRun(run, new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				RootPanel.get().add(new Label("Failed to drop profiler run!"));
			}

			public void onSuccess(Void result) {
				Inspector.getInstance().refreshRuns();
			}
		});
	}

	public void getLogs(final int offset, final int limit) {
		if (currentRun == null) {
			return;
		}
		inspector.getLogs(currentRun, offset, limit, new AsyncCallback<List<LogRecord>>() {
			public void onFailure(Throwable caught) {
				RootPanel.get().add(new Label("Failed to retrieve logs!"));
			}

			public void onSuccess(List<LogRecord> result) {
				for (LogListener l: logListeners) {
					l.logsAvailable(result, offset);
				}
			}
		});
	}

	public void refreshLogs() {
		if (currentRun == null) {
			return;
		}
		inspector.refreshLogs(currentRun, new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) {
				RootPanel.get().add(new Label("Failed to retrieve log size!"));
			}

			public void onSuccess(Integer size) {
				for (LogListener l: logListeners) {
					l.logsChanged(size);
				}
			}
		});
	}
}
