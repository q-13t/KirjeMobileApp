package edu.chat.kirje;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public class UDPOperator extends Thread {
	private final int PORT;
	private static DatagramChannel datagram;
	private static SocketAddress client;
	private boolean serverActive = true;

	UDPOperator()  {
		PORT = new Random().nextInt(10001);
		try {
			InetSocketAddress address = new InetSocketAddress("localhost", PORT);
			datagram = DatagramChannel.open();
			datagram.bind(address);
			System.out.println("UDP Datagram Started");
			System.out.println("UPD Is Located At: " + address);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static UDPOperator startServer() {
		try {
			return new UDPOperator();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean sendMessage(String message)  {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
			client = new InetSocketAddress("localhost", 8085);
			datagram.send(buffer, client);
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return  false;
		}
	}

	@Override
	public void run() {
		try {
			while (serverActive) {
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				client = datagram.receive(buffer);
				buffer.flip();
				byte[] bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				String msg = new String(bytes);
				System.out.println("Client: " + client + " | Message: " + msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

