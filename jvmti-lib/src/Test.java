public class Test extends A {
	public static void main(String[] args) {
		Test t = new Test("test");
		t.greet(t);
		t.equals(null);
		t.hashCode();
	}
	
	public Test(String foo) {
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
	protected String foo;
	public A(String foo) {
		this.foo = foo;
	}
}
