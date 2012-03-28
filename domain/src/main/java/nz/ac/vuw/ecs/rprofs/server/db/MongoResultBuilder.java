package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.util.ResultCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.ResultQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.ResultUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.Result;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ResultId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public abstract class MongoResultBuilder extends MongoBuilder<MongoResultBuilder, ResultId, Result>
		implements ResultCreator<MongoResultBuilder>, ResultQuery<MongoResultBuilder>, ResultUpdater<MongoResultBuilder> {

	@Override
	public MongoResultBuilder init(Result value) {
		reset();
		setId(value.getId());
		setClassName(value.getClassName());
		setPackageName(value.getPackageName());
		setNumObjects(value.getNumObjects());
		setTotals(value.getTotals());
		setCounts(value.getCounts());
		setFields(value.getFields());

		return this;
	}

	@Override
	public MongoResultBuilder setId(ResultId id) {
		b.put("_id", id.getValue());
		return this;
	}

	@Override
	public MongoResultBuilder setClassName(String className) {
		b.put("class", className);
		return this;
	}

	@Override
	public MongoResultBuilder setPackageName(String packageName) {
		b.put("package", packageName);
		return this;
	}

	@Override
	public MongoResultBuilder setNumObjects(int numObjects) {
		b.put("objects", numObjects);
		return this;
	}

	@Override
	public MongoResultBuilder setTotals(int[] totals) {
		BasicDBList list = new BasicDBList();
		for (int i : totals) list.add(i);
		b.put("totals", list);
		return this;
	}

	@Override
	public MongoResultBuilder setCounts(int[] counts) {
		BasicDBList list = new BasicDBList();
		for (int i : counts) list.add(i);
		b.put("counts", list);
		return this;
	}

	@Override
	public MongoResultBuilder setFields(Map<FieldId, Result.FieldInfo> fields) {
		List<DBObject> values = Lists.newArrayList();
		for (Result.FieldInfo field : fields.values()) {
			BasicDBObject b = new BasicDBObject();
			b.append("_id", field.getId().getValue());
			b.append("mutable", field.getMutable());
			b.append("reads", field.getReads());
			b.append("writes", field.getWrites());
			values.add(b);
		}
		this.b.put("fields", values);
		return this;
	}

	@Override
	public Result get() {
		ResultId id = new ResultId(((Long) b.get("_id")).longValue());

		String className = b.containsField("class") ? (String) b.get("class") : null;
		String packageName = b.containsField("package") ? (String) b.get("package") : null;

		int numObjects = (Integer) b.get("objects");

		List<?> totalsList = (List<?>) b.get("totals");
		int[] totals = new int[totalsList.size()];
		for (int i = 0; i < totals.length; i++) {
			totals[i] = (Integer) totalsList.get(i);
		}

		List<?> countsList = (List<?>) b.get("counts");
		int[] counts = new int[countsList.size()];
		for (int i = 0; i < counts.length; i++) {
			counts[i] = (Integer) countsList.get(i);
		}

		Map<FieldId, Result.FieldInfo> fields = Maps.newHashMap();
		if (b.containsField("fields")) {
			for (DBObject f : (List<DBObject>) b.get("fields")) {
				FieldId fid = new FieldId((Long) f.get("_id"));
				Result.FieldInfo info = new Result.FieldInfo(fid,
						(Integer) f.get("mutable"),
						(Integer) f.get("reads"),
						(Integer) f.get("writes"));
				fields.put(fid, info);
			}
		}
		return new Result(id, className, packageName, numObjects, totals, counts, fields);
	}
}
