package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9 September, 2011
 */
public class Database {

	@NotNull
	private final Mongo mongo;

	public Database(@NotNull Mongo mongo) {
		this.mongo = mongo;
	}

	public Dataset createDataset() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
		short id = getNextId();

		Dataset ds = new Dataset(new DataSetId(id), handle, now.getTime());

		DB db = mongo.getDB(getName(ds));
		DBCollection properties = db.getCollection("properties");
		properties.save(createPropertiesObject(ds));

		return ds;
	}

	public List<Dataset> getDatasets() {
		List<Dataset> result = Lists.newArrayList();
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof_")) {
				result.add(fetchDataset(dbname));
			}
		}
		return result;
	}

	public Dataset getDataSet(DataSetId id) {
		for (Dataset ds : getDatasets()) {
			if (ds.getId().equals(id)) return ds;
		}
		return null;
	}

	public Dataset getDataSet(Long id) {
		for (Dataset ds : getDatasets()) {
			if (ds.getId().longValue().equals(id)) return ds;
		}
		return null;
	}

	public Dataset getDataset(String handle) {
		for (Dataset ds : getDatasets()) {
			if (ds.getHandle().equals(handle)) return ds;
		}
		return null;
	}

	public DB getDatabase(@NotNull Dataset dataset) {
		return mongo.getDB(getName(dataset));
	}

	public Dataset setStopped(Dataset dataset, Date stopped) {
		dataset.setStopped(stopped);

		DB db = mongo.getDB(getName(dataset));
		DBCollection properties = db.getCollection("properties");
		properties.save(createPropertiesObject(dataset));

		return dataset;
	}

	public Dataset setProgram(Dataset dataset, String program) {
		dataset.setProgram(program);

		DB db = mongo.getDB(getName(dataset));
		DBCollection properties = db.getCollection("properties");
		properties.save(createPropertiesObject(dataset));

		return dataset;
	}

	private DBObject createPropertiesObject(Dataset ds) {
		BasicDBObjectBuilder b = new BasicDBObjectBuilder()
				.add("_id", ds.getId().indexValue())
				.add("started", ds.getStarted())
				.add("handle", ds.getHandle());
		if (ds.getStarted() != null) b.add("stopped", ds.getStopped());
		if (ds.getProgram() != null) b.add("program", ds.getProgram());
		return b.get();
	}

	public Dataset dropDataset(Dataset dataset) {
		Dataset ds = fetchDataset(getName(dataset));
		if (ds != null) {
			mongo.dropDatabase(getName(dataset));
		}
		return ds;
	}

	public void storeEvent(Dataset ds, EventManager.EventBuilder event) {
		if (event instanceof MongoEventBuilder) {
			DBCollection events = mongo.getDB(getName(ds)).getCollection("events");
			events.insert(((MongoEventBuilder) event).toDBObject());
		} else {
			throw new RuntimeException("not implemented");
		}
	}

	@VisibleForTesting
	short getNextId() {
		short max = 0;
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof")) {
				DB db = mongo.getDB(dbname);
				DBObject properties = db.getCollection("properties").findOne();
				short id = ((Integer) properties.get("_id")).shortValue();
				if (id > max) max = id;
			}
		}
		return ++max;
	}

	@VisibleForTesting
	String getName(Dataset dataset) {
		return "rprof_" + dataset.getHandle() + "_" + dataset.getId().indexValue();
	}

	@VisibleForTesting
	@Nullable
	Dataset fetchDataset(String name) {
		if (mongo.getDatabaseNames().contains(name)) {
			DB db = mongo.getDB(name);
			DBObject properties = db.getCollection("properties").findOne();
			short id = ((Integer) properties.get("_id")).shortValue();
			String handle = (String) properties.get("handle");
			Date started = (Date) properties.get("started");
			Dataset ds = new Dataset(new DataSetId(id), handle, started);
			ds.setStopped((Date) properties.get("stopped"));
			ds.setProgram((String) properties.get("program"));
			return ds;
		} else {
			return null;
		}
	}

}
