package com.jeremy.networking.v1_0;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.jeremy.serialization.v3_0.Bundle;

public class UdpClient {

	public static final int BUFFER_SIZE = 1024;

	private DatagramSocket socket;
	private UDPClientReceiveCallback callback;

	private InetAddress address;
	private int port;

	private boolean listening;
	private Thread thread;

	public UdpClient() {
		thread = new Thread(this::run);
	}

	public void connect(String address, int port) throws IOException {
		socket = new DatagramSocket();
		this.address = InetAddress.getByName(address);
		this.port = port;
	}

	public void disconnect() throws IOException, InterruptedException {
		if (isListening()) {
			ignore();
		}
		if (socket != null) {
			socket.close();
		}
	}

	public void listen() {
		listening = true;
		thread.start();
	}

	public boolean isListening() {
		return listening;
	}

	public void ignore() throws InterruptedException {
		listening = false;
	}

	public void send(Bundle bundle) throws IOException {
		byte[] buffer = bundle.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(packet);
	}

	private void run() {
		try {
			while (listening) {
				if (socket.isClosed()) {
					listening = false;
				}
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
				socket.receive(packet);
				Bundle bundle = new Bundle(packet.getData());
				if (getReceiveCallback() != null) {
					getReceiveCallback().receive(bundle);
				}
			}
		} catch (SocketException socketException) {
			System.out.println("Disconnected from server: " + socketException.getMessage());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public boolean isConnected() {
		return socket != null && !socket.isClosed();
	}

	public int getLocalPort() {
		return socket.getLocalPort();
	}

	public UDPClientReceiveCallback getReceiveCallback() {
		return callback;
	}

	public void setReceiveCallback(UDPClientReceiveCallback callback) {
		this.callback = callback;
	}

	public static interface UDPClientReceiveCallback {

		public abstract void receive(Bundle bundle);

	}

}
