package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;

public class ActiveContext {

	private final Context context;
	private final Dataset dataset;

	private ClassManager classes;

	private long eventId = 0;
	private int classId = 0;

	private final Map<String, List<ClassId>> awaitingSuper;

	public ActiveContext(Context context, Dataset dataset) {
		this.context = context;
		this.dataset = dataset;

		classes = new ClassManager();

		awaitingSuper = Collections.newMap();
	}

	public Dataset getDataset() {
		return dataset;
	}

	public Context getContext() {
		return context;
	}

	public void setMainMethod(String program) {
		Context d = ContextManager.getInstance().getDefault();
		try {
			d.open();
			Dataset ds = d.find(Dataset.class, dataset.getId());
			ds.setProgram(program);
		}
		finally {
			d.close();
		}
	}

	public EventId nextEvent() {
		return new EventId(dataset.getId(), ++eventId);
	}

	public ClassId nextClass() {
		return new ClassId(dataset.getId(), ++classId);
	}

	public byte[] weaveClass(byte[] buffer) {
		Weaver weaver = new Weaver(nextClass());

		byte[] result = weaver.weave(buffer);

		ClassRecord cr = weaver.getClassRecord();
		Class cls = cr.toClass(dataset);
		context.em().persist(cls);

		for (FieldRecord fr: cr.fields.values()) {
			context.em().persist(fr.toAttribute(cls));
		}

		for (MethodRecord mr: cr.methods.values()) {
			context.em().persist(mr.toAttribute(cls));
		}

		if (cr.superName != null) {
			Class parent = classes.findClass(cr.superName);

			if (parent != null) {
				cls.setParent(parent);
			}
			else {
				List<ClassId> list = awaitingSuper.get(cr.superName);
				if (list == null) {
					list = Collections.newList();
					awaitingSuper.put(cr.superName, list);
				}
				list.add(cls.getId());
			}
		}

		if (awaitingSuper.containsKey(cls.getName())) {
			for (ClassId cid: awaitingSuper.remove(cls.getName())) {
				Class c = classes.find(cid);
				if (c != null) {
					c.setParent(cls);
				}
				else {
					throw new NullPointerException(
							String.format("could not find class id %s with parent %s (%s) [%d %d]",
									cid.toString(), cls.getName(), cls.getId().toString(), cid.getId(), cls.getId().getId()));
				}
			}
		}

		context.em().persist(new Event(nextEvent(), null, Event.CLASS_WEAVE, cls, null, new ArrayList<Instance>()));

		return result;
	}
}