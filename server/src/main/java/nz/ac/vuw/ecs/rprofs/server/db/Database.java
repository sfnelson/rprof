package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.DataSetId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.Id;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 9 September, 2011
 */
public class Database {

	@NotNull
	private final Mongo mongo;

	@Autowired(required = true)
	private Context context;

	public Database(@NotNull Mongo mongo) {
		this.mongo = mongo;
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject> T createEntity(Class<T> type, Object... params) {

		UpdateBuilder update = new UpdateBuilder(new BasicDBObject());

		if (type == Dataset.class) {
			assert (params.length == 2);
			String handle = (String) params[0];
			Date started = (Date) params[1];
			short id = getNextId();
			Dataset ds = new Dataset(new DataSetId(id), handle, started);

			DB db = mongo.getDB(getName(ds));
			DBCollection properties = db.getCollection("properties");

			ds.visit(update);

			properties.save(update.getUpdate());

			return (T) ds;
		}

		Dataset dataset = context.getDataset();
		DB db = getDatabase(dataset);

		if (type == Clazz.class) {
			assert (params.length == 0);

			DBCollection classes = db.getCollection("classes");
			Long numClasses = classes.count();
			ClassId id = ClassId.create(dataset, numClasses.intValue() + 1);
			Clazz cls = new Clazz(dataset, id, null, null, 0);
			cls.visit(update);
			classes.insert(update.getUpdate());
			return (T) cls;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject<T, I>, I extends Id<T>> T findEntity(Class<T> type, Id<T> id) {
		if (type == Dataset.class) {
			for (Dataset ds : getDatasets()) {
				if (ds.getId().equals(id)) return (T) ds;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject> List<T> findEntities(Class<T> type, Object... queryParams) {
		if (type == Dataset.class) {
			List<Dataset> datasets = getDatasets();
			if (queryParams.length > 0) {
				assert (queryParams.length == 1);
				assert (queryParams[0].getClass() == String.class);
				String handle = (String) queryParams[0];
				for (Dataset ds : datasets) {
					if (ds.getHandle().equals(handle)) {
						return (List<T>) Lists.newArrayList(ds);
					}
				}
			} else {
				return (List<T>) datasets;
			}
		}
		return null;
	}

	public <T extends DataObject> T updateEntity(T entity) {
		EntityQueryBuilder query = new EntityQueryBuilder();
		entity.visit(query);

		Dataset dataset;
		if (entity.getClass() == Dataset.class) {
			dataset = (Dataset) entity;
		} else {
			dataset = context.getDataset();
		}

		if (dataset == null) throw new RuntimeException("no dataset in context");

		DB database = getDatabase(dataset);

		if (database == null) throw new RuntimeException("invalid dataset provided");

		DBCollection collection = database.getCollection(query.getCollection());
		DBObject target = collection.findOne(query.getQuery());

		UpdateBuilder update = new UpdateBuilder(target);
		entity.visit(update);

		collection.update(query.getQuery(), update.getUpdate());

		return entity;
	}

	public <T extends DataObject> boolean deleteEntity(T entity) {
		EntityQueryBuilder query = new EntityQueryBuilder();
		entity.visit(query);

		Dataset dataset;
		if (entity.getClass() == Dataset.class) {
			dataset = (Dataset) entity;
		} else {
			dataset = context.getDataset();
		}

		if (dataset == null) throw new RuntimeException("no dataset in context");

		DB database = getDatabase(dataset);

		if (database == null) throw new RuntimeException("invalid dataset provided");

		DBCollection collection = database.getCollection(query.getCollection());
		collection.remove(query.getQuery());

		if (entity.getClass() == Dataset.class) {
			mongo.dropDatabase(getName((Dataset) entity));
		}

		return true;
	}

	private List<Dataset> getDatasets() {
		List<Dataset> result = Lists.newArrayList();
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof_")) {
				DBObject properties = mongo.getDB(dbname)
						.getCollection("properties").findOne();
				result.add(create(Dataset.class, properties));
			}
		}
		return result;
	}

	private DB getDatabase(@NotNull Dataset dataset) {
		return mongo.getDB(getName(dataset));
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

	private class EntityQueryBuilder implements DataObject.DomainVisitor {
		BasicDBObjectBuilder query = new BasicDBObjectBuilder();
		String collection = null;

		public void visitDataset(Dataset dataset) {
			query.add("_id", dataset.getId().indexValue());
			collection = "properties";
		}

		public DBObject getQuery() {
			return query.get();
		}

		public String getCollection() {
			return collection;
		}
	}

	private class UpdateBuilder implements DataObject.DomainVisitor {
		BasicDBObjectBuilder update = new BasicDBObjectBuilder();
		DBObject current;

		public UpdateBuilder(DBObject current) {
			this.current = current;
		}

		public DBObject getUpdate() {
			return update.get();
		}

		public void visitDataset(Dataset ds) {
			checkProperty("_id", Integer.valueOf(ds.getId().indexValue()));
			checkProperty("handle", ds.getHandle());
			checkProperty("started", ds.getStarted());
			checkProperty("stopped", ds.getStopped());
			checkProperty("program", ds.getProgram());
		}

		private void checkProperty(String key, Object value) {
			if (current.containsField(key) && !current.get(key).equals(value)
					|| value == null) ;
			else update.add(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends DataObject> T create(Class<T> type, DBObject properties) {
		if (type == Dataset.class) {
			short id = ((Integer) properties.get("_id")).shortValue();
			String handle = (String) properties.get("handle");
			Date started = (Date) properties.get("started");
			Dataset ds = new Dataset(new DataSetId(id), handle, started);
			ds.setStopped((Date) properties.get("stopped"));
			ds.setProgram((String) properties.get("program"));
			return (T) ds;
		}
		return null;
	}
}
