function mapInstances() {
  emit( this.event , { count : 1} );
}
function reduceInstances(type, entries) {
  var result = { count : 0 };
  entries.forEach(
    function (entry) {
      result.count += entry.count;
    }
  );
  return result;
}
db.events.mapReduce(mapInstances, reduceInstances, { out : { replace : "type-counts" } });
