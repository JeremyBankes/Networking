import java.io.IOException;

import com.jeremy.networking.Client;
import com.jeremy.networking.Server;

public class Demo {

	public static void main(String[] args) {

		new Thread(() -> {
			try {
				Server server = new GameServer(2300);
				server.start();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			try {
				Client client = new GameClient();
				client.connect("localhost", 2300);
				client.send("Hello World\n".getBytes());
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}).start();

	}

}
