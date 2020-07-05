package com.jeremy.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TCPServer {

	protected ServerSocket socket;
	protected final HashSet<Socket> connected;

	private boolean running;
	private Thread thread;

	/**
	 * Creates a TCPServer and binds it to the specified port on the local machine.
	 * 
	 * @param port The port on which to listen for client connections.
	 * @throws IOException If an I/O error occurs when opening the socket.
	 */
	public TCPServer(int port) throws IOException {
		socket = new ServerSocket(port);
		socket.setSoTimeout(1000);

		connected = new HashSet<>();
	}

	/**
	 * Starts the server's listening for client connections.
	 */
	public void start() {
		if (!running) {
			thread = new Thread(this::run, "server-thread");
			running = true;
			thread.start();
		}
	}

	/**
	 * Stops the server listening for client connections. This method will block
	 * until the server has completely shutdown.
	 */
	public void stop() {
		if (running) {
			running = false;
			try {
				thread.join();
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
	}

	private void run() {
		ExecutorService executor = Executors.newCachedThreadPool();
		while (running) {
			try {
				final Socket clientSocket = socket.accept();
				final InetAddress address = clientSocket.getInetAddress();
				final int port = clientSocket.getPort();
				final InputStream inputStream = clientSocket.getInputStream();
				final OutputStream outputStream = clientSocket.getOutputStream();
				executor.execute(() -> {
					try {
						connected.add(clientSocket);
						onConnect(address, port);
						onReceiveClient(address, port, inputStream, outputStream);
					} catch (Exception exception) {
						exception.printStackTrace();
					} finally {
						disconnect(address, port);
						onDisconnect(address, port);
					}
				});
			} catch (SocketTimeoutException exception) { //
			} catch (IOException exception) {
				onException(exception);
			}
		}
		try {
			socket.close();
		} catch (IOException exception) {
			onException(exception);
		}
		executor.shutdown();
	}

	/**
	 * Called internally by com.jeremy.networking.TCPServer when a new client
	 * connects. This method is executed within its own thread. When is finishes,
	 * the client will be disconnected. Keep this thread alive to maintain a
	 * connection with the client. This method is meant to be overridden.
	 * 
	 * @param address      The address of the connected client
	 * @param port         The port on which the client has connected
	 * @param inputStream  The stream in which client messages can be read
	 * @param outputStream The stream in which messages to the client can be written
	 */
	protected void onReceiveClient(InetAddress address, int port, InputStream inputStream, OutputStream outputStream) {}

	/**
	 * Called internally by com.jeremy.networking.TCPServer if an exception occurs
	 * while handling a client. This method is meant to be overridden.
	 * 
	 * @param exception The exception created when handling a client
	 */
	protected void onException(Exception exception) {}

	/**
	 * Called internally by com.jeremy.networking.TCPServer when a client connects
	 * to the server. This method is meant to be overridden.
	 * 
	 * @param address The address of the connected client
	 * @param port    The port on which the client connected
	 */
	protected void onConnect(InetAddress address, int port) {}

	/**
	 * Called internally by com.jeremy.networking.TCPServer when a client
	 * disconnects from the server. This method is meant to be overridden.
	 * 
	 * @param address The address of the disconnected client
	 * @param port    The port on which the client disconnected
	 */
	protected void onDisconnect(InetAddress address, int port) {}

	/**
	 * Determines if a client is connected.
	 * 
	 * @param address The address of a potentially connected client
	 * @param port    The port of a potentially connected client
	 * @return Whether the client is connected
	 */
	public boolean isConnected(InetAddress address, int port) {
		return connected.stream().anyMatch(socket -> isSocketEqualAddress(socket, address, port));
	}

	/**
	 * Disconnects a client from the server.
	 * 
	 * @param address The address of the client to disconnect
	 * @param port    The port on which the client to disconnect is connected
	 */
	public void disconnect(InetAddress address, int port) {
		connected.removeIf(socket -> {
			if (isSocketEqualAddress(socket, address, port)) {
				try {
					socket.close();
				} catch (IOException exception) {
					exception.printStackTrace();
				}
				return true;
			}
			return false;
		});
	}

	private static boolean isSocketEqualAddress(Socket socket, InetAddress address, int port) {
		return socket.getInetAddress() == address && socket.getPort() == port;
	}

	/**
	 * @return All currently connected clients
	 */
	public HashSet<Socket> getConnected() {
		return connected;
	}

}
