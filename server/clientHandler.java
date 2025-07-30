import java.net.*;
import java.io.*;

public class clientHandler extends Thread {
    private Socket clientSocket = null;
    
    // socket we are using is passed into thread
    public clientHandler(Socket clientSocket) {
		super("KKClientHandler");
		this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            boolean validRequest = false;
            String requestType = "Null";

            if ((inputLine = in.readLine()) != null) {
                // If we recive "list" from server we must handle list request
                if (inputLine.equals("list")) {
                    requestType = "list";
                    // List out all files in serverFiles directory
                    File directoryPath = new File("./serverFiles");
                    String contents[] = directoryPath.list();

                    // If there are any files send them to the client
                    if (contents.length > 0) {
                        out.println("Listing " + contents.length + " file(s)");
                        for (int i=0; i<contents.length; i++) {
                            out.println(contents[i]);
                        }
                    }
                    // If no files send message to client instead
                    else {
                        out.println("No files to list");
                    }
                    validRequest = true;
                    // Tell client no more data will be sent
                    out.println("__END__");
                }
                
                else if (inputLine.equals("put")) {
                    requestType = "put";
                    validRequest = true;
                    // Advance to next sent item which is name
                    String filename = in.readLine();

                    // Check if file exists in serverFiles with same name
                    if (!(new File("serverFiles/" + filename).isFile())) {
                        // Create file with correct name in serverFiles directory
                        try {
                            File newFile = new File("serverFiles/" + filename);
                            newFile.createNewFile();
                        } 
                        catch (IOException e) {
                            System.out.println("An error occurred.");
                            out.println("__I/O_EXCEPTION_ERROR__");
                        }

                        // Create writer for file
                        FileWriter writer = new FileWriter("serverFiles/" + filename);

                        // Read all lines from client and write to server file
                        while (!(inputLine = in.readLine()).equals("__END_OF_FILE__")) {
                            writer.write(inputLine + "\n");
                        }
                        // If we reach here the file was successefully copied
                        writer.close();
                        out.println("__END__");
                    }
                    else {
                        out.println("FILE EXISTS ERROR");
                    }
                }
                else {
                    // Server error if client side code is incorrect
                    out.println("Internal Error:    Incorrect client message format");
                }
            }
            // Close streams and get clients IP
            out.close();
            in.close();
            String clientIP = clientSocket.getRemoteSocketAddress().toString().replace("/","");
            clientSocket.close();

            // Update log file if request was valid
            if (validRequest) {
                try {
                    boolean first = false;
                    // Check if the log file exists and create it if it does not
                    File file = new File("log.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                        first = true;
                    }
        
                    // Open the file in append mode
                    FileWriter logFileWriter = new FileWriter(file, true);
        
                    // Calculate data for & write to log file
                    String date = java.time.LocalDate.now().toString();
                    String time = java.time.LocalTime.now().toString();

                    // Print to log (ommitting newline if is first line in file)
                    if (first) {
                        logFileWriter.write(date+'|'+time+'|'+clientIP+'|'+requestType);
                    }
                    else {
                        logFileWriter.write("\n"+date+'|'+time+'|'+clientIP+'|'+requestType);
                    }
                    logFileWriter.close();
                }
                catch (IOException e) {
                    System.err.println("Error:    Log file not updated");
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
