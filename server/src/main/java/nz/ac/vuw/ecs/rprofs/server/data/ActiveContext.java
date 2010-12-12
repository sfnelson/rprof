package nz.ac.vuw.ecs.rprofs.server.data;

import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.rprofs.client.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;

public class ActiveContext extends Context {

	private long eventId = 0;
	private Map<Long, LogRecord> objects;

	public ActiveContext(RunRecord run) {
		super(run);

		classIdMap = Collections.newMap();
		classNameMap = Collections.newMap();
		objects = Collections.newMap();

		NamingStrategy.currentRun.set(run);
	}

	@Override
	protected void initClasses() {
		// don't do anything for active contexts,
		// we don't want to pull from an empty database.
	}

	public void storeLogs(List<LogRecord> records) {
		List<LogRecord> remove = Collections.newList();
		List<LogRecord> updates = Collections.newList();

		for (LogRecord r : records) {
			ClassRecord cls = getClass(r.getClassNumber());
			MethodRecord mth = getMethod(cls, r.getMethodNumber());

			switch (r.getEvent()) {
			case LogRecord.METHOD_ENTER:
				if (mth == null || !mth.isMain()) break;
				setMainMethod(cls.getName());
				break;
			case LogRecord.METHOD_RETURN:
				if (mth == null || !mth.isInit()) break;
			case LogRecord.OBJECT_TAGGED:
				LogRecord alloc = objects.get(r.getArguments()[0]);
				if (alloc == null) break;
				alloc.cnum = r.cnum;
				alloc.mnum = r.mnum;
				updates.add(alloc);
				break;
			case LogRecord.OBJECT_ALLOCATED:
				objects.put(r.getArguments()[0], r);
				remove.add(r);
				break;
			case LogRecord.OBJECT_FREED:
				objects.remove(r.getArguments()[0]);
				break;
			}
		}

		records.removeAll(remove);

		db.saveLogs(run, records);
		db.saveLogs(run, updates);
	}

	public ClassRecord createClassRecord() {
		ClassRecord cr = new ClassRecord(++run.numClasses);
		classIdMap.put(cr.getId(), cr);
		return cr;
	}

	public void initClassRecord(ClassRecord cr, int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		cr.init(version, access, name, signature, superName, interfaces);
		classNameMap.put(name, cr);
	}

	public FieldRecord createFieldRecord(ClassRecord cr) {
		return new FieldRecord(cr, cr.fields.size() + 1);
	}

	public void initFieldRecord(FieldRecord fr, int access, String name, String desc) {
		fr.init(access, name, desc);
	}

	public MethodRecord createMethodRecord(ClassRecord cr) {
		return new MethodRecord(cr, cr.methods.size() + 1);
	}

	public void initMethodRecord(MethodRecord mr, int access, String name, String desc,
			String signature, String[] exceptions) {
		mr.init(access, name, desc, signature, exceptions);
	}

	public void setMainMethod(String name) {
		if (run.getProgram() == null) {
			run.setProgram(name);
			db.updateRun(run);
		}
	}

	public long nextEvent() {
		return eventId++;
	}
}