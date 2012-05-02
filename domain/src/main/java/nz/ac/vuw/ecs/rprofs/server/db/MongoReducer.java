package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.data.util.Updater;
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
	private final Creator<?, I, T> output;
	private final Updater<?, I, T> update;

	private final Cache<I, T> cache;

	public MongoReducer(Query<I, T> input,
						Creator<?, I, T> output,
						Updater<?, I, T> update,
						Query<I, T> source,
						Reducer<I, T> reducer) {
		this.input = input;
		this.source = source;
		this.reducer = reducer;
		this.output = output;
		this.update = update;

		this.cache = new Cache<I, T>() {
			@Override
			protected void store(I id, T value) {
				MongoReducer.this.store(id, value);
			}
		};
	}

	private void store(I id, T value) {
		T current = source.find(id);
		if (current == null) {
			output.init(value).store();
		} else {
			update.init(reducer.reduce(id, Lists.newArrayList(current, value))).update(id);
		}
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