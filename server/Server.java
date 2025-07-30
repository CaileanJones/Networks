import java.net.*;
import java.util.concurrent.*;
import java.io.*;

public class Server {
    private ServerSocket serverSocket = null;

    public Server() {
        try {
            // Listen on server socket 9912
            serverSocket = new ServerSocket(9912);
        }
        catch (IOException e) {
            System.err.println("Could not listen on port: 9912.");
            System.exit(1);
        }
    }
    ExecutorService executorPool = Executors.newFixedThreadPool(20);

    public void runServer() {
        Socket clientSocket = null;
        while(true) {
            // Search socket and give to new clientHandler thread when new client detected
            try {
                clientSocket = serverSocket.accept();
                executorPool.submit(new clientHandler(clientSocket));
            }
            catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }            
        }
    }

    public static void main(String[] args) {
      Server fileServer = new Server();
      fileServer.runServer();
    }
}
