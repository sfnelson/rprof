function mapImmutable() {
	var equals = (this.firstHashCode || this.firstEquals) ? true : false;
	var immutable = true;
	if (this.fields) {
		this.fields.forEach(function (field) {
			if (field.firstRead && field.lastWrite && (field.firstRead < field.lastWrite)) {
				immutable = false;
			}
		});
	}
	var value = false;
	if (immutable && equals) {
		value = { "immeq" : 1 };
	}
	else if (immutable && !equals) {
		value = { "immneq" : 1 };
	}
	else if (!immutable && equals) {
		value = { "muteq" : 1 };
	}
	else {
		value = { "mutneq" : 1 };
	}
	var cls = this.type;
	if (cls) {
		var cr = db.classes.findOne({"_id":cls});
		if (cr) cls = cr.name;
	}
	emit(cls, value);
}
function reduceImmutable(type, entries) {
	var value = {};
		//"immeq":	0,
		//"immneq":	0,
		//"muteq":	0,
		//"mutneq":	0
	entries.forEach(
		function (entry) {
			if (entry.immeq) {
				if (!value.immeq) value.immeq = entry.immeq;
				else value.immeq += entry.immeq;
			}
			if (entry.immneq) {
				if (!value.immneq) value.immneq = entry.immneq;
				else value.immneq += entry.immneq;
			}
			if (entry.muteq) {
				if (!value.muteq) value.muteq = entry.muteq;
				else value.muteq += entry.muteq;
			}
			if (entry.mutneq) {
				if (!value.mutneq) value.mutneq = entry.mutneq;
				else value.mutneq += entry.mutneq;
			}
		}
	);
	return value;
}
db.instances.mapReduce(mapImmutable, reduceImmutable, {out: { replace: "immeqtypes" }});
