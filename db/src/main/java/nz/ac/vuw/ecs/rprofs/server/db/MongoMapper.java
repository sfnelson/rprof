package nz.ac.vuw.ecs.rprofs.server.db;

import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.db.reports.Mapper;
import nz.ac.vuw.ecs.rprofs.server.db.reports.ReduceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
class MongoMapper<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>>
		implements Runnable {

	private static final long BATCH_SIZE = 64000;

	private static final Logger log = LoggerFactory.getLogger(MongoMapper.class);

	private final Query<?, Input> input;
	private final Mapper<Input, OutId, Output> mapper;
	private final ReduceStore<OutId, Output> store;

	public MongoMapper(Query<?, Input> input, Mapper<Input, OutId, Output> mapper, ReduceStore<OutId, Output> store) {
		this.input = input;
		this.mapper = mapper;
		this.store = store;
	}

	@Override
	public void run() {
		Query.Cursor<? extends Input> q = input.find();
		int numResults = q.count();
		int processed = 0;

		log.info("starting map/reduce...");
		while (q.hasNext()) {
			Input current = q.next();
			mapper.map(current, store);

			if (processed % BATCH_SIZE == 0) {
				log.info("\tprocessed {}/{}", processed, numResults);
			}
			processed++;
		}
		q.close();
		store.flush();
		log.info("finished map/reduce.");
	}
}