/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import javax.persistence.*;

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
	@NamedQuery(name="numInstances", query="select count(I) from Instance I"),
	@NamedQuery(name="allInstances", query="select count(I) from Instance I"),
	@NamedQuery(name="numInstancesForType", query="select count(I) from Instance I where I.type = :type"),
	@NamedQuery(name="instancesForType", query="select I from Instance I where I.type = :type"),
	@NamedQuery(name = "deleteInstances", query = "delete Instance I")
})
public class Instance implements DataObject<Instance, ObjectId> {

	public static final java.lang.Class<Instance> TYPE = Instance.class;

	@EmbeddedId
	private ObjectId id;

	@Version
	private int version;

	@Transient
	private Dataset owner;

	@ManyToOne
	private Clazz type;

	@ManyToOne
	protected Method constructor;

	public Instance() {}

	public Instance(Dataset owner, ObjectId id, Clazz type, Method constructor) {
		this.owner = owner;
		this.id = id;
		this.type = type;
		this.constructor = constructor;
	}

	public ObjectId getId() {
		return id;
	}

	public Long getRpcId() {
		return id.longValue();
	}

	public Integer getVersion() {
		return version;
	}

	public Dataset getOwner() {
		return owner;
	}

	public Clazz getType() {
		return type;
	}

	public ClassId getTypeId() {
		return type.getId();
	}

	public void setType(Clazz type) {
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
		return id.longValue();
	}

	public short getThreadIndex() {
		return id.threadValue();
	}

	public int getInstanceIndex() {
		return id.indexValue();
	}
}
