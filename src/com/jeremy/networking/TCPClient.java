package com.jeremy.networking;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

	protected Socket socket;

	private Thread thread;

	/**
	 * Attempts to make a connection to the given serverAddress:serverPort
	 * 
	 * @param serverAddress The address of a listening
	 *                      com.jeremy.networking.TCPServer
	 * @param serverPort    The port of the listening server
	 * @throws UnknownHostException If the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          If an I/O error occurs when creating the socket.
	 */
	public void connect(String serverAddress, int serverPort) throws UnknownHostException, IOException {
		if (!isConnected()) {
			socket = new Socket(serverAddress, serverPort);
			if (isConnected()) {
				thread = new Thread(this::run, "client-thread");
				thread.start();
			}
		}
	}

	private void run() {
		try {
			onConnect();
			onServerContact(socket.getInputStream());
			disconnect();
			onDisconnect();
		} catch (IOException exception) {
			onException(exception);
		}
	}

	public void send(byte[] bytes) throws IOException {
		socket.getOutputStream().write(bytes);
	}

	/**
	 * Called internally by com.jeremy.networking.TCPClient when the client
	 * successfully makes a connection to the server.
	 */
	protected void onConnect() {}

	/**
	 * Called internally by com.jeremy.networking.TCPClient when the client is
	 * disconnected from the server.
	 */
	protected void onDisconnect() {}

	/**
	 * Called internally by com.jeremy.networking.TCPClient if an exception occurs
	 * while communicating with the server. This method is meant to be overridden.
	 * 
	 * @param exception The exception created when communicating with the server
	 */
	protected void onException(Exception exception) {}

	/**
	 * Called internally by com.jeremy.networking.TCPClient when the client makes a
	 * successful connection to the server. This method is executed within its own
	 * thread. When is finishes, the client will be disconnected. Keep this thread
	 * alive to maintain a connection with the server. This method is meant to be
	 * overridden.
	 * 
	 * @param inputStream The stream in which server messages can be read
	 */
	protected void onServerContact(InputStream inputStream) {}

	public String getLocalAddress() {
		return socket == null ? null : socket.getLocalAddress().getHostAddress();
	}

	public String getRemoteAddress() {
		return socket == null ? null : ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	/**
	 * Disconnects from the server. (Closes internal socket)
	 * 
	 * @throws IOException If an I/O error occurs when closing this socket.
	 */
	public void disconnect() throws IOException {
		if (socket != null) socket.close();
	}

}
