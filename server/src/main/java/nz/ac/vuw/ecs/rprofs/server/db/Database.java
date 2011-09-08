package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.domain.DataSet;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;

import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: ${DATE}
 */
public class Database {

	@NotNull
	private final Mongo mongo;

	public Database(@NotNull Mongo mongo) {
		this.mongo = mongo;
	}

	public DataSet createDataset() {
		Calendar now = Calendar.getInstance();
		String handle = String.format("%02d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH),
				now.get(Calendar.DATE),
				now.get(Calendar.HOUR),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
		short id = getNextId();

		DataSet ds = new DataSet(new DataSetId(id), handle, now.getTime());

		DB db = mongo.getDB(getName(ds));
		DBCollection properties = db.getCollection("properties");
		properties.save(new BasicDBObjectBuilder()
				.add("_id", ds.getId().indexValue())
				.add("started", ds.getStarted())
				.add("handle", ds.getHandle())
				.get()
		);
		return ds;
	}

	public List<DataSet> getDatasets() {
		List<DataSet> result = Lists.newArrayList();
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof_")) {
				result.add(fetchDataset(dbname));
			}
		}
		return result;
	}

	public DataSet getDataSet(DataSetId id) {
		for (DataSet ds: getDatasets()) {
			if (ds.getId().equals(id)) return ds;
		}
		return null;
	}

	public DataSet getDataSet(Long id) {
		for (DataSet ds: getDatasets()) {
			if (ds.getId().longValue().equals(id)) return ds;
		}
		return null;
	}

	public DataSet getDataSet(String handle) {
		for (DataSet ds: getDatasets()) {
			if (ds.getHandle().equals(handle)) return ds;
		}
		return null;
	}

	public DataSet dropDataset(DataSet dataset) {
		DataSet ds = fetchDataset(getName(dataset));
		if (ds != null) {
			mongo.dropDatabase(getName(dataset));
		}
		return ds;
	}

	@VisibleForTesting
	short getNextId() {
		short max = 0;
		for (String dbname: mongo.getDatabaseNames()) {
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
	String getName(DataSet dataset) {
		return "rprof_" + dataset.getHandle() + "_" + dataset.getId().indexValue();
	}

	@VisibleForTesting
	DataSet fetchDataset(String name) {
		DB db = mongo.getDB(name);
		DBObject properties = db.getCollection("properties").findOne();
		short id = ((Integer) properties.get("_id")).shortValue();
		String handle = (String) properties.get("handle");
		Date started = (Date) properties.get("started");
		DataSet ds = new DataSet(new DataSetId(id), handle, started);
		ds.setStopped((Date) properties.get("stopped"));
		ds.setProgram((String) properties.get("program"));
		return ds;
	}

}
