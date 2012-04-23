public class Threads extends Thread {
	public Threads() {}

	int count = 0;

	public void run() {
		while (count < 120000) count++;
	}
		
	public static void main(String args[]) {
		new Threads().start();
		new Threads().start();
		new Threads().start();
	}
}
