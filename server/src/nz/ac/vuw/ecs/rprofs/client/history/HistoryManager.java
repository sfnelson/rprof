/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client.history;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.client.Inspector;
import nz.ac.vuw.ecs.rprofs.client.data.ProfilerRun;
import nz.ac.vuw.ecs.rprofs.client.events.ProfilerRunHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class HistoryManager implements HasValueChangeHandlers<History>, ValueChangeHandler<String>, ProfilerRunHandler {
	
	private static HistoryManager instance = GWT.create(HistoryManager.class);
	
	public static HistoryManager getInstance() {
		return instance;
	}

	private HandlerManager manager = new HandlerManager(this);
	private Map<String, ProfilerRun> runs = Collections.newMap();
	private History previous;
	
	public HistoryManager() {
		com.google.gwt.user.client.History.addValueChangeHandler(this);
		Inspector.getInstance().addProfilerRunHandler(this);
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<History> handler) {
		return manager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		manager.fireEvent(event);
	}
	
	public History getHistory() {
		return parse(com.google.gwt.user.client.History.getToken());
	}
	
	public void update(History history) {
		com.google.gwt.user.client.History.newItem(history.toString());
	}
	
	public void forceUpdate() {
		History history = getHistory();
		
		ValueChangeEvent.fire(this, history);
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		History history = parse(event.getValue());
		ValueChangeEvent.fire(this, history);
	}
	
	@Override
	public void profilerRunsAvailable(List<ProfilerRun> result) {
		runs.clear();
		for (ProfilerRun run: result) {
			runs.put(run.handle, run);
		}
		
		History current = getHistory();
		if (previous != null && current != null && current.run != null && current.run.equals(previous.run));
		else {
			ValueChangeEvent.fire(this, current);
		}
		
		previous = current;
	}

	private History parse(String input) {
		History history = new History();
		
		int last = 0;
		int i = input.indexOf('&') + 1;
		while (i != 0) {
		  parseToken(history, input, last, i - 1);
		  last = i;
		  i = input.indexOf('&', i+1) + 1;
		}
		parseToken(history, input, last, input.length());
		
		if (history.handle != null) {
			history.run = runs.get(history.handle);
		}
		
		return history;
	}
	
	private void parseToken(History history, String input, int from, int to) {
		String token = input.substring(from, to);
		int separator = token.indexOf('=');
		if (separator == -1) return;
		
		String name = token.substring(0, separator);
		String value = token.substring(separator+1);
		
		if (name.equals("run")) {
			history.handle = value;
		}
		else if (name.equals("id")) {
			history.id = Long.parseLong(value);
		}
		else if (name.equals("view")) {
			history.view = value;
		}
	}
}
