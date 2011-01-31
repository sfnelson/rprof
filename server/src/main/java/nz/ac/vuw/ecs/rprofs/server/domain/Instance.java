/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.Event.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.Method.MethodId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.LongId;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "instances" )
public class Instance {

	@SuppressWarnings("serial")
	@Embeddable
	public static class InstanceId extends LongId {
		public InstanceId() {}
		public InstanceId(long id) {
			super(id);
		}
		public int getThreadIndex() {
			return getUpper();
		}
		public int getInstanceIndex() {
			return getLower();
		}
	}

	@EmbeddedId
	private InstanceId id;

	@Version
	private int version;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "type_index", referencedColumnName = "index")
	})
	private Class type;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "method_index", referencedColumnName = "index"),
		@JoinColumn(name = "method_owner_index", referencedColumnName = "owner_index")
	})
	protected Method constructor;

	@ManyToMany(mappedBy="args", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	protected List<Event> events;

	public Instance() {}

	public Instance(InstanceId id, Class type, Method constructor, List<? extends Event> events) {
		this.id = id;
		this.type = type;
		this.constructor = constructor;
		this.events = Collections.newList();
		if (events != null) {
			this.events = Collections.newList();
			this.events.addAll(events);
		}
	}

	public long getId() {
		return id.getIndex();
	}

	public int getVersion() {
		return version;
	}

	public InstanceId getInstanceId() {
		return id;
	}

	public long getIndex() {
		return id.getIndex();
	}

	public Class getType() {
		return type;
	}

	public ClassId getTypeId() {
		return type.getClassId();
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Method getConstructor() {
		return constructor;
	}

	public MethodId getConstructorId() {
		return constructor.getAttributeId();
	}

	public void setConstructor(Method m) {
		this.constructor = m;
	}

	public int getThreadIndex() {
		return id.getThreadIndex();
	}

	public int getInstanceIndex() {
		return id.getInstanceIndex();
	}

	public List<Event> getEvents() {
		return events;
	}

	public List<EventId> getEventIds() {
		List<EventId> ids = Collections.newList();
		for (Event e: events) {
			ids.add(e.getEventId());
		}
		return ids;
	}
}
