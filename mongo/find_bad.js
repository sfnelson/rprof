function findBad() {
	var bad = db.field.results.find({declaredFinal:true,final:false},{_id:1,name:1});
	bad.forEach(function (f) {
		printjson(f.name);
		var cls = db.fields.findOne({_id:f._id}).owner;
		printjson(db.instances.findOne({fields:{$elemMatch:{_id:f._id,writes:{$gt:1}}}},{fields:1}));
	});
}
