package com.jeremy.networking.v1_0;

import static com.jeremy.networking.v1_0.NetworkUtilities.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import com.jeremy.serialization.v3_0.Bundle;

public class TcpServer {

	private ServerSocket socket;
	private TcpServerReceiveCallback callback;
	private NetworkEventCallback connectCallback;
	private NetworkEventCallback disconnectCallback;

	private HashMap<String, Client> connectedClients;

	private boolean running;
	private Thread thread;

	public TcpServer() {
		connectedClients = new HashMap<String, Client>();
	}

	public void start(int port) throws IOException {
		running = true;
		thread = new Thread(this::run, "tcp-server");
		thread.setDaemon(true);
		socket = new ServerSocket(port);
		thread.start();
	}

	public void stop() {
		running = false;
	}

	private void run() {
		while (running) {
			try {
				Socket clientSocket = socket.accept();
				final String key = key(clientSocket);
				new Thread(() -> {
					if (!clientSocket.isConnected()) {
						return;
					}
					try {
						Object disconnectInfo = null;
						final InputStream inputStream = clientSocket.getInputStream();
						final OutputStream outputStream = clientSocket.getOutputStream();
						connectedClients.put(key, new Client(clientSocket, outputStream));
						if (getConnectCallback() != null)
							getConnectCallback().onEvent(key, null);
						try {
							while (!clientSocket.isClosed()) {
								try {
									Bundle bundle = readBundleFromStream(inputStream);
									if (getReceieveCallback() != null) {
										getReceieveCallback().receive(key, bundle);
									}
								} catch (ArrayIndexOutOfBoundsException outOfBoundsException) {
									disconnectInfo = outOfBoundsException.getMessage();
									clientSocket.close();
								}
							}
						} catch (SocketException socketException) {
							disconnectInfo = socketException.getMessage();
						}
						if (getDisconnectCallback() != null)
							getDisconnectCallback().onEvent(key, disconnectInfo);
					} catch (Exception exception) {
						exception.printStackTrace();
					} finally {
						connectedClients.remove(key);
					}
				}).start();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	public void send(String address, Bundle bundle) throws IOException {
		connectedClients.get(address).sendBundle(bundle);
	}

	public void disconnect(String address) throws IOException {
		connectedClients.get(address).socket.close();
	}

	public boolean isClient(String address) {
		return connectedClients.containsKey(address);
	}

	public boolean isRunning() {
		return running;
	}

	public TcpServerReceiveCallback getReceieveCallback() {
		return callback;
	}

	public void setReceiveCallback(TcpServerReceiveCallback callback) {
		this.callback = callback;
	}

	public NetworkEventCallback getConnectCallback() {
		return connectCallback;
	}

	public void setConnectCallback(NetworkEventCallback connectCallback) {
		this.connectCallback = connectCallback;
	}

	public NetworkEventCallback getDisconnectCallback() {
		return disconnectCallback;
	}

	public void setDisconnectCallback(NetworkEventCallback disconnectCallback) {
		this.disconnectCallback = disconnectCallback;
	}

	public void broadcast(Bundle bundle) throws IOException {
		for (Client client : connectedClients.values()) {
			client.sendBundle(bundle);
		}
	}

	public static interface TcpServerReceiveCallback {

		public abstract void receive(String address, Bundle bundle);

	}

	public static interface NetworkEventCallback {

		public abstract void onEvent(String address, Object info);

	}

	private static class Client {

		private Socket socket;
		private OutputStream outputStream;

		public Client(Socket socket, OutputStream outputStream) {
			this.socket = socket;
			this.outputStream = outputStream;
		}

		public void sendBundle(Bundle bundle) throws IOException {
			outputStream.write(bundle.getBytes());
		}

	}

}
