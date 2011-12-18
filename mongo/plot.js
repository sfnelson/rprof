var dbs = db.getMongo().getDBNames().sort();
print("Dataset\tImmutable/No Equals\tImmutable/Equals\tMutable/No Equals\tMutable/Equals\tTotal");
for (i in dbs) {
    var d = db.getMongo().getDB(dbs[i]);
    var cols = d.getCollectionNames();
    for (j in cols) {
        if (cols[j] == "TABLE_NAME") {
            var output = d.properties.findOne().benchmark;
            d.TABLE_NAME.find({ _id : "RESULT_SET" }).forEach(function (r) {
                output += "\t" + r.value.immutable.noequals;
                output += "\t" + r.value.immutable.equals;
                output += "\t" + r.value.mutable.noequals;
                output += "\t" + r.value.mutable.equals;
		output += "\t" + r.value.total;
            });
            print(output);
        }
    }
}
