var dbs = db.getMongo().getDBNames();
print("Dataset\tImmutable/No Equals\tImmutable/Equals\tMutable/No Equals\tMutable/Equals\tTotal");
for (i in dbs) {
    var d = db.getMongo().getDB(dbs[i]);
    var cols = d.getCollectionNames();
    for (j in cols) {
        if (cols[j] == "results") {
            var output = d.properties.findOne().benchmark;
            d.results.find({ _id : "constructorReturn" }).forEach(function (r) {
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
