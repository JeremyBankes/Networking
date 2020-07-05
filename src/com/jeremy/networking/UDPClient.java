package com.jeremy.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPClient {

	private byte[] buffer;
	private DatagramSocket socket;
	private DatagramPacket incomingPacket;
	private int receivePacketSize;

	private final Endpoint server;

	private boolean listening;
	private Thread listenThread;

	/**
	 * Crates a UDPClient and binds it to any available port on the local machine.
	 * 
	 * @param server            The end point (address/port pair) of the server
	 * @param receivePacketSize The buffer size for receiving server packets.
	 * @throws SocketException If the socket could not be opened.
	 */
	public UDPClient(Endpoint server, int receivePacketSize) throws SocketException {
		this.server = server;
		this.receivePacketSize = receivePacketSize;
		buffer = new byte[receivePacketSize];
		socket = new DatagramSocket();
	}

	/**
	 * Starts the client's listening for packets from the server
	 */
	public void startListening() {
		if (!listening) {
			listenThread = new Thread(this::listen, "upd-listen-thread");
			listening = true;
			listenThread.start();
		}
	}

	/**
	 * Stops the client's listening for packets from the server
	 */
	public void stopListening() {
		if (listening) {
			listening = false;
			try {
				listenThread.join();
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Called internally in com.jeremy.networking.UDPClient on a separate thread
	 */
	private void listen() {
		try {
			incomingPacket = new DatagramPacket(buffer, receivePacketSize);
			socket.receive(incomingPacket);
			onReceive(incomingPacket.getData());
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Called internally by com.jeremy.networking.UDPClient when a packet is
	 * received from the server. This method is meant to be overridden.
	 * 
	 * @param data The data in the received packet
	 */
	protected void onReceive(byte[] data) {}

	/**
	 * Sends a packet to the server defined by the serverAddress and serverPort
	 * given on instantiation
	 * 
	 * @param data The data to send to the server
	 * @throws IOException If an I/O error occurs.
	 */
	public void send(byte[] data) throws IOException {
		socket.send(new DatagramPacket(data, data.length, server.address, server.port));
	}

}
