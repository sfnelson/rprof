package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mongodb.*;
import nz.ac.vuw.ecs.rprofs.server.context.Context;
import nz.ac.vuw.ecs.rprofs.server.data.ClassManager;
import nz.ac.vuw.ecs.rprofs.server.data.DatasetManager;
import nz.ac.vuw.ecs.rprofs.server.data.EventManager;
import nz.ac.vuw.ecs.rprofs.server.domain.*;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
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

	public DatasetManager.DatasetBuilder getDatasetBuilder() {
		return createDatasetBuilder();
	}

	public DatasetManager.DatasetBuilder getDatasetUpdater(final Dataset dataset) {
		final DBCollection properties = getCollection(dataset, Dataset.class);
		return new MongoDatasetBuilder() {
			@Override
			public short _getId() {
				return dataset.getId().indexValue();
			}

			@Override
			public void _store(DBObject dataset) {
				properties.update(new BasicDBObject("_id", _getId()), dataset);
			}
		};
	}

	public ClassManager.ClassBuilder getClassBuilder() {
		return createClassBuilder();
	}

	public EventManager.EventBuilder getEventBuilder() {
		return createEventBuilder();
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject<?, T>> T findEntity(@Nullable Id<?, T> id) {
		if (id == null) {
			return null;
		}
		if (Dataset.class.equals(id.getTargetClass())) {
			for (Dataset ds : getDatasets()) {
				if (ds.getId().equals(id)) {
					return id.getTargetClass().cast(ds);
				}
			}
			return null;
		} else {
			Dataset current = context.getDataset();
			DB db = getDatabase(current);
			DBCollection collection = getCollection(db, id.getTargetClass());
			DBObject data = collection.findOne(new BasicDBObject("_id", id.longValue()));
			return getBuilder(id.getTargetClass()).init(data).get();
		}
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

	public <T extends DataObject> boolean deleteEntity(T entity) {

		Dataset dataset;
		if (entity.getClass() == Dataset.class) {
			dataset = (Dataset) entity;
		} else {
			dataset = context.getDataset();
		}

		if (dataset == null) throw new RuntimeException("no dataset in context");

		DB database = getDatabase(dataset);

		if (database == null) throw new RuntimeException("invalid dataset provided");

		DBCollection collection = getCollection(database, entity.getClass());
		collection.remove(new BasicDBObject("_id", entity.getId().longValue()));

		if (entity.getClass() == Dataset.class) {
			mongo.dropDatabase(getDBName((Dataset) entity));
		}

		return true;
	}

	private DB getDatabase() {
		Dataset current = context.getDataset();
		if (current != null) return getDatabase(current);
		else return null;
	}

	private DB getDatabase(@NotNull Dataset dataset) {
		return mongo.getDB(getDBName(dataset));
	}

	private DBCollection getCollection(Class<? extends DataObject> type) {
		DB root = getDatabase();
		if (root != null) return getCollection(root, type);
		else return null;
	}

	private DBCollection getCollection(Dataset dataset, Class<? extends DataObject> type) {
		DB root = getDatabase(dataset);
		if (root != null) return getCollection(root, type);
		else return null;
	}

	private DBCollection getCollection(DB root, Class<? extends DataObject> type) {
		if (type == Dataset.class) {
			return root.getCollection("properties");
		} else if (type == Clazz.class) {
			return root.getCollection("classes");
		} else if (type == Method.class) {
			return root.getCollection("methods");
		} else if (type == Field.class) {
			return root.getCollection("fields");
		} else if (type == Instance.class) {
			return root.getCollection("objects");
		} else if (type == Event.class) {
			return root.getCollection("events");
		} else {
			throw new RuntimeException("type not implemented: " + type);
		}
	}

	private List<Dataset> getDatasets() {
		List<Dataset> result = Lists.newArrayList();
		MongoDatasetBuilder builder = createDatasetBuilder();
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof_")) {
				DBObject properties = mongo.getDB(dbname)
						.getCollection("properties").findOne();
				result.add(builder.init(properties).get());
			}
		}
		return result;
	}

	@VisibleForTesting
	short getNextId() {
		short max = 0;
		for (String dbname : mongo.getDatabaseNames()) {
			if (dbname.startsWith("rprof")) {
				DB db = mongo.getDB(dbname);
				DBObject properties = db.getCollection("properties").findOne();
				short id = ((Long) properties.get("_id")).shortValue();
				if (id > max) max = id;
			}
		}
		return ++max;
	}

	@VisibleForTesting
	String getDBName(Dataset dataset) {
		return "rprof_" + dataset.getHandle() + "_" + dataset.getId().indexValue();
	}

	@SuppressWarnings("unchecked")
	private <T extends DataObject<?, T>> EntityBuilder<T> getBuilder(@NotNull Class<T> type) {
		if (type.equals(Dataset.class)) {
			return EntityBuilder.class.cast(createDatasetBuilder());
		} else if (type.equals(Event.class)) {
			return EntityBuilder.class.cast(createEventBuilder());
		}
		return null;
	}

	private MongoDatasetBuilder createDatasetBuilder() {
		return new MongoDatasetBuilder() {
			@Override
			public short _getId() {
				short max = 0;
				for (String dbname : mongo.getDatabaseNames()) {
					if (dbname.startsWith("rprof")) {
						DB db = mongo.getDB(dbname);
						DBObject properties = db.getCollection("properties").findOne();
						short id = ((Long) properties.get("_id")).shortValue();
						if (id > max) max = id;
					}
				}
				return ++max;
			}

			@Override
			public void _store(DBObject dataset) {
				String dbname = "rprof_" + dataset.get("handle") + "_" + dataset.get("_id");
				DB db = mongo.getDB(dbname);
				DBCollection properties = db.getCollection("properties");
				properties.insert(dataset);
			}
		};
	}

	private MongoEventBuilder createEventBuilder() {
		return new MongoEventBuilder() {
			final DBCollection events = getCollection(Event.class);

			@Override
			void _store(DBObject event) {
				events.insert(event);
			}
		};
	}

	private MongoClassBuilder createClassBuilder() {
		return new MongoClassBuilder() {
			DBCollection classes = getCollection(Clazz.class);

			@Override
			long _nextId() {
				// TODO this is not thread-safe!
				return classes.count() + 1;
			}

			@Override
			void _store(DBObject data) {
				classes.insert(data);
			}
		};
	}
}
