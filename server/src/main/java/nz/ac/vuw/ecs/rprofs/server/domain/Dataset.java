package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.db.Datastore;
import nz.ac.vuw.ecs.rprofs.server.db.NamingStrategy;
import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.Field.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.Method.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.StringId;
import nz.ac.vuw.ecs.rprofs.server.weaving.ActiveContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gwt.user.client.rpc.IsSerializable;


@Entity
@Table(name = Dataset.TABLE_NAME)
@NamedQueries({
	@NamedQuery(name="findDatasets", query="select D from Dataset D")
})
public class Dataset implements IsSerializable, Comparable<Dataset> {

	public static final String TABLE_NAME = "profiler_runs";

	private static final Map<DatasetId, Datastore> stores = Collections.newMap();

	private static Datastore database;

	static {
		NamingStrategy.currentRun.set(null);

		ApplicationContext c =
			new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");

		database = c.getBean(Datastore.class);
	}

	public static Dataset createDataset() {
		Calendar s = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				s.get(Calendar.YEAR),
				s.get(Calendar.MONTH),
				s.get(Calendar.DATE),
				s.get(Calendar.HOUR),
				s.get(Calendar.MINUTE),
				s.get(Calendar.SECOND));

		Dataset dataset = new Dataset(new DatasetId(handle), s.getTime(), null, null);
		dataset = database.storeRecord(dataset);
		dataset.init();
		return dataset;
	}

	public static Dataset updateDataset(Dataset dataset) {
		return database.updateDataset(dataset);
	}

	public static void stopDataset(Dataset dataset) {
		DatasetId current = null;
		if (ActiveContext.getCurrent() != null) {
			current = ActiveContext.getCurrent().getDataset();
		}

		if (current != null && current.equals(dataset.getDatasetId())) {
			ActiveContext.stop();
		}
		else {
			dataset = Dataset.getDataset(dataset.getDatasetId());
			Dataset.setStopped(dataset, Calendar.getInstance().getTime());
		}
	}

	public static void deleteDataset(Dataset dataset) {
		database.deleteDataset(dataset);
		dataset.db.deleteDatastore(dataset);
		dataset.db.close();
	}

	public static Dataset findDataset(String handle) {
		return getDataset(new DatasetId(handle));
	}

	public static Dataset getDataset(DatasetId id) {
		Dataset dataset = database.findRecord(Dataset.class, id);
		if (dataset != null) {
			dataset.init();
		}
		return dataset;
	}

	public static List<Dataset> findAllDatasets() {
		List<Dataset> data = database.getDatasets();
		for (Dataset d: data) {
			d.init();
		}
		return data;
	}

	public static Dataset setStopped(Dataset ds, Date time) {
		ds.stopped = time;

		Dataset r = updateDataset(ds);
		r.db = ds.db;
		return r;
	}

	public static Dataset setProgram(Dataset ds, String name) {
		ds.program = name;

		Dataset r = updateDataset(ds);
		r.db = ds.db;
		return r;
	}


	public List<Package> findPackages() {
		List<? extends Class> classes = getClasses();
		Map<String, Package> packages = Collections.newMap();

		for (Class cls: classes) {
			String pkg = cls.getPackage();
			if (!packages.containsKey(pkg)) {
				packages.put(pkg, new Package(pkg, 1));
			}
			else {
				packages.get(pkg).incrementClasses();
			}
		}

		List<Package> result = Collections.newList();
		result.addAll(packages.values());
		return result;
	}

	public List<Class> findClasses(String pkg) {
		List<? extends Class> classes = getClasses();
		List<Class> result = Collections.newList();

		for (Class cls: classes) {
			if (cls.getPackage().equals(pkg)) {
				result.add(cls);
			}
		}

		return result;
	}

	public List<Field> findFields(int classId) {
		Class cls = db.findRecord(Class.class, new ClassId(classId));
		if (cls == null) return new ArrayList<Field>();

		return cls.getFields();
	}

	public List<Method> findMethods(int classId) {
		Class cls = db.findRecord(Class.class, new ClassId(classId));
		if (cls == null) return new ArrayList<Method>();

		return cls.getMethods();
	}

	public List<Instance> findInstances(int classId) {
		Class cls = db.findRecord(Class.class, new ClassId(classId));
		if (cls == null) return new ArrayList<Instance>();

		return db.findInstancesByType(cls);
	}

	public Instance findInstance(long id) {
		return db.findRecord(Instance.class, new InstanceId(id));
	}

	public List<Event> findEventsByInstance(long id) {
		Instance i = findInstance(id);
		if (i == null) {
			return Collections.newList();
		}

		return i.getEvents();
	}

	@SuppressWarnings("serial")
	@Embeddable
	public static class DatasetId extends StringId {
		public DatasetId() {}
		public DatasetId(String id) {
			super(id);
		}
	}

	@EmbeddedId
	DatasetId id;

	@Version
	int version;

	String program;

	Date started;

	Date stopped;

	@Transient
	Datastore db;

	public Dataset() {}

	public Dataset(DatasetId id, Date started, Date stopped, String program) {
		this.id = id;
		this.started = started;
		this.stopped = stopped;
		this.program = program;
	}

	private void init() {
		if (db != null) return;
		if (id == null) {
			id = getDatasetId();
		}

		if (stores.containsKey(id)) {
			db = stores.get(id);
		}
		else {
			NamingStrategy.currentRun.set(id);
			ApplicationContext c =
				new ClassPathXmlApplicationContext("nz/ac/vuw/ecs/rprofs/server/context.xml");
			db = c.getBean(Datastore.class);
			stores.put(id, db);
		}
	}

	public String getId() {
		return id.getHandle();
	}

	public int getVersion() {
		return version;
	}

	public String getHandle() {
		return id.getHandle();
	}

	public DatasetId getDatasetId() {
		return id;
	}

	public String getProgram() {
		return program;
	}

	public Date getStarted() {
		return started;
	}

	public Date getStopped() {
		return stopped;
	}

	@Override
	public int compareTo(Dataset o) {
		return started.compareTo(o.started);
	}

	public Class getClass(ClassId id) {
		return db.findRecord(Class.class, id);
	}

	public List<? extends Class> getClasses() {
		return db.findRecords(Class.class);
	}

	public Method getMethod(MethodId id) {
		return db.findRecord(Method.class, id);
	}

	public List<? extends Method> getMethods() {
		return db.findRecords(Method.class);
	}

	public Field getField(FieldId id) {
		return db.findRecord(Field.class, id);
	}

	public List<? extends Field> getFields() {
		return db.findRecords(Field.class);
	}

	public int getNumLogs(int type) {
		Query q = db.em.createQuery("select count(*) from LogRecord L where band(L.event, ?) <> 0");
		q.setParameter(1, type);
		return ((Long)q.getSingleResult()).intValue();
	}

	public List<? extends Event> getLogs(int type) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0", Event.class);
		q.setParameter(1, type);
		return q.getResultList();
	}

	public List<? extends Event> getLogs(int offset, int limit, int type) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0", Event.class);
		q.setParameter(1, type);
		return q.getResultList();
	}

	public int getNumLogs(int type, InstanceId instance) {
		Query q = db.em.createQuery("select count(*) from LogRecord L where band(L.event, ?) <> 0 and L.args[0] = ?");
		q.setParameter(1, type);
		q.setParameter(2, instance);
		return ((Long)q.getSingleResult()).intValue();
	}

	public List<? extends Event> getLogs(int type, InstanceId instance) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.args[0] = ?", Event.class);
		q.setParameter(1, type);
		q.setParameter(2, instance);
		return q.getResultList();
	}

	public List<? extends Event> getLogs(int offset, int limit, int type, InstanceId instance) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.args[0] = ?", Event.class);
		q.setParameter(1, type);
		q.setParameter(2, instance);
		return q.getResultList();
	}

	public int getNumLogs(int type, ClassId cls) {
		Query q = db.em.createQuery("select count(*) from LogRecord L where band(L.event, ?) <> 0 and L.cnum = ?");
		q.setParameter(1, type);
		q.setParameter(2, cls);
		return ((Long)q.getSingleResult()).intValue();
	}

	public List<? extends Event> getLogs(int type, ClassId cls) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.cnum = ?", Event.class);
		q.setParameter(1, type);
		q.setParameter(2, cls);
		return q.getResultList();
	}

	public List<? extends Event> getLogs(int offset, int limit, int type, ClassId cls) {
		TypedQuery<Event> q = db.em.createQuery("select L from LogRecord L where band(L.event, ?) <> 0 and L.cnum = ?", Event.class);
		q.setParameter(1, type);
		q.setParameter(2, cls);
		return q.getResultList();
	}

	public Class storeClass(Class cls) {
		return db.storeRecord(cls);
	}

	public List<? extends Class> storeClasses(Iterable<? extends Class> classes) {
		return db.storeRecords(classes);
	}

	public void updateClass(Class cls) {
		db.updateRecord(cls);
	}

	public void updateClasses(Iterable<? extends Class> classes) {
		db.updateRecords(classes);
	}

	public List<? extends Event> storeLogs(Iterable<? extends Event> events) {
		return db.storeRecords(events);
	}

	public void updateField(Field f) {
		db.updateRecord(f);
	}

	public Instance storeInstance(Instance i) {
		return db.storeRecord(i);
	}

	public void updateInstance(Instance i) {
		db.updateRecord(i);
	}

	public Instance getInstance(InstanceId id) {
		return db.findRecord(Instance.class, id);
	}

	public List<? extends FieldWriteRecord> storeReports(Iterable<? extends FieldWriteRecord> records) {
		return db.storeRecords(records);
	}
}
