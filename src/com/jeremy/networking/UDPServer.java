package com.jeremy.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPServer {

	protected DatagramSocket serverSocket;
	private int receivePacketSize;

	private boolean running;
	private Thread thread;

	/**
	 * Creates a UDPServer and binds it to the specified port on the local machine.
	 * 
	 * @param port              The port on which to listen for client packets.
	 * @param receivePacketSize The buffer size for receiving client packets.
	 * @throws SocketException If the socket could not be opened, or the socket
	 *                         could not bound to the specified local port.
	 */
	public UDPServer(int port, int receivePacketSize) throws SocketException {
		this.receivePacketSize = receivePacketSize;
		serverSocket = new DatagramSocket(port);
		serverSocket.setSoTimeout(1000);
	}

	/**
	 * Starts the server's listening for client packets.
	 */
	public void start() {
		if (!running) {
			thread = new Thread(this::run, "server-thread");
			running = true;
			thread.start();
		}
	}

	/**
	 * Stops the server listening for client packets. This method will block until
	 * the server has completely shutdown.
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
		while (running) {
			try {
				byte[] buffer = new byte[receivePacketSize];
				DatagramPacket packet = new DatagramPacket(buffer, receivePacketSize);
				serverSocket.receive(packet);
				onReceivePacket(packet.getAddress(), packet.getPort(), packet.getData());
			} catch (SocketTimeoutException exception) { //
			} catch (IOException exception) {
				onException(exception);
			}
		}
	}

	/**
	 * Called internally by com.jeremy.networking.UDPServer when a packet is
	 * received from a client. This method is meant to be overridden.
	 * 
	 * @param address The address of the client who send the packet
	 * @param port    The port on which the client send the packet
	 * @param data    The data in the received packet
	 */
	protected void onReceivePacket(InetAddress address, int port, byte[] data) {}

	/**
	 * Called internally by com.jeremy.networking.UDPServer if an exception occurs
	 * while handling a packet. This method is meant to be overridden.
	 * 
	 * @param exception The exception created when handling a client
	 */
	protected void onException(Exception exception) {}

	/**
	 * Sends a packet to a given address and port
	 * 
	 * @param address The address to which the packet is being sent
	 * @param port    The port on which the packet is to be sent
	 * @param data    The data that is to be send
	 * @throws IOException if an I/O error occurs.
	 */
	public void send(InetAddress address, int port, byte[] data) throws IOException {
		serverSocket.send(new DatagramPacket(data, data.length, address, port));
	}

}
