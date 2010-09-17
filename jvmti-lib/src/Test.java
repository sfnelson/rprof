public class Test extends A {
	public static void main(String[] args) {
		Test t = new Test("test");
		t.greet(t);
		t.equals(null);
		t.hashCode();
		t.a = 4;
		System.out.println(t.a);
		t = new Test(t);
	}
	
	public Test(Object foo) {
		super(foo);
	}
	
	public void greet(Test t) {
		System.out.println("Hello World!");
	}
	
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
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
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(getClass())) {
			return false;
		}
		return ((A) o).a == a;
	}
}
