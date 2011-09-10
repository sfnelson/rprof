/**
 *
 */
package nz.ac.vuw.ecs.rprofs.server.domain;

import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
import nz.ac.vuw.ecs.rprofs.server.model.Attribute;
import nz.ac.vuw.ecs.rprofs.server.model.DataObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * @author Stephen Nelson (stephen@sfnelson.org)
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
		public void visitArrayAllocated(Event e) {
		}

		public void visitClassInitialized(Event e) {
		}

		public void visitClassWeave(Event e) {
		}

		public void visitFieldRead(Event e) {
		}

		public void visitFieldWrite(Event e) {
		}

		public void visitObjectAllocated(Event e) {
		}

		public void visitObjectFreed(Event e) {
		}

		public void visitObjectTagged(Event e) {
		}

		public void visitMethodEnter(Event e) {
		}

		public void visitMethodException(Event e) {
		}

		public void visitMethodReturn(Event e) {
		}
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

	@NotNull
	private EventId id;

	@Nullable
	private Instance thread;

	@NotNull
	private Integer event;

	@Nullable
	private Clazz type;

	@Nullable
	private Method method;

	@Nullable
	private Field field;

	@Nullable
	private List<Instance> args;

	public Event() {
	}

	public Event(@NotNull EventId id, @NotNull Integer event) {
		this.id = id;
		this.event = event;
	}

	@NotNull
	public EventId getId() {
		return id;
	}

	@NotNull
	public Long getRpcId() {
		return id.longValue();
	}

	@NotNull
	public Integer getVersion() {
		return 0;
	}

	@Nullable
	public Instance getThread() {
		return thread;
	}

	@NotNull
	public Event setThread(@Nullable Instance thread) {
		this.thread = thread;
		return this;
	}

	@Nullable
	public Clazz getType() {
		return type;
	}

	@NotNull
	public Event setType(@Nullable Clazz type) {
		this.type = type;
		return this;
	}

	@NotNull
	public Integer getEvent() {
		return event;
	}

	@Nullable
	public Field getField() {
		return field;
	}

	@Nullable
	public Method getMethod() {
		return method;
	}

	@Nullable
	public Attribute<?> getAttribute() {
		return (method == null) ? field : method;
	}

	@NotNull
	public Event setAttribute(@NotNull Attribute<?> attribute) {
		if (attribute instanceof Method) {
			method = (Method) attribute;
		} else {
			field = (Field) attribute;
		}
		return this;
	}

	@Nullable
	public List<Instance> getArgs() {
		if (this.args == null || this.args.isEmpty()) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(args);
		}
	}

	@Nullable
	public Instance getFirstArg() {
		List<Instance> args = this.args;
		if (args != null && args.size() >= 1) {
			return args.get(0);
		}

		return null;
	}

	@NotNull
	public Event setArgs(@Nullable List<Instance> args) {
		this.args = args;
		return this;
	}

	public void visit(EventVisitor visitor) {
		switch (getEvent()) {
			case OBJECT_ALLOCATED:
				visitor.visitObjectAllocated(this);
				break;
			case ARRAY_ALLOCATED:
				visitor.visitArrayAllocated(this);
				break;
			case METHOD_ENTER:
				visitor.visitMethodEnter(this);
				break;
			case METHOD_RETURN:
				visitor.visitMethodReturn(this);
				break;
			case FIELD_READ:
				visitor.visitFieldRead(this);
				break;
			case FIELD_WRITE:
				visitor.visitFieldWrite(this);
				break;
			case CLASS_WEAVE:
				visitor.visitClassWeave(this);
				break;
			case CLASS_INITIALIZED:
				visitor.visitClassInitialized(this);
				break;
			case OBJECT_TAGGED:
				visitor.visitObjectTagged(this);
				break;
			case OBJECT_FREED:
				visitor.visitObjectFreed(this);
				break;
			case METHOD_EXCEPTION:
				visitor.visitMethodException(this);
				break;
		}
	}

	public void visit(DomainVisitor visitor) {
		// TODO visitEvent()
	}
}
