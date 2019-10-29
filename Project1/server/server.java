import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class server {

    public static Dictionary clients = new Hashtable();
    public static int clientNumber = 1;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5056);

        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("\nA new client connection was established.\nName: " + socket);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("A new thread was allocated for this client.");
            clients.put(socket.getPort(), "client" + clientNumber);
            clientNumber++;
            Thread thread = new ClientHandler(socket, dataInputStream, dataOutputStream);
            thread.start();
        }

    }

}
class ClientHandler extends Thread {

    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;
    Scanner sc = new Scanner(System.in);

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {

        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;

    }

    static String getAllFiles(String path) {

        File directory = new File(path);
        File[] filesList = directory.listFiles();
        String directoryString = "";
        for(File f: filesList) {
            directoryString += f.getName() + "\n";
        }
        return(directoryString);

    }

    static int getFilenameEnd(String value) {

        int i = 0;
        for(i = 7; i < value.length(); i ++) {
            if (value.charAt(i) == ' ') {
                break;
            }
        }
        return i;

    }

    public void run() {

        String recieved, toReturn = "BOGUS", fileContent = "", filename;
        while(true) {
            try {
                dataOutputStream.writeUTF("\nType the command that you want to run.\n1. dir\n2. get <filename>\n3. upload <filename>\n");
                recieved = dataInputStream.readUTF();
                if (recieved.equals("Exit")) {
                    System.out.println((String)server.clients.get(socket.getPort()) + " is exiting.");
                    this.socket.close();
                    System.out.println("Connection Closed.");
                    break;
                }
                if (recieved.length() < 3) {
                    toReturn = "Invalid Command.";
                    dataOutputStream.writeUTF(toReturn);
                }
                else if (recieved.substring(0, 3).equals("dir")) {
                    System.out.println("Running the command : " + recieved);
                    toReturn = getAllFiles(".");
                    dataOutputStream.writeUTF("\n" + toReturn);
                }
                else if (recieved.substring(0, 3).equals("get")) {
                    filename = recieved.substring(4);
                    String allFilesServer = getAllFiles(".");
                    String clientNameInGet = (String)server.clients.get(socket.getPort());
                    String allFilesClient = getAllFiles("../" + clientNameInGet + "/");
                    if (!allFilesServer.contains(filename + "\n")) {
                        dataOutputStream.writeUTF("No file named " + filename + " exists on the server.");
                    }
                    else if (allFilesClient.contains(filename + "\n")) {
                        dataOutputStream.writeUTF("File named " + filename + " already exists on the client " + clientNameInGet + ".");
                    }
                    else {
                        System.out.println("Running the command : " + recieved);
                        fileContent = Files.readString(Paths.get(filename), StandardCharsets.US_ASCII);
                        String clientName = (String)server.clients.get(socket.getPort());
                        FileOutputStream fileOutputStream = new FileOutputStream("../" + clientName + "/" + filename);
                        byte[] stringToBytes = fileContent.getBytes();
                        fileOutputStream.write(stringToBytes);
                        fileOutputStream.close();
                        dataOutputStream.writeUTF("The file has been retrieved.\n\nThe contents are :\n" + fileContent);
                    }
                }
                else if (recieved.substring(0, 6).equals("upload")) {
                    int filenameEnd = getFilenameEnd(recieved);
                    filename = recieved.substring(7, filenameEnd);
                    String allFilesServer = getAllFiles(".");
                    String clientNameInUpload = (String)server.clients.get(socket.getPort());
                    String allFilesClient = getAllFiles("../" + clientNameInUpload + "/");
                    if (allFilesServer.contains(filename + "\n")) {
                        dataOutputStream.writeUTF("File named " + filename + " already exists on the server.");
                    }
                    else if (!allFilesClient.contains(filename + "\n")) {
                        dataOutputStream.writeUTF("No file named " + filename + " exists on the client " + clientNameInUpload + ".");
                    }
                    else {
                        System.out.println("Running the command : " + recieved);
                        String clientName = (String)server.clients.get(socket.getPort());
                        fileContent = Files.readString(Paths.get("../" + clientName + "/" + filename), StandardCharsets.US_ASCII);
                        FileOutputStream fileOutputStream = new FileOutputStream(filename);
                        byte[] stringToBytes = fileContent.getBytes();
                        fileOutputStream.write(stringToBytes);
                        fileOutputStream.close();
                        toReturn = "The file has been uploaded to the server directory.";
                        dataOutputStream.writeUTF("\n" + toReturn);
                    }
                }
                else {
                    toReturn = "Invalid Command.";
                    dataOutputStream.writeUTF(toReturn);
                }
                toReturn = "BOGUS";
                fileContent = "";
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        try{
            this.dataInputStream.close();
            this.dataOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
