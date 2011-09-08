/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name="numInstances", query="select count(I) from Instance I where I.owner = :dataset"),
	@NamedQuery(name="allInstances", query="select count(I) from Instance I where I.owner = :dataset"),
	@NamedQuery(name="numInstancesForType", query="select count(I) from Instance I where I.owner = :dataset and I.type = :type"),
	@NamedQuery(name="instancesForType", query="select I from Instance I where I.owner = :dataset and I.type = :type"),
	@NamedQuery(name = "deleteInstances", query = "delete Instance I where I.owner = :dataset")
})
public class Instance implements DataObject<Instance, ObjectId> {

	public static final java.lang.Class<Instance> TYPE = Instance.class;

	@EmbeddedId
	private ObjectId id;

	@Version
	private int version;

	@ManyToOne
	private DataSet owner;

	@ManyToOne
	private Class type;

	@ManyToOne
	protected Method constructor;

	public Instance() {}

	public Instance(DataSet owner, ObjectId id, Class type, Method constructor) {
		this.owner = owner;
		this.id = id;
		this.type = type;
		this.constructor = constructor;
	}

	public ObjectId getId() {
		return id;
	}

	public Long getRpcId() {
		return id.getId();
	}

	public Integer getVersion() {
		return version;
	}

	public DataSet getOwner() {
		return owner;
	}

	public Class getType() {
		return type;
	}

	public ClassId getTypeId() {
		return type.getId();
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Method getConstructor() {
		return constructor;
	}

	public AttributeId<Method> getConstructorId() {
		return constructor.getId();
	}

	public void setConstructor(Method m) {
		this.constructor = m;
	}

	public long getIndex() {
		return id.getId();
	}

	public short getThreadIndex() {
		return id.threadValue();
	}

	public int getInstanceIndex() {
		return id.indexValue();
	}
}
