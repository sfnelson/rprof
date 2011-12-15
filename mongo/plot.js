var dbs = db.getMongo().getDBNames();
print("Set\tImm/NEq\tImm/Eq\tMut/NEq\tMut/Eq\tTotal");
var output = "";
for (i in dbs) {
    var d = db.getMongo().getDB(dbs[i]);
    var cols = d.getCollectionNames();
    for (j in cols) {
        if (cols[j] == "results") {
            var output = d.properties.findOne().benchmark;
            //var output = "" + count;
            d.results.find({ _id : "fieldsFine" }).forEach(function (r) {
                output += "\t" + r.value.immutable.noequals;
                output += "\t" + r.value.immutable.equals;
                output += "\t" + r.value.mutable.noequals;
                output += "\t" + r.value.mutable.equals;
                output += "\t" + (r.value.immutable.noequals + r.value.immutable.equals
                    + r.value.mutable.noequals + r.value.mutable.equals);
            });
            print(output);
        }
    }
}
