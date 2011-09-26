function mapImmutable() {
	var read = {};
	var immutable = true;
	var equals = false;
	var inEquals = 0;
	this.events.forEach(function (event) {
		if (event.event == 0x10) { // field read
			read[event.name] = true;
			if (inEquals > 0) equals = true;
		}
		else if (event.event == 0x20) { // field write
			if (read[event.name]) {
				immutable = false;
			}
		}
		else if (event.event == 0x4 && event.name == "equals") {
			inEquals++;
		}
		else if (event.event == 0x8 && event.name == "equals") {
			inEquals--;
		}
		else if (event.event == 0x400 && event.name == "equals") {
			inEquals--;
		}
	});
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
