package lez02;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Address:");
        String address = userInput.readLine();
        System.out.println("Port:");
        int port;
        try {
            port = Integer.parseInt(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }
        System.out.println("Insert username: ");
        String username = userInput.readLine();

        Socket clientSocket = new Socket(address, port);
        Receiver receiver = new Receiver(clientSocket);
        receiver.start();
        Sender sender = new Sender(clientSocket, username);
        sender.start();
        try {
            sender.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupted. Shutting down...");
            clientSocket.close();
            return;
        }
        clientSocket.close();
        try {
            receiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Interrupted. Shutting down...");
        }
    }
}

class Receiver extends Thread {
    BufferedReader inFromServer;
    Socket socket;

    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
        this.inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        String message;
        while (true) {
            try {
                message = this.inFromServer.readLine();
            } catch (IOException e) {
                if (!this.socket.isClosed()) {
                    e.printStackTrace();
                    System.out.println("IO Exception. Returning...");
                    return;
                }
                System.out.println("Closing thread. Returning...");
                return;
            }
            if (message == null) {
                System.out.println("End of stream. Closing...");
                return;
            }
            System.out.println(message);
        }
    }
}

class Sender extends Thread {
    DataOutputStream outToServer;
    Socket socket;
    String username;
    BufferedReader inFromServer;

    public Sender(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outToServer = new DataOutputStream(socket.getOutputStream());
        this.username = username;
    }

    @Override
    public void run() {
        try {
            this.outToServer.writeBytes("/username::" + this.username + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception. Returning...");
            return;
        }
        String messageToSend;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                messageToSend = userInput.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("IO Exception. Returning...");
                return;
            }
            if (messageToSend.length() == 0) {
                System.out.println("Empty string received. Closing down.");
                return;
            }
            try {
                this.outToServer.writeBytes(messageToSend + "\n");
            } catch (IOException e) {
                String testRead;
                try {
                    testRead = this.inFromServer.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
                if (testRead != null) {
                    e.printStackTrace();
                    return;
                }
                System.out.println("Server is off. Returning...");
                return;
            }
        }
    }
}
