package demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import com.jeremy.networking.Server;

public class DemoServer extends Server {

	public DemoServer(int port) throws IOException {
		super(port);
	}

	@Override
	public void onConnect(String address, int port) {
		System.out.printf("[SERVER] A user has connected. (%s:%d)%n", address, port);
	}

	@Override
	protected void onReceiveClient(String address, int port, InputStream inputStream, OutputStream outputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			PrintStream writer = new PrintStream(outputStream);
			while (isConnected(address, port)) {
				// Read client command
				String clientCommand = reader.readLine();

				System.out.println("[SERVER] Read from client: " + clientCommand);

				// Handle client command
				if (clientCommand.equalsIgnoreCase("stop")) {
					disconnect(address, port);
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
	public void onDisconnect(String address, int port) {
		System.out.printf("[SERVER] A user has disconnected. (%s:%d)%n", address, port);
	}

}
