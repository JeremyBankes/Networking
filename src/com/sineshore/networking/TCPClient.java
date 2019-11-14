package com.sineshore.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPClient {

	public Socket socket;
	public String serverAddress;
	public int serverPort;

	private int timeout;

	private ServerReceiver receiver;

	public void send(byte[] bytes) throws IOException {
		if (bytes.length > Server.PACKET_SIZE)
			throw new IllegalArgumentException("Failed to send data. Packet oversized. (" + bytes.length + " bytes)");
		socket.getOutputStream().write(bytes);
	}

	public OutputStream output() throws IOException {
		return socket.getOutputStream();
	}

	public void connect(String address, int port) throws IOException {
		this.serverAddress = address;
		this.serverPort = port;
		run();
	}

	private void run() throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(serverAddress), serverPort);
		socket.setSoTimeout(timeout);
		socket.setTcpNoDelay(true);
		while (socket.isConnected() && !socket.isClosed()) {
			if (receiver != null) {
				receiver.receive(socket.getInputStream());
			}
		}
	}

	public void stop() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}

	public ServerReceiver getReciever() {
		return receiver;
	}

	public void setReceiver(ServerReceiver receiver) {
		this.receiver = receiver;
	}

	public int getTimeout() throws SocketException {
		return timeout;
	}

	public void setTimeout(int timeout) throws SocketException {
		this.timeout = timeout;
	}

	public boolean isActive() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public String getAddress() {
		return socket.getLocalAddress().getHostAddress();
	}

	public int getPort() {
		return socket.getLocalPort();
	}

	public static interface ServerReceiver {

		public abstract void receive(InputStream input);

	}

}
