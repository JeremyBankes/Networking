package demo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jeremy.networking.Client;

public class DemoClient extends Client {

	@Override
	protected void onConnect() {
		System.out.println("[CLIENT] Connected to Server!");
	}

	@Override
	protected void onServerContact(InputStream inputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			while (isConnected()) {
				// Read response from server
				String serverResponse = reader.readLine();
				System.out.printf("[CLIENT] The server responded '%s'.%n", serverResponse);
			}
			reader.close();
		} catch (IOException exception) {
			// Handle exception
		}
	}

	@Override
	protected void onDisconnect() {
		System.out.println("[CLIENT] Disconnected from server.");
	}

	@Override
	protected void onException(Exception exception) {
		exception.printStackTrace();
	}

}
