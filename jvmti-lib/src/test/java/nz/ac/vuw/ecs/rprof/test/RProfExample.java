package nz.ac.vuw.ecs.rprof.test;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 28/02/12
 */
public class RProfExample {

	public static void main(String args[]) {
		RProfExample e = new RProfExample();
		e.hello();
	}

	public String hello() {
		return "Hello!";
	}

	public boolean isHello(String message) {
		return message.equals("Hello!");
	}
}
