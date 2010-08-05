public class Test extends A {
	public static void main(String[] args) {
		Test t = new Test("test");
		t.greet(t);
	}
	
	public Test(String foo) {
		super(foo);
	}
	
	public void greet(Test t) {
		System.out.println("Hello World!");
	}
}

class A {
	protected String foo;
	public A(String foo) {
		this.foo = foo;
	}
}
