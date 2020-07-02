package demo;

import java.io.IOException;

import com.jeremy.networking.TCPClient;
import com.jeremy.networking.TCPServer;

public class TCPDemo {

	public static void main(String[] args) {
		// Create a server & client on their own threads to simulate running on separate machines.

		new Thread(() -> {
			try {
				// Create & start a new TCPServer instance listening on port 2300.
				TCPServer server = new TCPDemoServer(2300);
				server.start();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			try {
				String serverAddress = "127.0.0.1";
				int serverPort = 2300;

				// Create a TCPClient instance, connect it to [serverAddress:serverPort] and send a pay load
				TCPClient client = new TCPDemoClient();
				client.connect(serverAddress, serverPort);
				client.send("Hello World\n".getBytes());
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}).start();

	}

}
