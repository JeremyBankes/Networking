package com.sineshore.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public final static int PACKET_SIZE = 1024;

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public final int port;

    private Thread listenUDP;
    private boolean runningUDP;

    private Thread listenTCP;
    private boolean runningTCP;

    private DatagramSocket socketUDP;
    private ServerSocket socketTCP;

    private TCPRecieveCallback tcpRecieveCallback;
    private UDPRecieveCallback udpRecieveCallback;

    private HashMap<String, Socket> connections;

    public Server(int port) throws IOException {
	this.port = port;
	listenUDP = new Thread(this::listenUDP, "udp-listener");
	listenTCP = new Thread(this::listenTCP, "tcp-listener");

	socketUDP = new DatagramSocket(port);
	socketTCP = new ServerSocket(port);

	connections = new HashMap<>();
    }

    public void start(boolean udp, boolean tcp) {
	if (udp) {
	    if (!runningUDP) {
		runningUDP = true;
		listenUDP.start();
	    }
	}
	if (tcp) {
	    if (!runningTCP) {
		runningTCP = true;
		listenTCP.start();
	    }
	}
    }

    public void stop() throws IOException {
	stopUDP();
	stopTCP();
    }

    public void stopUDP() {
	if (runningUDP) {
	    runningUDP = false;
	    socketUDP.close();
	}
    }

    public void stopTCP() throws IOException {
	if (runningTCP) {
	    runningTCP = false;
	    socketTCP.close();
	}
    }

    private void listenUDP() {
	try {
	    while (runningUDP) {
		byte[] buffer = new byte[PACKET_SIZE];
		DatagramPacket recieved = new DatagramPacket(buffer, PACKET_SIZE);
		socketUDP.receive(recieved);
		byte[] data = recieved.getData();
		if (udpRecieveCallback != null)
		    udpRecieveCallback.recieve(recieved.getAddress().getHostAddress(), recieved.getPort(), data);
	    }
	} catch (IOException e) {
	    if (runningUDP) {
		e.printStackTrace();
		stopUDP();
	    }
	}
    }

    private void listenTCP() {
	try {
	    while (runningTCP) {
		Socket socket = socketTCP.accept();
		EXECUTOR.execute(() -> {
		    String address = socket.getInetAddress().getHostAddress();
		    int port = socket.getPort();
		    connections.put(address + ":" + port, socket);
		    while (socket.isConnected() && !socket.isClosed()) {
			if (tcpRecieveCallback != null) {
			    try {
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				tcpRecieveCallback.recieve(address, socket.getPort(), inputStream, outputStream);
			    } catch (IOException e) {
				e.printStackTrace();
			    }
			}
		    }
		    connections.remove(address + ":" + port);
		});
	    }
	} catch (IOException e) {
	    if (runningTCP) {
		e.printStackTrace();
		try {
		    stopTCP();
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    public void sendUDP(byte[] data, String address, int port) throws UnknownHostException, IOException {
	socketUDP.send(new DatagramPacket(data, 0, Math.min(data.length, Server.PACKET_SIZE), InetAddress.getByName(address), port));
    }

    public TCPRecieveCallback getTCPRecieveCallback() {
	return tcpRecieveCallback;
    }

    public void setTCPRecieveCallback(TCPRecieveCallback tcpRecieveCallback) {
	this.tcpRecieveCallback = tcpRecieveCallback;
    }

    public UDPRecieveCallback getUDPRecieveCallback() {
	return udpRecieveCallback;
    }

    public void setUDPRecieveCallback(UDPRecieveCallback udpRecieveCallback) {
	this.udpRecieveCallback = udpRecieveCallback;
    }

    public void disconnect(String address, int port) {
	if (!connections.containsKey(address + ":" + port)) {
	    throw new IllegalArgumentException("No clients connected with address '" + address + ":" + port + "'.");
	}
	try {
	    connections.get(address + ":" + port).close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public String getAddress() {
	return socketTCP.getInetAddress().getHostAddress();
    }

}