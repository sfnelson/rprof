package nz.ac.vuw.ecs.rprof.test;

public class ThreadsTest extends Thread {
	public ThreadsTest() {}

	int count = 0;

	public void run() {
		while (count < 120000) count++;
	}
		
	public static void main(String args[]) {
		new ThreadsTest().start();
		new ThreadsTest().start();
		new ThreadsTest().start();
	}
}
