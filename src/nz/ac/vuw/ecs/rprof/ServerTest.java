package nz.ac.vuw.ecs.rprof;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerTest {

	public static void main(String args[]) throws IOException {
		DatagramSocket socket = new DatagramSocket();

		byte[] buffer = "testing 1,2,3".getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), 9035);
		socket.send(packet);
	}
}
