package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import com.jeremy.networking.Endpoint;
import com.jeremy.networking.TCPServer;

public class TCPDemoServer extends TCPServer {

	public TCPDemoServer(int port) throws IOException {
		super(port);
	}

	@Override
	public void onConnect(Endpoint client) {
		System.out.printf("[SERVER] A user has connected. (%s:%d)%n", client.address.getHostAddress(), client.port);
	}

	@Override
	protected void onReceiveClient(Endpoint client, InputStream inputStream, OutputStream outputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			PrintStream writer = new PrintStream(outputStream);
			while (isConnected(client)) {
				// Read client command
				String clientCommand = reader.readLine();

				System.out.println("[SERVER] Read from client: " + clientCommand);

				// Handle client command
				if (clientCommand.equalsIgnoreCase("stop")) {
					disconnect(client);
				}

				// Respond to client
				writer.printf("You have sent the server the '%s' command.%n", clientCommand);
			}
			writer.close();
			reader.close();
		} catch (IOException exception) {
			// Handle exception
		}
	}

	@Override
	public void onDisconnect(Endpoint client) {
		System.out.printf("[SERVER] A user has disconnected. (%s:%d)%n", client.address.getHostAddress(), client.port);
	}

	@Override
	protected void onException(Exception exception) {
		// Handle exception
	}

}
