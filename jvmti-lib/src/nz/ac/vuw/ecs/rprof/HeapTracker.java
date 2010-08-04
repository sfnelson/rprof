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

public class HeapTracker {

	private static int engaged = 0;

	private static native void _newobj(Object thread, int cnum, int mnum, Object o, long id);
	public static void newobj(int cnum, int mnum, Object o) {
		if ( engaged != 0 && !(o instanceof HeapTracker)) {
			_newobj(Thread.currentThread(), cnum, mnum, o, id(o));
		}
	}

	private static native void _newarr(Object thread, Object a, long id);
	public static void newarr(Object a) {
		if ( engaged != 0 ) {
			_newarr(Thread.currentThread(), a, id(a));
		}
	}

	private static native void _menter(Object thread, int cnum, int mnum, Object[] args);
	public static void enter(int cnum, int mnum, Object[] args) {
		if ( engaged != 0 ) {
			_menter(Thread.currentThread(), cnum, mnum, args);
		}
	}

	private static native void _mexit(Object thread, int cnum, int mnum);
	public static void exit(int cnum, int mnum) {
		if ( engaged != 0) {
			_mexit(Thread.currentThread(), cnum, mnum);
		}
	}

	private static native void _main(Object thread, int cnum, int mnum);
	public static void main(int cnum, int mnum) {
		if (engaged != 0) {
			_main(Thread.currentThread(), cnum, mnum);
		}
	}

	private static HeapTracker nullCounter;
	private static HeapTracker getTracker() {
		HeapTracker c;
		Thread current = Thread.currentThread();
		if (current == null) {
			c = nullCounter;
			if (c == null) {
				c = new HeapTracker();
				nullCounter = c;
			}
		} else {
			c = _getTracker(current);
			if (c == null) {
				c = new HeapTracker();
				_setTracker(current, c);
			}
		}

		return c;
	}
	
	public static HeapTracker _getTracker(Thread current) {
		return null; // return current._rprof;
	}

	public static void _setTracker(Thread current, HeapTracker tracker) {
		// current._rprof = tracker;
	}

	private static long id(Object o) {
		if (o instanceof HeapTracker) {
			return nextThreadId;
		}

		try {
			return getTracker().newId();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	static {
		nextThreadId = (1l << 32);
		//map = new WeakHashMap<Thread, HeapTracker>();
	}

	private static volatile long nextThreadId;
	//private static final WeakHashMap<Thread, HeapTracker> map;

	private static synchronized long nextThreadId() {
		long id = nextThreadId;
		nextThreadId = id + (1l << 32);
		return id;
	}

	private final long threadId;
	private long counter = 0;

	private HeapTracker() {
		threadId = nextThreadId();
	}

	public long newId() {
		return threadId | ++counter;
	}
}

