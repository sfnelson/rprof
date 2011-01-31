package nz.ac.vuw.ecs.rprofs.server.weaving;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.data.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Attribute;
import nz.ac.vuw.ecs.rprofs.server.domain.Class;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.Event;
import nz.ac.vuw.ecs.rprofs.server.domain.Event.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.Field;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;

public class ActiveContext extends Context {

	private long eventId = 0;
	private final Map<Integer, ClassRecord> classRecords;
	private final Map<Long, Instance> objects;

	private final Map<InstanceId, Event> pendingAllocations;
	private final Map<String, List<Class>> pendingSupers;

	private final Map<Integer, Class> classMap;
	private final Map<String, Class> classNameMap;

	public ActiveContext(Dataset data) {
		super(data);

		classRecords = Collections.newMap();
		objects = Collections.newMap();

		pendingAllocations = Collections.newMap();
		pendingSupers = Collections.newMap();

		classMap = Collections.newMap();
		classNameMap = Collections.newMap();
	}

	public void storeLogs(List<Event> records) {
		List<Event> remove = Collections.newList();
		List<Event> updates = Collections.newList();

		for (Event r : records) {
			Class cls = r.getType();
			Attribute attr = r.getAttribute();
			Instance first = r.getArguments().isEmpty() ? null : r.getArguments().get(0);

			switch (r.getEvent()) {
			case Event.METHOD_ENTER:
				if (attr != null && attr instanceof Method && ((Method) attr).isMain()) {
					setMainMethod(cls.getName());
				}
				break;
			case Event.METHOD_RETURN:
				if (attr != null && attr instanceof Method && ((Method) attr).isInit());
				else break;
			case Event.OBJECT_TAGGED:
				Event alloc = pendingAllocations.get(first);
				if (alloc != null) {
					alloc.setType(cls);
					alloc.setAttribute(attr);
					updates.add(alloc);
				}
				if (first != null) {
					first = db.getInstance(first.getInstanceId());
					first.setType(cls);
					first.setConstructor((Method) r.getAttribute());
					db.updateInstance(first);
				}
				break;
			case Event.OBJECT_ALLOCATED:
				pendingAllocations.put(first.getInstanceId(), r);
				remove.add(r);
				break;
			case Event.OBJECT_FREED:
				pendingAllocations.remove(first);
				break;
			}
		}

		records.removeAll(remove);

		db.storeLogs(records);
		db.storeLogs(updates);
	}

	public void setMainMethod(String name) {
		if (db.getProgram() == null) {
			db = Dataset.setProgram(db, name);
		}
	}

	public EventId nextEvent() {
		return new EventId(++eventId);
	}

	public byte[] weaveClass(byte[] buffer) {
		int cid = classMap.size() + 1;
		Weaver weaver = new Weaver(this, cid);

		byte[] result = weaver.weave(buffer);

		ClassRecord cr = weaver.getClassRecord();
		Class cls = db.storeClass(cr.toClass());

		EventId id = nextEvent();
		Event record = new Event(id, null, Event.CLASS_WEAVE, cls, null, new ArrayList<Instance>());
		storeLogs(Arrays.asList(record));

		classRecords.put(cr.id, cr);
		classMap.put(cr.id, cls);
		classNameMap.put(cls.getName(), cls);

		if (cr.superName == null);
		else if (classNameMap.containsKey(cr.superName)) {
			cls.setParent(classNameMap.get(cr.superName));
			db.updateClass(cls);
		}
		else {
			List<Class> list = pendingSupers.get(cr.superName);
			if (list == null) {
				list = Collections.newList();
				pendingSupers.put(cr.superName, list);
			}
			list.add(cls);
		}

		if (pendingSupers.containsKey(cls.getName())) {
			List<Class> records = pendingSupers.remove(cls.getName());
			for (Class c: records) {
				c.setParent(cls);
			}
			db.updateClasses(records);
		}

		return result;
	}

	public Event createEvent(long threadId, int event, int cnum, int mnum,
			long[] args) {
		EventId id = nextEvent();
		Instance thread = getInstance(threadId);
		Class type = getClass(cnum);
		Attribute attr = null;
		if ((event & Event.FIELDS) != 0) {
			attr = getField(type, mnum);
		}
		else if ((event & Event.METHODS) != 0) {
			attr = getMethod(type, mnum);
		}

		ArrayList<Instance> argList = Collections.newList();
		for (long arg: args) {
			argList.add(getInstance(arg));
		}

		return new Event(id, thread, event, type, attr, argList);
	}

	Class getClass(int cnum) {
		Class cls = null;
		if (cnum != 0 && classMap.containsKey(cnum)) {
			cls = classMap.get(cnum);
		}
		return cls;
	}

	Method getMethod(Class cls, int mnum) {
		if (mnum == 0) return null;

		for (Method m: cls.getMethods()) {
			if (m.getIndex() == mnum) return m;
		}

		return null;
	}

	Field getField(Class cls, int mnum) {
		if (mnum == 0) return null;

		for (Field f: cls.getFields()) {
			if (f.getIndex() == mnum) return f;
		}

		return null;
	}

	Instance getInstance(long id) {
		if (id == 0) {
			return null;
		}

		if (objects.containsKey(id)) {
			return objects.get(id);
		}
		else {
			Instance i = new Instance(new InstanceId(id), null, null, null);
			i = db.storeInstance(i);
			objects.put(id, i);
			return i;
		}
	}


	Field getField(String owner, String name, String desc) {
		Class c = classNameMap.get(owner);

		if (c == null) {
			System.err.printf("we haven't indexed %s yet, so can't access it's fields\n", owner);
			return null;
		}

		for (Field f: c.getFields()) {
			if (f.getName().equals(name)) {
				if (f.getDescription().equals(desc)) {
					return f;
				}
				else {
					System.err.printf("%s doesn't match %s for %s.%s\n", desc, f.getDescription(), owner, name);
					return null;
				}
			}
		}
		System.err.printf("could not find %s.%s (%s)\n", owner, name, desc);
		return null;
	}

	void setEquals(Field f, boolean b) {
		f.setEquals(b);
		db.updateField(f);
	}

	void setHash(Field f, boolean b) {
		f.setHash(b);
		db.updateField(f);
	}
}