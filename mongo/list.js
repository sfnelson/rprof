var dbs = db.getMongo().getDBNames();
for (i in dbs) {
  var d = db.getMongo().getDB(dbs[i]);
  var cols = d.getCollectionNames();
  for (j in cols) {
    if (cols[j] == "instances") print(dbs[i]);
  }
}
