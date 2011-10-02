package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.reports.MapReduce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
abstract class MongoMapReduce<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>,
		TmpBuilder extends EntityBuilder<TmpBuilder, OutId, Output> & Creator<TmpBuilder, OutId, Output>>
		implements Runnable {

	private static final long BATCH_SIZE = 64000;

	private static final Logger log = LoggerFactory.getLogger(MongoMapReduce.class);

	private final Query<?, Input> input;
	private final Creator<?, OutId, Output> output;
	private final MapReduce<Input, OutId, Output> mapReduce;

	private final Map<OutId, List<BasicDBObject>> cache = Maps.newHashMap();

	private DBCollection tmp;

	public MongoMapReduce(Query<?, Input> input, Creator<?, OutId, Output> output,
						  MapReduce<Input, OutId, Output> mapReduce) {
		this.input = input;
		this.output = output;
		this.mapReduce = mapReduce;

		tmp = getTmpCollection();
	}

	protected abstract TmpBuilder getTmpBuilder();

	protected abstract DBCollection getTmpCollection();

	@SuppressWarnings("unchecked")
	public void run() {
		Query.Cursor<? extends Input> input = this.input.find();
		TmpBuilder tmpBuilder = getTmpBuilder();
		int numResults = input.count();
		int processed = 0;

		final HashMap<OutId, List<Output>> cache = Maps.newHashMap();
		MapReduce.Emitter emitter = new MapReduce.Emitter<OutId, Output>() {
			@Override
			public void emit(OutId key, Output value) {
				List<Output> values;
				if (cache.containsKey(key)) {
					values = cache.get(key);
				} else {
					values = Lists.newArrayList();
					cache.put(key, values);
				}
				values.add(value);
			}
		};

		log.info("starting map...");
		while (input.hasNext()) {
			Input current = input.next();
			mapReduce.map(current, emitter);

			if (processed % BATCH_SIZE == 0) {
				flushCache(cache, tmpBuilder);

				log.info("\tprocessed {}/{}", processed, numResults);
			}
			processed++;
		}
		input.close();
		flushCache(cache, tmpBuilder);
		log.info("finished map.");

		tmp.createIndex(new BasicDBObject("id", 1));

		log.info("starting reduce...");
		numResults = (int) tmp.count();
		processed = 0;
		while (true) {
			DBObject one = tmp.findOne();
			if (one == null) break;
			Long id = (Long) one.get("id");
			List<Output> values = Lists.newArrayList();
			DBCursor cursor = tmp.find(new BasicDBObject("id", id));
			Output result;
			while (true) {
				if (values.size() > BATCH_SIZE || !cursor.hasNext()) {
					result = mapReduce.reduce(id, values);
					if (!cursor.hasNext()) break;
					else {
						values.clear();
						values.add(result);
					}
				}

				values.add(tmpBuilder.init(cursor.next()).get());

				if (processed % BATCH_SIZE == 0) {
					log.info("\tprocessed {}/{}", processed, numResults);
				}
				processed++;
			}
			cursor.close();
			if (result != null) {
				output.init(result).store();
			}
			tmp.remove(new BasicDBObject("id", id));
		}
		log.info("finished reduce.");

		log.info("cleaning up...");
		tmp.drop();
		log.info("all done.");
	}

	private void flushCache(Map<OutId, List<Output>> cache, TmpBuilder output) {
		for (OutId id : cache.keySet()) {
			Output value = mapReduce.reduce(id.getValue(), cache.get(id));
			output.init(value).store();
		}
		cache.clear();
	}
}