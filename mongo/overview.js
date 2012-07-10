var dbs = db.getMongo().getDBNames();
function render(db) {
  var props = db.properties.findOne();
  var name = "\\mbox{\\lstinline{"+props.benchmark+"}}";
  var classes = db.classes.count();
  var methods = db.methods.count();
  var fields = db.fields.count();
  var objs = Math.round(db.instances.count()/1000) + "K";
  var events = Math.round(props.events/1000000) + "M";
  var time = Math.round((props.finished - props.started)/(1000*60));
  print(name + " & "
	+ classes + " & "
	+ methods + " & "
	+ fields + " & "
	+ objs + " & "
	+ events + " & "
        + time + " \\\\");
}
for (i in dbs) {
  var d = db.getMongo().getDB(dbs[i]);
  var cols = d.getCollectionNames();
  for (j in cols) {
    if (cols[j] == "instances") render(d);
  }
}
