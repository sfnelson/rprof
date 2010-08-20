package nz.ac.vuw.ecs.rprofs.client.data;

import java.io.Serializable;

public class LogRecord implements Serializable {
	private static final long serialVersionUID = -2196809197295190606L;
	
	public static final int OBJECT_ALLOCATED = 1;
	public static final int ARRAY_ALLOCATED = 2;
	public static final int METHOD_ENTER = 3;
	public static final int METHOD_RETURN = 4;
	public static final int FIELD_READ = 5;
	public static final int FIELD_WRITE = 6;
	public static final int CLASS_WEAVE = 7;
	public static final int CLASS_INITIALIZED = 8;
	public static final int OBJECT_TAGGED = 9;
	
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
