# Networking API
A TCP, client-server, Java networking API.

## Overview
This API is a lightweight TCP/UDP networking API ideal for simple Client/Server applications.

## Server Application Demo
```java
public class TCPDemoServer extends TCPServer {

  public TCPDemoServer(int port) throws IOException {
    super(port);
  }

  @Override
  public void onConnect(InetAddress address, int port) {
    System.out.printf("[SERVER] A user has connected. (%s:%d)%n", address, port);
  }

  @Override
  protected void onReceiveClient(InetAddress address, int port, InputStream inputStream, OutputStream outputStream) {
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
  public void onDisconnect(InetAddress address, int port) {
    System.out.printf("[SERVER] A user has disconnected. (%s:%d)%n", address, port);
  }

  @Override
  protected void onException(Exception exception) {
    // Handle exception
  }

}
```

## Client Application Demo
```java
public class TCPDemoClient extends TCPClient {

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
    // Handle exception
  }

}
```
