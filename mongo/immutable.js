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
	if (immutable && equals) {
		emit("immeq",{count:1});
	}
	else if (immutable && !equals) {
		emit("immneq",{count:1});
	}
	else if (!immutable && equals) {
		emit("muteq",{count:1});
	}
	else {
		emit("mutneq",{count:1});
	}
}
function reduceImmutable(type, entries) {
	var count = 0;
	entries.forEach(
		function (entry) {
			count += entry.count;
		}
	);
	return {count: count};
}
db.instances.mapReduce(mapImmutable, reduceImmutable, {out: { replace: "immeq" }});
