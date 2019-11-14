package com.jeremy.networking.v1_0;

import static com.jeremy.networking.v1_0.NetworkUtilities.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.jeremy.serialization.v3_0.Bundle;

public class TcpClient {

	private Socket socket;
	private TCPClientReceiveCallback callback;

	private boolean listening;
	private Thread thread;

	private InputStream inputStream;
	private OutputStream outputStream;

	public TcpClient() {
		thread = new Thread(this::run);
	}

	public void connect(String address, int port) throws IOException {
		socket = new Socket(address, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
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
		if (outputStream == null) {
			throw new IOException("Attempted to send bundle to server before successfully connecting");
		}
		outputStream.write(bundle.getBytes());
	}

	private void run() {
		if (!socket.isConnected()) {
			return;
		}

		try {
			while (listening) {
				if (socket.isClosed()) {
					listening = false;
				} else {
					Bundle bundle = readBundleFromStream(inputStream);
					if (getReceiveCallback() != null) {
						getReceiveCallback().receive(bundle);
					}
				}
			}
		} catch (SocketException socketException) {
			System.out.println("Disconnected from server: " + socketException.getMessage());
		}
	}

	public boolean isConnected() {
		return socket != null && !socket.isClosed();
	}

	public TCPClientReceiveCallback getReceiveCallback() {
		return callback;
	}

	public void setReceiveCallback(TCPClientReceiveCallback callback) {
		this.callback = callback;
	}

	public static interface TCPClientReceiveCallback {

		public abstract void receive(Bundle bundle);

	}

}
