package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.reports.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 6/12/11
 */
abstract class MongoReducer<I extends Id<I, T>, T extends DataObject<I, T>> implements Reducer.ReducerTask {

	private static final long BATCH_SIZE = 64000;

	private static final Logger log = LoggerFactory.getLogger(MongoReducer.class);

	private final Query<I, T> input;
	private final Query<I, T> source;
	private final Reducer<I, T> reducer;

	private final OutputCache<I, T> cache;

	public MongoReducer(Query<I, T> input,
						Creator<?, I, T> output,
						Query<I, T> source,
						Reducer<I, T> reducer) {
		this.input = input;
		this.source = source;
		this.reducer = reducer;

		this.cache = new OutputCache<I, T>(output);
	}

	protected abstract void cleanup();

	@Override
	public void reduce() {
		log.info("starting reduce...");
		Query.Cursor<? extends T> q = input.find();
		int numResults = q.count();
		int processed = 0;

		while (q.hasNext()) {
			T result = q.next();
			I id = result.getId();

			if (cache.containsKey(id)) {
				result = reducer.reduce(id, Lists.newArrayList(cache.get(id), result));
			} else {
				T current = source.find(id);
				if (current != null) {
					result = reducer.reduce(id, Lists.newArrayList(current, result));
				}
			}

			cache.put(id, result);

			processed++;
			if (processed % BATCH_SIZE == 0) {
				log.info("\tprocessed {}/{}", processed, numResults);
			}
		}
		q.close();
		cache.flush();
		log.info("finished reduce.");

		log.info("cleaning up...");
		cleanup();
		log.info("all done.");
	}

}