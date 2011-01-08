/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.Collections;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "instances" )
public class Instance implements IsSerializable {

	@Transient
	private InstanceId id;

	@Id
	private Long index;

	@Version
	private int version;

	@ManyToOne
	private Class type;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "method_index", referencedColumnName = "index"),
		@JoinColumn(name = "method_owner_index", referencedColumnName = "owner_index")
	})
	protected Method constructor;

	@Transient
	protected List<Event> events;

	public Instance(InstanceId id, Class type, Method constructor, List<? extends Event> events) {
		this.id = id;
		this.index = id.index;
		this.type = type;
		this.constructor = constructor;
		this.events = Collections.newList();
		if (events != null) {
			this.events = Collections.newList();
			this.events.addAll(events);
		}
	}

	public InstanceId getInstanceId() {
		if (id == null) {
			id = new InstanceId(index);
		}
		return id;
	}

	public Class getType() {
		return type;
	}

	public ClassId getTypeId() {
		return type.getClassId();
	}

	public Method getConstructor() {
		return constructor;
	}

	public MethodId getConstructorId() {
		return constructor.getId();
	}

	public List<? extends Event> getEvents() {
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
