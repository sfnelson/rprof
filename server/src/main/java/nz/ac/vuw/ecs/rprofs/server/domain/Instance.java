/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "instances" )
public class Instance implements DataObject<Instance> {

	public static final java.lang.Class<Instance> TYPE = Instance.class;

	@EmbeddedId
	private ObjectId id;

	@Version
	private int version;

	@ManyToOne
	@JoinColumn(name = "type_id")
	private Class type;

	@ManyToOne
	@JoinColumn(name = "constructor_id")
	protected Method constructor;

	@ManyToMany(mappedBy="args", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	protected List<Event> events;

	public Instance() {}

	public Instance(ObjectId id, Class type, Method constructor, List<? extends Event> events) {
		this.id = id;
		this.type = type;
		this.constructor = constructor;
		this.events = Collections.newList();
		if (events != null) {
			this.events = Collections.newList();
			this.events.addAll(events);
		}
	}

	public ObjectId getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
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
		return id.getThread();
	}

	public int getInstanceIndex() {
		return id.getIndex();
	}

	public List<Event> getEvents() {
		return events;
	}

	public List<EventId> getEventIds() {
		List<EventId> ids = Collections.newList();
		for (Event e: events) {
			ids.add(e.getId());
		}
		return ids;
	}
}
