package com.sineshore.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {

	private DatagramSocket socket;
	public InetAddress serverAddress;
	public int serverPort;
	private boolean listening;
	private ServerReciever reciever;

	public void connect(String address, int port) throws UnknownHostException, SocketException {
		this.serverPort = port;
		this.serverAddress = InetAddress.getByName(address);
		listening = true;
		run();
	}

	public void send(byte[] bytes) throws IOException {
		if (bytes.length > Server.PACKET_SIZE) {
			System.out.println("Failed to send data. Packet oversized. (" + bytes.length + " bytes)");
			return;
		}
		DatagramPacket send = new DatagramPacket(bytes, bytes.length, serverAddress, serverPort);
		socket.send(send);
	}

	public void stop() {
		listening = false;
		if (socket != null) socket.close();
	}

	private void run() {
		try {
			socket = new DatagramSocket();
			while (listening) {
				if (reciever != null) {
					DatagramPacket recieve = new DatagramPacket(new byte[Server.PACKET_SIZE], Server.PACKET_SIZE);
					socket.receive(recieve);
					reciever.recieve(recieve.getData());
				}
			}
		} catch (SocketException e) {
			if (listening) listening = false;
		} catch (IOException e) {
			e.printStackTrace();
			stop();
		}
	}

	public ServerReciever getReciever() {
		return reciever;
	}

	public void setReciever(ServerReciever reciever) {
		this.reciever = reciever;
	}

	public String getAddress() {
		return socket.getLocalAddress().getHostAddress();
	}

	public int getPort() {
		return socket.getLocalPort();
	}

	public static interface ServerReciever {

		public abstract void recieve(byte[] data);

	}

}
