/**
 * 
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.Id;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
 *
 */
public class Event implements DataObject<Event, EventId> {

	public static final java.lang.Class<Event> TYPE = Event.class;

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

	@EmbeddedId
	EventId id;

	@Version
	Integer version;

	@ManyToOne
    DataSet owner;

	@ManyToOne
	Instance thread;

	Integer event;

	@ManyToOne
	Class type;

	@ManyToOne
	Method method;

	@ManyToOne
	Field field;

	@OneToMany(cascade=CascadeType.ALL)
	Set<Argument> args;

	private static final Comparator<Argument> comparator = new Comparator<Argument>() {
		@Override
		public int compare(Argument a, Argument b) {
			return a.position - b.position;
		}
	};

	public Event() {}

	public Event(DataSet owner, EventId id, Instance thread, int event, Class type, Attribute<?> attr, ArrayList<Instance> args) {
		this.owner = owner;
		this.id = id;
		this.thread = thread;
		this.event = event;
		this.type = type;

		this.args = new TreeSet<Argument>();
		for (int i = 0; i < args.size(); i++) {
			Argument a = new Argument(i, args.get(i));
			this.args.add(a);
		}

		setAttribute(attr);
	}

	public Set<? extends Argument> getArguments() {
		return args;
	}

	public List<Instance> getArgs() {
		if (this.args.isEmpty()) return Collections.emptyList();
		List<Argument> args = Collections.newList();
		args.addAll(this.args);
		Collections.sort(args, comparator);
		List<Instance> result = Collections.newList();
		for (Argument a: this.args) {
			result.add(a.parameter);
		}
		return result;
	}

	public List<ObjectId> getArgumentIds() {
		List<ObjectId> ids = Collections.newList();
		for (Argument a: args) {
			ids.add(a.getParameter().getId());
		}
		return ids;
	}

	public Class getType() {
		return type;
	}

	public Id<Class> getTypeId() {
		return type.getId();
	}

	public int getEvent() {
		return event;
	}

	public EventId getId() {
		return id;
	}

	public Long getRpcId() {
		return id.getId();
	}

	public long getEventId() {
		return id.getId();
	}

	public Integer getVersion() {
		return version;
	}

	public Field getField() {
		return field;
	}

	public Method getMethod() {
		return method;
	}

	public Attribute<?> getAttribute() {
		return (method == null) ? field : method;
	}

	public AttributeId<?> getAttributeId() {
		return getAttribute().getId();
	}

	public Instance getThread() {
		return thread;
	}

	public ObjectId getThreadId() {
		return thread.getId();
	}

	public void setAttribute(Attribute<?> attribute) {
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

	public Instance getFirstArg() {
		for (Argument a: args) {
			if (a.position == 0) {
				return a.parameter;
			}
		}

		return null;
	}
}
