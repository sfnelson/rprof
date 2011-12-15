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
    init : function (immutable, equals) {
        this[(immutable ? "immutable" : "mutable")] = new EqualsRecord().init(equals);
        return this;
    },
    add : function (o) {
        if (!o) return;
        if (o.immutable) this.immutable = (this.immutable ? this.immutable.add(o.immutable) : o.immutable);
        if (o.mutable) this.mutable = (this.mutable ? this.mutable.add(o.mutable) : o.mutable);
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
    },
    cast : function (obj) {
        obj.__proto__ = ClassPrototype;
        MutabilityPrototype.cast(obj.fieldsFine);
        MutabilityPrototype.cast(obj.fieldsCoarse);
        MutabilityPrototype.cast(obj.constructorReturn);
    }
}
ClassRecord.prototype = ClassPrototype;

function mapInstances() {
    var equals = (this.firstHashCode || this.firstEquals) ? true : false;
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

    var result = new ClassRecord();
    result.fieldsFine.init(fieldsFineImmutable, equals);
    result.fieldsCoarse.init(fieldsCoarseImmutable, equals);
    result.constructorReturn.init(constructorReturnImmutable, equals);

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

function mapClassRecords() {
    emit("fieldsFine", this.value.fieldsFine);
    emit("fieldsCoarse", this.value.fieldsCoarse);
    emit("constructorReturn", this.value.constructorReturn);
}

function reduceClassRecords(key, entries) {
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
            replace: "classResults"
        },
        scope : scope
    }
);

db.classResults.mapReduce(mapClassRecords, reduceClassRecords,
    {
        out : {
            replace: "results"
        },
        scope : scope
    }
);
