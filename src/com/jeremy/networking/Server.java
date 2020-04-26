package com.jeremy.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server {

	private ServerSocket socket;
	private final HashSet<Socket> connected;

	private boolean running;
	private Thread thread;

	public Server(int port) throws IOException {
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
				final String address = clientSocket.getInetAddress().getHostName();
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
		executor.shutdown();
	}

	/**
	 * Called internally by com.jeremy.networking.Server when a new client connects.
	 * This method is executed within its own thread. When is finishes, the client
	 * will be disconnected. Keep this thread alive to maintain a connection with
	 * the client. This method is meant to be overridden.
	 * 
	 * @param address      The address of the connected client
	 * @param port         The port on which the client has connected
	 * @param inputStream  The stream in which client messages can be read
	 * @param outputStream The stream in which messages to the client can be written
	 */
	protected void onReceiveClient(String address, int port, InputStream inputStream, OutputStream outputStream) {}

	/**
	 * Called internally by com.jeremy.networking.Server if an exception occurs
	 * while handling a client. This method is meant to be overridden.
	 * 
	 * @param exception The exception created when handling a client
	 */
	protected void onException(Exception exception) {}

	/**
	 * Called internally by com.jeremy.networking.Server when a client connects to
	 * the server. This method is meant to be overridden.
	 * 
	 * @param address The address of the connected client
	 * @param port    The port on which the client connected
	 */
	protected void onConnect(String address, int port) {}

	/**
	 * Called internally by com.jeremy.networking.Server when a client disconnects
	 * from the server. This method is meant to be overridden.
	 * 
	 * @param address The address of the disconnected client
	 * @param port    The port on which the client disconnected
	 */
	protected void onDisconnect(String address, int port) {}

	/**
	 * Determines if a client is connected.
	 * 
	 * @param address The address of a potentially connected client
	 * @param port    The port of a potentially connected client
	 * @return Whether the client is connected
	 */
	public boolean isConnected(String address, int port) {
		return connected.stream().anyMatch(socket -> isSocketEqualAddress(socket, address, port));
	}

	/**
	 * Disconnects a client from the server.
	 * 
	 * @param address The address of the client to disconnect
	 * @param port    The port on which the client to disconnect is connected
	 */
	public void disconnect(String address, int port) {
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

	private static boolean isSocketEqualAddress(Socket socket, String address, int port) {
		return socket.getInetAddress().getHostAddress().equals(address) && socket.getPort() == port;
	}

}