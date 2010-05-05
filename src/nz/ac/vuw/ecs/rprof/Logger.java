package nz.ac.vuw.ecs.rprof;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Logger {

	public static final int BUFFER_SIZE = 256;
	
	public static void main(String args[]) throws IOException {
		DatagramSocket socket = new DatagramSocket(90935);
		
		byte[] buffer = new byte[BUFFER_SIZE];
		
		while (true) {
			DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
			socket.receive(packet);
			
			System.out.println(new String(buffer));
		}
	}
}
