import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket fileServerSocket = null;
    private PrintWriter socketOutput = null;
    private BufferedReader socketInput = null;

    public void connectToFileServer(String[] args) {
        // Check we have required valid args
        if (args.length == 0) {
            System.err.println("Error:    Argument required. Must specify list or put.\n");
            System.exit(1);
        }
        else {
            try {
                // Try and create the socket. The server is assumed to be running on the same host ('localhost')
                fileServerSocket = new Socket("localhost", 9912);
                // Chain a writing stream
                socketOutput = new PrintWriter(fileServerSocket.getOutputStream(), true);
                // Chain a reading stream
                socketInput = new BufferedReader(new InputStreamReader(fileServerSocket.getInputStream()));
            }
            catch (UnknownHostException e) {
                System.err.println("Don't know about host.\n");
                System.exit(1);
            }
            catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to host.\n");
                System.exit(1);
            }

            // Check if we are perfoming a list or put operation
            String fromServer;
            try {
                if (args[0].equals("list")) {
                    // Send list to server to tell them we are doing list
                    socketOutput.println(args[0]);
    
                    // Print server responce in client window, then exit
                    while((fromServer = socketInput.readLine())!=null && !fromServer.equals("__END__")) {
                        System.out.println(fromServer);
                    }
                    fileServerSocket.close();
                    System.exit(0);
                }
                else if (args[0].equals("put")) {
                    // Exist if dont have a second arg for put command
                    if (args.length < 2) {
                        System.err.println("Error:    Invalid arguments must specify file to put.\n");
                        fileServerSocket.close();
                        System.exit(1);
                    }
                    else {
                        if (new File(args[1]).isFile()) {
                            File uploadFile = new File(args[1]);
                            // Tell server we want to "put"
                            socketOutput.println("put");
                            // Then send name of file to create
                            socketOutput.println(uploadFile.getName());

                            // Send file across line by line to server
                            Scanner myReader = new Scanner(uploadFile);
                            while (myReader.hasNextLine()) {
                                String thisLine = myReader.nextLine();
                                socketOutput.println(thisLine);
                            }
                            // Let the server know this is the end of the
                            socketOutput.println("__END_OF_FILE__");
                            myReader.close();

                            String closingMessage;
                            // Check if we have server side error, where file already existed printing message if we do
                            if ((closingMessage = socketInput.readLine()).equals("FILE EXISTS ERROR")) {  
                                System.err.println("Error:    Cannot upload file '" + uploadFile.getName() + "'; already exists on server");
                                fileServerSocket.close();
                                System.exit(1);
                            }
                            // Check if server encounted an I/O exception
                            if (closingMessage.equals("__I/O_EXCEPTION_ERROR__")) {  
                                System.err.println("Error:    I/O Exception occured on server side, your file was not uploaded");
                                fileServerSocket.close();
                                System.exit(1);
                            }
                            // If server was successful it sends __END__, however we do not print out for this   
                        }
                        else {
                            System.out.println("Error:    Cannot open local file '" + args[1] + "' for reading.");
                            fileServerSocket.close();
                            System.exit(1);
                        }  
                    }
                    fileServerSocket.close();
                    System.exit(0);
                }
                else {
                    System.err.println("Error:    Invalid arguments must specify list or put.\n");
                    fileServerSocket.close();
                    System.exit(1);
                }
            }
            catch (IOException e) {
                System.err.println("I/O exception during execution\n");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        Client fileServerClient = new Client();
        fileServerClient.connectToFileServer(args);
    }
}
    