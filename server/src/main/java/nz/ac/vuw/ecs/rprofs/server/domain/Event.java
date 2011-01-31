/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.Attribute.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.Class.ClassId;
import nz.ac.vuw.ecs.rprofs.server.domain.Instance.InstanceId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.LongId;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
@Entity
@Table( name = "events" )
public class Event {

	public interface EventVisitor {
		public void visitArrayAllocated(Event e);
		public void visitClassInitialized(Event e);
		public void visitClassWeave(Event e);
		public void visitFieldRead(Event e);
		public void visitFieldWrite(Event e);
		public void visitObjectAllocated(Event e);
		public void visitObjectFreed(Event e);
		public void visitObjectTagged(Event e);
		public void visitMethodEnter(Event e);
		public void visitMethodException(Event e);
		public void visitMethodReturn(Event e);
	}

	public static abstract class AbstractVisitor implements EventVisitor {
		public void visitArrayAllocated(Event e) {}
		public void visitClassInitialized(Event e) {}
		public void visitClassWeave(Event e) {}
		public void visitFieldRead(Event e) {}
		public void visitFieldWrite(Event e) {}
		public void visitObjectAllocated(Event e) {}
		public void visitObjectFreed(Event e) {}
		public void visitObjectTagged(Event e) {}
		public void visitMethodEnter(Event e) {}
		public void visitMethodException(Event e) {}
		public void visitMethodReturn(Event e) {}
	}

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

	@SuppressWarnings("serial")
	@Embeddable
	public static class EventId extends LongId {
		public EventId() {}
		public EventId(long id) {
			super(id);
		}
	}

	@EmbeddedId
	EventId id;

	@Version
	int version;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "thread_index", referencedColumnName = "index")
	})
	Instance thread;

	Integer event;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "class_index", referencedColumnName = "index")
	})
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

	@ManyToMany
	@JoinTable(
			joinColumns = { @JoinColumn(name = "instance_index", referencedColumnName = "index") },
			inverseJoinColumns = { @JoinColumn(name = "event_index", referencedColumnName = "index") }
	)
	List<Instance> args;

	public Event() {}

	public Event(EventId id, Instance thread, int event, Class type, Attribute attr, ArrayList<Instance> args) {
		this.id = id;
		this.thread = thread;
		this.event = event;
		this.type = type;
		this.args = args;

		setAttribute(attr);
	}

	public List<? extends Instance> getArguments() {
		return args;
	}

	public List<InstanceId> getArgumentIds() {
		List<InstanceId> ids = Collections.newList();
		for (Instance i: args) {
			ids.add(i.getInstanceId());
		}
		return ids;
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

	public long getId() {
		return id.getIndex();
	}

	public EventId getEventId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Field getField() {
		return field;
	}

	public Method getMethod() {
		return method;
	}

	public Attribute getAttribute() {
		return (method == null) ? field : method;
	}

	public AttributeId getAttributeId() {
		return getAttribute().getAttributeId();
	}

	public Instance getThread() {
		return thread;
	}

	public InstanceId getThreadId() {
		return thread.getInstanceId();
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

	public void visit(EventVisitor visitor) {
		switch (getEvent()) {
		case OBJECT_ALLOCATED:
			visitor.visitObjectAllocated(this); break;
		case ARRAY_ALLOCATED:
			visitor.visitArrayAllocated(this); break;
		case METHOD_ENTER:
			visitor.visitMethodEnter(this); break;
		case METHOD_RETURN:
			visitor.visitMethodReturn(this); break;
		case FIELD_READ:
			visitor.visitFieldRead(this); break;
		case FIELD_WRITE:
			visitor.visitFieldWrite(this); break;
		case CLASS_WEAVE:
			visitor.visitClassWeave(this); break;
		case CLASS_INITIALIZED:
			visitor.visitClassInitialized(this); break;
		case OBJECT_TAGGED:
			visitor.visitObjectTagged(this); break;
		case OBJECT_FREED:
			visitor.visitObjectFreed(this); break;
		case METHOD_EXCEPTION:
			visitor.visitMethodException(this); break;
		}
	}
}
