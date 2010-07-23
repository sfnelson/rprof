public class Test {
	public static void main(String[] args) {
		Test t = new Test();
		t.greet(t);
	}
	
	public void greet(Test t) {
		System.out.println("Hello World!");
	}
}

