/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.client;

import java.util.ArrayList;
import java.util.HashMap;

import nz.ac.vuw.ecs.rprofs.client.data.ClassData;
import nz.ac.vuw.ecs.rprofs.client.data.FieldData;
import nz.ac.vuw.ecs.rprofs.client.data.InstanceData;
import nz.ac.vuw.ecs.rprofs.client.data.LogData;
import nz.ac.vuw.ecs.rprofs.client.events.InstanceEvent;
import nz.ac.vuw.ecs.rprofs.client.events.InstanceHandler;
import nz.ac.vuw.ecs.rprofs.client.history.History;
import nz.ac.vuw.ecs.rprofs.client.history.HistoryManager;
import nz.ac.vuw.ecs.rprofs.client.ui.FrameLayout;
import nz.ac.vuw.ecs.rprofs.client.ui.UIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class InstanceInspector extends Composite implements ValueChangeHandler<History>, InstanceHandler {

	private static InstanceInspectorUiBinder uiBinder = GWT
			.create(InstanceInspectorUiBinder.class);

	interface InstanceInspectorUiBinder extends
			UiBinder<Widget, InstanceInspector> {
	}

	private FrameLayout parent;
	
	@UiField UIButton close;
	@UiField HTML title;
	@UiField HTML content;
	
	public InstanceInspector(FrameLayout parent) {
		this.parent = parent;
		
		initWidget(uiBinder.createAndBindUi(this));
		
		HistoryManager.getInstance().addValueChangeHandler(this);
		Inspector.getInstance().addInstanceHandler(this);
	}

	@Override
	public void onValueChange(ValueChangeEvent<History> event) {
		History history = event.getValue();
		
		if (history.id != null && history.run != null) {
			parent.showBottom(true);
			Inspector.getInstance().getInstanceInformation(history.run, history.id);
		}
		else {
			parent.showBottom(false);
		}
	}
	
	@UiHandler("close")
	public void onClickClose(ClickEvent event) {
		HistoryManager m = HistoryManager.getInstance();
		History h = m.getHistory();
		h.id = null;
		m.update(h);
	}

	@Override
	public void onInstanceEvent(InstanceEvent event) {
		InstanceData info = event.getValue();
		
		if (info == null) {
			ErrorPanel.showMessage("unable to retrieve the requested object id", new Exception("instance info was null"));
			parent.showBottom(false);
			return;
		}
		
		StringBuilder title = new StringBuilder();
		title.append(info.getType().name);
		title.append(": ");
		title.append(info.getId()>> 32);
		title.append(".");
		title.append(info.getId() & 0xffffffffl);
		title.append("&nbsp;&nbsp;&nbsp;");
		title.append(info.getType().name);
		for (ClassData c = info.getType().parent; c != null; c = c.parent) {
			title.append(" &rarr; ");
			title.append(c.name);
		}
		
		this.title.setHTML(title.toString());
		
		String content = "";
		
		HashMap<Integer, HashMap<Integer, FieldStats>> stats = Collections.newMap();
		ArrayList<FieldStats> statList = Collections.newList();
		
		for (ClassData c = info.getType(); c != null; c = c.parent) {
			HashMap<Integer, FieldStats> map = Collections.newMap();
			stats.put(c.id, map);
			for (FieldData f: c.getFields()) {
				FieldStats s = new FieldStats();
				s.owner = c.name;
				s.name = f.name;
				s.type = f.desc;
				statList.add(s);
				map.put(f.id, s);
			}
		}
		
		for (LogData lr: info.getEvents()) {
			switch (lr.event) {
			case LogData.FIELD_READ:
				if (stats.containsKey(lr.cnum) && stats.get(lr.cnum).containsKey(lr.mnum))
					stats.get(lr.cnum).get(lr.mnum).reads++;
				else throw new RuntimeException("unknown field: " + lr.mnum);
				break;
			case LogData.FIELD_WRITE:
				if (stats.containsKey(lr.cnum) && stats.get(lr.cnum).containsKey(lr.mnum))
					stats.get(lr.cnum).get(lr.mnum).writes++;
				else throw new RuntimeException("unknown field: " +  lr.cnum + " " + lr.mnum);
				break;
			}
		}
		
		for (FieldStats s : statList) {
			content += s.owner + "." + s.name + ":" + s.type + "\t" + s.reads + "\t" + s.writes + "\n";
		}
		
		this.content.setHTML("<pre>" + content + "</pre>");
	}
	
	private class FieldStats {
		String owner;
		String name;
		String type;
		int reads;
		int writes;
	}
}
