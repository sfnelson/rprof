var dbs = db.getMongo().getDBNames();
for (i in dbs) {
  var d = db.getMongo().getDB(dbs[i]);
  d.dropDatabase();
}
