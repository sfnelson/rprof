public class Test extends A {
	public static void main(String[] args) {
		Test t = new Test("test");
		t.greet(t);
		t.equals(null);
		t.equals(t);
		t.hashCode();
		t.a = 4;
		System.out.println(t.a);
		t = new Test(t);

		B b = new B();
		b.foo();
		b.bar();

		C c = new C(5);
		c.equals(null);
		c.equals(c);

		try {
			c.equals(t);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			c.hashCode();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			new D();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println(Thread.currentThread().getName());

		throw new RuntimeException("test exception in main method");
	}

	public Test(Object foo) {
		super(foo);
	}

	public void greet(Test t) {
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

class A {
	public int a;
	protected Object foo;
	public A(Object foo) {
		this.foo = foo;
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(getClass())) {
			return false;
		}
		return ((A) o).a == a;
	}
}

class B {
	private int a;
	public B() {
		a = 1;
	}
	public void foo() {
		a = 2;
	}
	public void bar() {
		if (a == 2);
		a = 3;
	}
}

class C {
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

class D {
	public D() {
		throw new NullPointerException("test constructor exception");
	}
}

