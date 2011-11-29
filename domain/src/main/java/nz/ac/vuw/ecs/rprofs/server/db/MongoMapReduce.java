package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.reports.MapReduce;
import nz.ac.vuw.ecs.rprofs.server.reports.MapReduceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
abstract class MongoMapReduce<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>,
		TmpBuilder extends EntityBuilder<TmpBuilder, OutId, Output> & Creator<TmpBuilder, OutId, Output>>
		implements MapReduceTask<Input> {

	private static final long BATCH_SIZE = 64000;

	private static final Logger log = LoggerFactory.getLogger(MongoMapReduce.class);

	private final Database database;

	private final Query<?, Input> input;
	private final Creator<?, OutId, Output> output;
	private final MapReduce<Input, OutId, Output> mapReduce;
	private final HashMap<OutId, List<Output>> cache;
	private final Emitter emitter;

	private final DBCollection tmp;
	private final TmpBuilder tmpBuilder;

	public MongoMapReduce(Database database,
						  Query<?, Input> input,
						  Creator<?, OutId, Output> output,
						  MapReduce<Input, OutId, Output> mapReduce) {
		this.database = database;

		this.input = input;
		this.output = output;
		this.mapReduce = mapReduce;
		this.cache = Maps.newHashMap();
		this.emitter = new Emitter();

		tmp = getTmpCollection();
		tmpBuilder = getTmpBuilder();
	}

	protected abstract TmpBuilder getTmpBuilder();

	protected abstract DBCollection getTmpCollection();

	@Override
	public void run() {
		map();
		reduce();
	}

	@Override
	public void map() {
		Query.Cursor<? extends Input> input = this.input.find();
		int numResults = input.count();
		int processed = 0;

		log.info("starting map...");
		while (input.hasNext()) {
			Input current = input.next();
			mapReduce.map(current, emitter);

			if (processed % BATCH_SIZE == 0) {
				flushToTmp();

				log.info("\tprocessed {}/{}", processed, numResults);
			}
			processed++;
		}
		input.close();
		flushToTmp();
		log.info("finished map.");
	}

	@Override
	public void mapVolatile(Input input) {
		mapReduce.map(input, emitter);
	}

	@Override
	public void flush() {
		flushToTmp();
	}

	@Override
	public void reduce() {
		log.info("starting reduce...");
		int numResults = (int) tmp.count();
		int processed = 0;
		DBCursor cursor = tmp.find();
		while (cursor.hasNext()) {
			Output result = tmpBuilder.init(cursor.next()).get();
			Output current = database.findEntity(result.getId());
			if (current != null) {
				result = mapReduce.reduce(result.getId().getValue(), Lists.newArrayList(result, current));
			}
			output.init(result).store();

			if (processed % BATCH_SIZE == 0) {
				log.info("\tprocessed {}/{}", processed, numResults);
			}
			processed++;
		}
		log.info("finished reduce.");

		log.info("cleaning up...");
		tmp.drop();
		log.info("all done.");
	}

	private void flushToTmp() {
		for (OutId id : cache.keySet()) {
			Output value = mapReduce.reduce(id.getValue(), cache.get(id));
			tmpBuilder.init(value).store();
		}
		cache.clear();
	}

	private class Emitter implements MapReduce.Emitter<OutId, Output> {
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
	}
}