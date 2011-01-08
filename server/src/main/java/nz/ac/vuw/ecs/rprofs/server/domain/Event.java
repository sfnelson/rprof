/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "events" )
public class Event {

	public static final int OBJECT_ALLOCATED = 0x1;
	public static final int ARRAY_ALLOCATED = 0x2;
	public static final int METHOD_ENTER = 0x4;
	public static final int METHOD_RETURN = 0x8;
	public static final int FIELD_READ = 0x10;
	public static final int FIELD_WRITE = 0x20;
	public static final int CLASS_WEAVE = 0x40;
	public static final int CLASS_INITIALIZED = 0x80;
	public static final int OBJECT_TAGGED = 0x100;
	public static final int OBJECT_FREED = 0x200;
	public static final int METHOD_EXCEPTION = 0x400;

	public static final int ALL = 0xFFF;
	public static final int ALLOCATION = OBJECT_ALLOCATED | OBJECT_TAGGED;
	public static final int METHODS = METHOD_ENTER | METHOD_RETURN | METHOD_EXCEPTION;
	public static final int FIELDS = FIELD_READ | FIELD_WRITE;
	public static final int CLASSES = CLASS_WEAVE | CLASS_INITIALIZED;

	@Transient
	EventId id;

	@Id
	long index;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="index", column=@Column(name="thread_index"))
	})
	InstanceId thread;

	Integer event;

	@ManyToOne
	Class type;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "method_index", referencedColumnName = "index"),
		@JoinColumn(name = "method_owner_index", referencedColumnName = "owner_index")
	})
	Method method;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "field_index", referencedColumnName = "index"),
		@JoinColumn(name = "field_owner_index", referencedColumnName = "owner_index")
	})
	Field field;


	@ElementCollection
	@CollectionTable(name = "args",
			joinColumns=@JoinColumn(name="event_id")
	)
	List<InstanceId> args;

	public Event() {}

	public Event(EventId id, InstanceId thread, int event, Class type, Attribute attr, ArrayList<InstanceId> args) {
		this.id = id;
		this.index = id.index;
		this.thread = thread;
		this.event = event;
		this.type = type;
		this.args = args;

		setAttribute(attr);
	}

	public List<? extends InstanceId> getArguments() {
		return args;
	}

	public List<InstanceId> getArgumentIds() {
		return args;
		/*List<InstanceId> ids = Collections.newList();
		for (Instance i: args) {
			ids.add(i.getId());
		}
		return ids;*/
	}

	public Class getType() {
		return type;
	}

	public ClassId getTypeId() {
		return type.getClassId();
	}

	public int getEvent() {
		return event;
	}

	public EventId getId() {
		if (id == null) {
			id = new EventId(index);
		}
		return id;
	}

	public Attribute getAttribute() {
		return (method == null) ? field : method;
	}

	public AttributeId getAttributeId() {
		return getAttribute().getId();
	}

	public InstanceId getThread() {
		return thread;
	}

	public InstanceId getThreadId() {
		return thread;
	}

	public void setAttribute(Attribute attribute) {
		if (attribute instanceof Method) {
			method = (Method) attribute;
		}
		else {
			field = (Field) attribute;
		}
	}

	public void setType(Class type) {
		this.type = type;
	}
}
