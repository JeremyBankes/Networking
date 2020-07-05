package com.jeremy.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TCPServer {

	protected ServerSocket socket;
	protected final HashMap<Endpoint, Socket> connected;

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

		connected = new HashMap<>();
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
				final Endpoint client = new Endpoint(clientSocket.getInetAddress(), clientSocket.getPort());
				final InputStream inputStream = clientSocket.getInputStream();
				final OutputStream outputStream = clientSocket.getOutputStream();
				executor.execute(() -> {
					try {
						connected.put(client, clientSocket);
						onConnect(client);
						onReceiveClient(client, inputStream, outputStream);
					} catch (Exception exception) {
						exception.printStackTrace();
					} finally {
						try {
							disconnect(client);
						} catch (IOException exception) {
							onException(exception);
						}
						onDisconnect(client);
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
	 * @param client       The endpoint on which the connected client
	 * @param inputStream  The stream in which client messages can be read
	 * @param outputStream The stream in which messages to the client can be written
	 */
	protected void onReceiveClient(Endpoint client, InputStream inputStream, OutputStream outputStream) {}

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
	 * @param client The endpoint of the newly connected client
	 */
	protected void onConnect(Endpoint client) {}

	/**
	 * Called internally by com.jeremy.networking.TCPServer when a client
	 * disconnects from the server. This method is meant to be overridden.
	 * 
	 * @param client The endpoint of the disconnected client
	 */
	protected void onDisconnect(Endpoint client) {}

	/**
	 * Determines if a client is connected.
	 * 
	 * @param address The endpoint of the potentially connected client
	 * @return Whether the client is connected
	 */
	public boolean isConnected(Endpoint client) {
		return connected.containsKey(client);
	}

	/**
	 * Disconnects a client from the server.
	 * 
	 * @param endpoint The endpoint of the client to disconnect
	 * @throws IOException If an I/O error occurs when closing this socket.
	 */
	public void disconnect(Endpoint endpoint) throws IOException {
		connected.remove(endpoint).close();
	}

	/**
	 * @return All currently connected clients
	 */
	public Set<Endpoint> getConnected() {
		return connected.keySet();
	}

}
