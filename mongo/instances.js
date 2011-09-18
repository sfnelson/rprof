function emit(k,v) {
  print("emit");
  print("  k:" + k + " v:" + tojson(v));
}
function mapInstances() {
  if (!this.args || !this.args[0]) return;
  var event = {};
  event._id = this._id;
  event.event = this.event;
  if (this.method) {
    event.name = db.methods.findOne({_id: this.method}).name;
  }
  else if (this.field) {
    event.name = db.fields.findOne({_id: this.field}).name;
  }
  var result = { events : [ event ] };
  if (event.name == "<init>" && event.event == 8) {
    result.constructorReturn = event._id;
  }
  emit( this.args[0], result );
}
function reduceInstances(instance, entries) {
  var result = { events : [] };
  entries.forEach(
    function (entry) {
      result.events = Array.sort(
        Array.concat(result.events, entry.events),
        function (a,b) { return a._id - b._id; }
      );
      var constructorReturn = (function (a, b) {
        if (!a) return b;
        if (!b) return a;
	return (a < b) ? b : a;
      })(result.constructorReturn, entry.constructorReturn);
      if (constructorReturn) {
        result.constructorReturn = constructorReturn;
      }
    }
  );
  return result;
}
function finalizeInstances(id, entry) {
  var result = { _id : id };
  entry.events.forEach(function (event) {
    if (event.event == 32 || event.event == 16) { // field write or read
      if (!entry.fields) entry.fields = {};
      if (!entry.fields[event.name]) entry.fields[event.name] = { creads: 0, cwrites: 0, reads: 0, writes: 0 };
      if (entry.constructorReturn && entry.constructorReturn > event._id) {
        if (event.event == 16) entry.fields[event.name].creads++;
        if (event.event == 32) entry.fields[event.name].cwrites++;
      }
      else {
        if (event.event == 16) entry.fields[event.name].reads++;
        if (event.event == 32) entry.fields[event.name].writes++;
      }
    }
  });
  return entry;
}
db.events.mapReduce(mapInstances, reduceInstances, { finalize : finalizeInstances, out : { replace : "instances" }});
db.instances.find().forEach(function (r) { db.instances.update({_id:r._id}, r.value)})
