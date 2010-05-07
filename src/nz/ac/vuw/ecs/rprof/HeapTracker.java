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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.IdentityHashMap;

/* Java class to hold static methods which will be called in byte code
 *    injections of all class files.
 */

public class HeapTracker {
	
	static {
		nextThreadId = (1 << 24);
		map = new IdentityHashMap<Thread, HeapTracker>();
		nullCounter = new HeapTracker();
	}

	private static int engaged = 0;

	private static native void _newobj(Object thread, Object o, long id);
	public static void newobj(Object o) {
		if ( engaged != 0 && !(o instanceof HeapTracker)) {
			_newobj(Thread.currentThread(), o, id(o));
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
		if ( engaged != 0 && Thread.currentThread() != null) {
			_mexit(Thread.currentThread(), cnum, mnum);
		}
	}
	
	private static native void _main();
	public static void main() {
		engaged = 2;
		
		getTracker().log("main method called");
		
		_main();
	}
	
	private static HeapTracker getTracker() {
		HeapTracker c;
		if (Thread.currentThread() == null) {
			c = nullCounter;
		} else {
			c = map.get(Thread.currentThread());
		}

		if (c == null) {
			c = new HeapTracker();
			map.put(Thread.currentThread(), c);
		}
		return c;
	}

	private static long id(Object o) {
		if (o instanceof HeapTracker) {
			return nextThreadId;
		}
		
		return getTracker().newId();
	}

	private static final HeapTracker nullCounter;
	private static final IdentityHashMap<Thread, HeapTracker> map;

	private static volatile long nextThreadId;

	private static synchronized long nextThreadId() {
		long id = nextThreadId;
		nextThreadId = id + (1 << 24);
		return id;
	}

	private final long threadId;
	private long counter = 0;
	private DatagramSocket socket;

	private HeapTracker() {
		threadId = nextThreadId();
		
	}

	public long newId() {
		return threadId | ++counter;
	}
	
	public void log(String message) {
		message = threadId + ": " + message;
		byte[] m = message.getBytes();
		DatagramPacket packet;
		try {
			packet = new DatagramPacket(m, m.length, InetAddress.getLocalHost(), 9035);
			socket().send(packet);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private DatagramSocket socket() {
		if (socket == null) {
			try {
				socket = new DatagramSocket();
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}
		return socket;
	}
}

