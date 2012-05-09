package nz.ac.vuw.ecs.rprof.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RProfTest {
	public static void main(String[] args) {
		A t = new A("test");
		t.greet(t);
		t.equals(null);
		t.equals(t);
		t.hashCode();
		t.a = 4;
		System.out.println(t.a);
		t = new A(t);

		B b = new B();
		b.foo();
		b.bar();

		C c = new C(5);
		c.equals(null);
		c.equals(c);

		try {
			c.equals(t);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			c.hashCode();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			new D(5);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		E e = new E();
		e.x = 2;
		e = new E();
		System.out.println(e.y);
		e.x = 2;
		e = new E();
		System.out.println(e.y);
		e.y = 2;

		System.out.println(Thread.currentThread().getName());

		new LinkedList<Object>().add(new CollectionElement());
		new HashSet<Object>().add(new CollectionElement());
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 0; i < 64; i++)
			map.put(new CollectionElement(), new CollectionElement());

		for (int i = 0; i < 1000; i++) {
			final int testFinal = i;
			new Runnable() {
				public void run() {
					if (testFinal % 1 == 0) System.out.print(".");
				}
			}.run();
		}
		System.out.println();

		throw new RuntimeException("test exception in main method");
	}

	static class Base {
		public int a;
		protected Object foo;

		public Base(Object foo) {
			this.foo = foo;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!o.getClass().equals(getClass())) {
				return false;
			}
			return ((Base) o).a == a;
		}
	}

	static class A extends Base {
		public A(Object foo) {
			super(foo);
		}

		public void greet(A t) {
			System.out.println("Hello World!");
		}

		@Override
		public boolean equals(Object o) {
			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return 4;
		}
	}

	static class B {
		private int a;

		public B() {
			a = 1;
		}

		public void foo() {
			a = 2;
		}

		public void bar() {
			if (a == 2) ;
			a = 3;
		}
	}

	static class C {
		private int a;

		public C(int a) {
			this.a = a;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o instanceof C) {
				return ((C) o).a == a;
			}
			throw new NullPointerException("test equals exception");
		}

		@Override
		public int hashCode() {
			throw new NullPointerException("test hashcode exception");
		}
	}

	static class D {
		public final int d;

		public D(int d) {
			this.d = d;
			throw new NullPointerException("test constructor exception");
		}
	}

	static class E {
		public int x;
		public int y;

		public E() {
			x = 1;
			y = 1;
		}
	}

	static class CollectionElement {
	}
}