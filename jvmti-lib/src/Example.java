/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/02/12
 */
public class Example {

	public static void main(String args[]) {
		Example e = new Example();
		e.hello();
	}

	public String hello() {
		return "Hello!";
	}

	public boolean isHello(String message) {
		return message.equals("Hello!");
	}
}
