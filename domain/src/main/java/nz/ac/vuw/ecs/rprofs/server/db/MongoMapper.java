package nz.ac.vuw.ecs.rprofs.server.db;

import com.google.common.collect.Lists;
import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
import nz.ac.vuw.ecs.rprofs.server.model.Id;
import nz.ac.vuw.ecs.rprofs.server.reports.Mapper;
import nz.ac.vuw.ecs.rprofs.server.reports.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 29/09/11
 */
class MongoMapper<Input extends DataObject<?, Input>, OutId extends Id<OutId, Output>,
		Output extends DataObject<OutId, Output>>
		implements Mapper.MapTask<Input> {

	private static final long BATCH_SIZE = 64000;

	private static final Logger log = LoggerFactory.getLogger(MongoMapper.class);

	private final Query<?, Input> input;
	private final Mapper<Input, OutId, Output> mapper;
	private final Reducer<OutId, Output> reducer;
	private final Emitter emitter;

	private final OutputCache<OutId, Output> cache;

	public MongoMapper(Query<?, Input> input,
					   Creator<?, OutId, Output> output,
					   Mapper<Input, OutId, Output> mapper,
					   Reducer<OutId, Output> reducer) {

		this.input = input;
		this.mapper = mapper;
		this.reducer = reducer;
		this.emitter = new Emitter();

		this.cache = new OutputCache<OutId, Output>(output);
	}

	@Override
	public void map() {
		Query.Cursor<? extends Input> q = input.find();
		int numResults = q.count();
		int processed = 0;

		log.info("starting map...");
		while (q.hasNext()) {
			Input current = q.next();
			mapper.map(current, emitter);

			if (processed % BATCH_SIZE == 0) {
				log.info("\tprocessed {}/{}", processed, numResults);
			}
			processed++;
		}
		q.close();
		flush();
		log.info("finished map.");
	}

	@Override
	public void mapVolatile(Input input) {
		mapper.map(input, emitter);
	}

	@Override
	public void flush() {
		cache.flush();
	}

	private class Emitter implements nz.ac.vuw.ecs.rprofs.server.reports.Emitter<OutId, Output> {
		@Override
		public void emit(OutId key, Output value) {
			if (cache.containsKey(key)) {
				value = reducer.reduce(key, Lists.newArrayList(cache.get(key), value));
			}
			cache.put(key, value);
		}
	}
}