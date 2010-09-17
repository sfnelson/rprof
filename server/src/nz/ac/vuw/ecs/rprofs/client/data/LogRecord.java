package nz.ac.vuw.ecs.rprofs.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LogRecord implements IsSerializable {
	
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

	public static final int ALL = 0xFFF;
	public static final int ALLOCATION = OBJECT_ALLOCATED | OBJECT_TAGGED;
	public static final int METHODS = METHOD_ENTER | METHOD_RETURN;
	public static final int FIELDS = FIELD_READ | FIELD_WRITE;
	public static final int CLASSES = CLASS_WEAVE | CLASS_INITIALIZED;
	
	public long index;
	public long thread;
	public int event;
	public int cnum;
	public int mnum;
	public long[] args;
	
	public LogRecord() {}
	public LogRecord(long index, long thread, int event, int cnum, int mnum, long[] args) {
		this.index = index;
		this.thread = thread;
		this.event = event;
		this.cnum = cnum;
		this.mnum = mnum;
		this.args = args;
	}

	public LogRecord toRPC() {
		return new LogRecord(index, thread, event, cnum, mnum, args);
	}
	
	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass())) return false;
		LogRecord r = (LogRecord) o;
		return r.index == index;
	}
	
	public int hashCode() {
		return new Long(index).hashCode();
	}
}
