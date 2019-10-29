import java.io.*;
import java.util.*;
import java.net.*;

public class client {

    static int findNextSpace(int currChar, String str) {
        for (int i = currChar; i < str.length(); i ++) {
            if (str.charAt(i) == ' ') {
                return(i);
            }
        }
        return(-1);
    }

    public static void main(String[] args) throws IOException {

        final String username = "username";
        final String password = "password";
        String usernameInput, passwordInput;
        String commandInput;
        int flag = 0;

        try {
            Scanner sc = new Scanner(System.in);
            Console console = System.console();
            do {
                System.out.println("\nEnter the command you want to run.\n1. ftpclient <ip> <port_number>");
                commandInput = sc.nextLine();
                commandInput += "  ";
                if (commandInput.substring(0, Math.min(10, commandInput.length() - 1)).equals("ftpclient ")) {
                    if ((commandInput.substring(10, findNextSpace(10, commandInput)).equals("localhost")) && (commandInput.substring(findNextSpace(10, commandInput) + 1).equals(String.valueOf("5056  ")))) {
                        flag = 1;
                    }
                    else {
                        System.out.println("Incorrect ip or port_number.");
                    }
                }
                else {
                    System.out.println("Inavlid command.");
                }
            } while (flag == 0);
            do {
                System.out.print("\nEnter username : ");
                usernameInput = sc.nextLine();
                passwordInput = new String(console.readPassword("Enter password : "));
                if (!usernameInput.equals(username) || !passwordInput.equals(password)) {
                    System.out.println("Incorrect username or password.");
                }
            } while(!usernameInput.equals(username) || !passwordInput.equals(password));
            if (usernameInput.equals(username) && passwordInput.equals(password)) {
                InetAddress inetIp = InetAddress.getByName("localhost");
                Socket socket = new Socket(inetIp, 5056);
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                while(true) {
                    System.out.println(dataInputStream.readUTF());
                    String toSend = sc.nextLine();
                    dataOutputStream.writeUTF(toSend);
                    if (toSend.equals("Exit")) {
                        System.out.println("Closing this connection.\nName: " + socket);
                        socket.close();
                        System.out.println("Connection Closed.");
                        break;
                    }
                    String recieved = dataInputStream.readUTF();
                    System.out.println(recieved);
                }
                sc.close();
                dataInputStream.close();
                dataOutputStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
