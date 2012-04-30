package nz.ac.vuw.ecs.rprofs.server.db;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClassSummaryCreator;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClassSummaryQuery;
import nz.ac.vuw.ecs.rprofs.server.data.util.ClassSummaryUpdater;
import nz.ac.vuw.ecs.rprofs.server.domain.ClassSummary;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassSummaryId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.FieldId;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 27/03/12
 */
public abstract class MongoClassSummaryBuilder extends MongoBuilder<MongoClassSummaryBuilder, ClassSummaryId, ClassSummary>
		implements ClassSummaryCreator<MongoClassSummaryBuilder>, ClassSummaryQuery<MongoClassSummaryBuilder>, ClassSummaryUpdater<MongoClassSummaryBuilder> {

	@Override
	public MongoClassSummaryBuilder init(ClassSummary value) {
		reset();
		setId(value.getId());
		setClassName(value.getClassName());
		setPackageName(value.getPackageName());
		setNumObjects(value.getNumObjects());
		setEqCol(value.getEqCol());
		setEq(value.getEq());
		setCol(value.getCol());
		setNone(value.getNone());
		setFields(value.getFields());

		return this;
	}

	@Override
	public MongoClassSummaryBuilder setId(ClassSummaryId id) {
		b.put("_id", id.getValue());
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setClassName(String className) {
		b.put("class", className);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setPackageName(String packageName) {
		b.put("package", packageName);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setNumObjects(int numObjects) {
		b.put("objects", numObjects);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setEqCol(int[] eqcol) {
		BasicDBList list = new BasicDBList();
		for (int i : eqcol) list.add(i);
		b.put("eqcol", list);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setEq(int[] eq) {
		BasicDBList list = new BasicDBList();
		for (int i : eq) list.add(i);
		b.put("eq", list);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setCol(int[] col) {
		BasicDBList list = new BasicDBList();
		for (int i : col) list.add(i);
		b.put("col", list);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setNone(int[] none) {
		BasicDBList list = new BasicDBList();
		for (int i : none) list.add(i);
		b.put("none", list);
		return this;
	}

	@Override
	public MongoClassSummaryBuilder setFields(Map<FieldId, ClassSummary.FieldInfo> fields) {
		List<DBObject> values = Lists.newArrayList();
		for (ClassSummary.FieldInfo field : fields.values()) {
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
	public ClassSummary get() {
		ClassSummaryId id = new ClassSummaryId(((Long) b.get("_id")).longValue());

		String className = b.getString("class");
		String packageName = b.getString("package");

		int numObjects = b.getInt("objects", 0);

		List<?> eqcolList = (List<?>) b.get("eqcol");
		int[] eqcol = new int[eqcolList.size()];
		for (int i = 0; i < eqcol.length; i++) {
			eqcol[i] = (Integer) eqcolList.get(i);
		}

		List<?> eqList = (List<?>) b.get("eq");
		int[] eq = new int[eqList.size()];
		for (int i = 0; i < eq.length; i++) {
			eq[i] = (Integer) eqList.get(i);
		}

		List<?> colList = (List<?>) b.get("col");
		int[] col = new int[colList.size()];
		for (int i = 0; i < col.length; i++) {
			col[i] = (Integer) colList.get(i);
		}

		List<?> noneList = (List<?>) b.get("none");
		int[] none = new int[noneList.size()];
		for (int i = 0; i < none.length; i++) {
			none[i] = (Integer) noneList.get(i);
		}

		Map<FieldId, ClassSummary.FieldInfo> fields = Maps.newHashMap();
		if (b.containsField("fields")) {
			for (DBObject f : (List<DBObject>) b.get("fields")) {
				FieldId fid = new FieldId((Long) f.get("_id"));
				ClassSummary.FieldInfo info = new ClassSummary.FieldInfo(fid,
						(Integer) f.get("mutable"),
						(Integer) f.get("reads"),
						(Integer) f.get("writes"));
				fields.put(fid, info);
			}
		}
		ClassSummary cs = new ClassSummary(id, numObjects, eqcol, eq, col, none, fields);
		cs.setClassName(className);
		cs.setPackageName(packageName);
		return cs;
	}
}
