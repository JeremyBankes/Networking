package com.jeremy.networking.v1_0;

import java.io.IOException;

import com.jeremy.serialization.v3_0.Bundle;

public class NetworkingSandbox {

	public static void main(String[] args) throws IOException {
		UdpServer server = new UdpServer();

		server.setReceiveCallback((address, bundle) -> {
			System.out.println("Got bundle from client: " + bundle);

			try {
				server.send(address, bundle);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		});

		server.start(2345);

		new Thread(() -> {

			try {

				UdpClient client = new UdpClient();

				client.setReceiveCallback(bundle -> {
					System.out.println("Got bundle from server: " + bundle);
				});

				client.connect("localhost", 2345);
				client.listen();

				Bundle bundle = new Bundle("Person");
				bundle.put("Name", "Jeremy");
				bundle.put("age", 18);

				client.send(bundle);

			} catch (IOException ioException) {
				ioException.printStackTrace();
			}

		}).start();
	}

}
