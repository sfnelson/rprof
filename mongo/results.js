function EqualsRecord() {
    this.__proto__ = EqualsPrototype;
}
var EqualsPrototype = {
    init : function (equals) {
        if (equals) this.equals = 1;
        else this.noequals = 1;
        return this;
    },
    add : function (o) {
        if (!o) return;
        if (o.equals) this.equals = (this.equals || 0) + o.equals;
        if (o.noequals) this.noequals = (this.noequals || 0) + o.noequals;
        return this;
    },
    cast : function (obj) {
        if (!obj) return;
        obj.__proto__ = EqualsPrototype;
    }
}
EqualsRecord.prototype = EqualsPrototype;

function MutabilityRecord() {
    this.__proto__ = MutabilityPrototype;
}
var MutabilityPrototype = {
    total : 0,
    init : function (immutable, equals) {
        this[(immutable ? "immutable" : "mutable")] = new EqualsRecord().init(equals);
        this.total = 1;
        return this;
    },
    add : function (o) {
        if (!o) return;
        if (o.immutable) this.immutable = (this.immutable ? this.immutable.add(o.immutable) : o.immutable);
        if (o.mutable) this.mutable = (this.mutable ? this.mutable.add(o.mutable) : o.mutable);
        this.total += o.total;
        return this;
    },
    inc : function (o) {
        if (!o) return;
        this.total = 1;
        if (o.immutable) {
            this.immutable = new EqualsRecord();
            if (o.immutable.equals) this.immutable.equals = 1;
            if (o.immutable.noequals) this.immutable.noequals = 1;
        }
        if (o.mutable) {
            this.mutable = new EqualsRecord();
            if (o.mutable.equals) this.mutable.equals = 1;
            if (o.mutable.noequals) this.mutable.noequals = 1;
        }
        return this;
    },
    cast : function (obj) {
        if (!obj) return;
        obj.__proto__ = MutabilityPrototype;
        EqualsPrototype.cast(obj.immutable);
        EqualsPrototype.cast(obj.mutable);
    }
}
MutabilityRecord.prototype = MutabilityPrototype;

function ClassRecord() {
    this.fieldsFine = new MutabilityRecord();
    this.fieldsCoarse = new MutabilityRecord();
    this.constructorReturn = new MutabilityRecord();
    this.firstEquals = new MutabilityRecord();
    this.composite = new MutabilityRecord();
    this.__proto__ = ClassPrototype;
}
var ClassPrototype = {
    add : function (o) {
        if (!o) return;
        this.name = this.name || o.name;
        this["package"] = this["package"] || o["package"];
        this.fieldsFine.add(o.fieldsFine);
        this.fieldsCoarse.add(o.fieldsCoarse);
        this.constructorReturn.add(o.constructorReturn);
        this.firstEquals.add(o.firstEquals);
        this.composite.add(o.composite);
    },
    cast : function (obj) {
        obj.__proto__ = ClassPrototype;
        MutabilityPrototype.cast(obj.fieldsFine);
        MutabilityPrototype.cast(obj.fieldsCoarse);
        MutabilityPrototype.cast(obj.constructorReturn);
        MutabilityPrototype.cast(obj.firstEquals);
        MutabilityPrototype.cast(obj.composite);
    }
}
ClassRecord.prototype = ClassPrototype;

function mapInstances() {
    var equals = (this.firstHashCode || this.firstEquals) ? true : false;
    var firstEquals = null;
    if (this.firstHashCode) firstEquals = this.firstHashCode;
    if (this.firstEquals && (!firstEquals || this.firstEquals < firstEquals)) firstEquals = this.firstEquals;

    var fieldsAreImmutable = true;
    var firstRead = false;
    var lastWrite = false;
    if (this.fields) {
        this.fields.forEach(function (field) {
            if (field.firstRead && field.lastWrite && (field.firstRead < field.lastWrite)) {
                fieldsAreImmutable = false;
            }
            if (field.firstRead && (!firstRead || field.firstRead < firstRead)) {
                firstRead = field.firstRead;
            }
            if (field.lastWrite && (!lastWrite || field.lastWrite > lastWrite)) {
                lastWrite = field.lastWrite;
            }
        });
    }

    var fieldsFineImmutable = fieldsAreImmutable;
    var fieldsCoarseImmutable = (!firstRead || !lastWrite || firstRead > lastWrite);
    var constructorReturnImmutable = (!this.constructorReturn || !lastWrite || this.constructorReturn > lastWrite);
    var firstEqualsImmutable = (equals && firstEquals > lastWrite);
    var composite = fieldsCoarseImmutable || constructorReturnImmutable;

    var result = new ClassRecord();
    result.fieldsFine.init(fieldsFineImmutable, equals);
    result.fieldsCoarse.init(fieldsCoarseImmutable, equals);
    result.constructorReturn.init(constructorReturnImmutable, equals);
    result.firstEquals.init(firstEqualsImmutable, equals);
    result.composite.init(composite, equals);

    if (this.type) {
        var cr = db.classes.findOne({"_id": this.type});
        if (cr) result.name = cr.name;
        if (cr) result.package = cr.package;
    }
    emit(this.type, result);
}

function reduceInstances(type, entries) {
    var result = new ClassRecord();
    entries.forEach(
        function (entry) {
            ClassPrototype.cast(entry);
            result.add(entry);
        }
    );
    return result;
}

function mapInstanceSummary() {
    emit("fieldsFine", this.value.fieldsFine);
    emit("fieldsCoarse", this.value.fieldsCoarse);
    emit("constructorReturn", this.value.constructorReturn);
    emit("firstEquals", this.value.firstEquals);
    emit("composite", this.value.composite);
}

function reduceInstanceSummary(key, entries) {
    var result = new MutabilityRecord();
    entries.forEach(
        function (record) {
            MutabilityPrototype.cast(record);
            result.add(record);
        }
    );
    return result;
}

function mapClassSummary() {
    emit("fieldsFine", new MutabilityRecord().inc(this.value.fieldsFine));
    emit("fieldsCoarse", new MutabilityRecord().inc(this.value.fieldsCoarse));
    emit("constructorReturn", new MutabilityRecord().inc(this.value.constructorReturn));
    emit("firstEquals", new MutabilityRecord().inc(this.value.firstEquals));
    emit("composite", new MutabilityRecord().inc(this.value.composite));
}

function reduceClassSummary(key, entries) {
    var result = new MutabilityRecord();
    entries.forEach(
        function (record) {
            MutabilityPrototype.cast(record);
            result.add(record);
        }
    );
    return result;
}

var scope = {
    ClassRecord : ClassRecord,
    ClassPrototype : ClassPrototype,
    MutabilityRecord : MutabilityRecord,
    MutabilityPrototype : MutabilityPrototype,
    EqualsRecord : EqualsRecord,
    EqualsPrototype : EqualsPrototype
};

db.instances.mapReduce(mapInstances, reduceInstances,
    {
        out : {
            replace: "rawClassResults"
        },
        scope : scope
    }
);


db.rawClassResults.mapReduce(mapClassSummary, reduceClassSummary,
    {
        out : {
            replace: "resultsClasses"
        },
        scope : scope
    }
)

db.rawClassResults.mapReduce(mapInstanceSummary, reduceInstanceSummary,
    {
        out : {
            replace: "resultsInstances"
        },
        scope : scope
    }
);
