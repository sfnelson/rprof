/*
 * @(#)HeapTracker.java	1.4 05/11/17
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package nz.ac.vuw.ecs.rprof;

/* Java class to hold static methods which will be called in byte code
 *    injections of all class files.
 */

/**
 * Provides static methods which are called from the byte code injected by the
 * static weaver. Provides facility for generating unique ids for objects.
 * <p/>
 * Object ids are 64 bits:
 * <dl>
 * <dt>0-15</dt><dd>These bits are reserved for the database.</dd>
 * <dt>16-31</dt><dd>These bits identify the thread that created the object.</dd>
 * <dt>32-63</dt><dd>These bits identify the object - unique within a thread.</dd>
 * </dl>
 */
public class HeapTracker {

	private static int engaged;
	private static int cnum;
	private static HeapTracker nullCounter;
	private static volatile short nextThreadId;

	public static HeapTracker create() {
		return new HeapTracker();
	}

	private static native void _clinit(Object cls);

	public static void clinit(Object cls) {
		if (engaged != 0) {
			_clinit(cls);
		}
	}

	private static native void _newcls(Object cls, int cnum, int[] fieldsToWatch);

	public static void newcls(Object cls, int cnum, int[] fieldsToWatch) {
		/**
		 * We need this to run before engage is set. This is safe because it's only called from JNI.
		 */
		_newcls(cls, cnum, fieldsToWatch);
	}

	private static native void _newobj(Object thread, Class cls, Object o, long id);

	public static void newobj(Class cls, Object o) {
		if (engaged != 0) {
			_newobj(Thread.currentThread(), cls, o, id(o));
		}
	}

	private static native void _newarr(Object thread, Object a, long id);

	public static void newarr(Object a) {
		if (engaged != 0) {
			_newarr(Thread.currentThread(), a, id(a));
		}
	}

	private static native void _menter(Object thread, int cnum, int mnum, Object[] args);

	public static void enter(int cnum, int mnum, Object[] args) {
		if (engaged != 0) {
			_menter(Thread.currentThread(), cnum, mnum, args);
		}
	}

	private static native void _mexit(Object thread, int cnum, int mnum, Object arg);

	public static void exit(int cnum, int mnum, Object arg) {
		if (engaged != 0) {
			_mexit(Thread.currentThread(), cnum, mnum, arg);
		}
	}

	private static native void _mexcept(Object thread, int cnum, int mnum, Object arg, Object err);

	public static void except(int cnum, int mnum, Object arg, Throwable err) {
		if (engaged != 0) {
			_mexcept(Thread.currentThread(), cnum, mnum, arg, err);
		}
	}

	private static native void _main(Object thread, int cnum, int mnum);

	public static void main(int cnum, int mnum) {
		if (engaged != 0) {
			_main(Thread.currentThread(), cnum, mnum);
		}
	}

	private static long id(Object o) {
		return getTracker().newId();
	}

	private static HeapTracker getTracker() {
		HeapTracker c = null;
		Thread current = Thread.currentThread();

		if (current != null) {
			c = _getTracker(current);
		}

		if (c == null) {
			c = nullCounter;
		}

		return c;
	}

	public static HeapTracker _getTracker(Thread current) {
		return null; // return current._rprof;
	}

	public static void _setTracker(Thread current, HeapTracker tracker) {
		// current._rprof = tracker;
	}

	private static synchronized short nextThreadId() {
		short id = nextThreadId;
		nextThreadId = id++;
		return id;
	}

	static {
		engaged = 0;
		nextThreadId = 1;
		nullCounter = new HeapTracker((short) 0);
	}

	private final long thread;
	private long counter = 0;

	private HeapTracker() {
		this(nextThreadId());
	}

	private HeapTracker(short threadId) {
		this.thread = ((long) threadId) << 32;
	}

	public long newId() {
		return (thread == 0) ? 0 : (thread | ++counter);
	}
}

/* Empty version for testing */

/*public class HeapTracker {

	private static int engaged = 0;

	public static HeapTracker create() {
		return new HeapTracker();
	}

	private static native void _newcls(Object cls, int cnum, int[] fieldsToWatch);
	public static void newcls(Object cls, int cnum, int[] fieldsToWatch) {}

	private static native void _newobj(Object thread, Object o, long id);
	public static void newobj(Object o) {}

	private static native void _newarr(Object thread, Object a, long id);
	public static void newarr(Object a) {}

	private static native void _menter(Object thread, int cnum, int mnum, Object[] args);
	public static void enter(int cnum, int mnum, Object[] args) {}

	private static native void _mexit(Object thread, int cnum, int mnum, Object arg);
	public static void exit(int cnum, int mnum, Object arg) {}

	private static native void _main(Object thread, int cnum, int mnum);
	public static void main(int cnum, int mnum) {}

	public static HeapTracker _getTracker(Thread current) {
		return null; // return current._rprof;
	}

	public static void _setTracker(Thread current, HeapTracker tracker) {
		// current._rprof = tracker;
	}
}*/