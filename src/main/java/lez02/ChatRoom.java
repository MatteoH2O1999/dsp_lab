package lez02;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    public static void main(String[] args) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        int port;
        try {
            System.out.println("Insert port number:");
            port = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Invalid port. Must be an integer.");
            return;
        }

        System.out.printf("Starting server socket on port %d\n", port);
        ServerSocket socket = new ServerSocket(port);

        AcceptConnections connectionHandler = new AcceptConnections(socket);

        connectionHandler.start();
        userInput.readLine();
        connectionHandler.stopAcceptingConnections();
        try {
            connectionHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Message {
    String message, username;

    public Message(String username, String message) {
        this.message = message;
        this.username = username;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.username, this.message);
    }
}

class AcceptConnections extends Thread {
    final ServerSocket socket;
    final ArrayList<Initializer> startedThreads = new ArrayList<>();
    final Broadcaster broadcaster = new Broadcaster();

    public AcceptConnections(ServerSocket socket) {
        this.socket = socket;
        this.broadcaster.start();
    }

    @Override
    public void run() {
        while (!this.socket.isClosed()) {
            try {
                Initializer initializer = new Initializer(this.socket.accept(), this.broadcaster);
                initializer.start();
                this.startedThreads.add(initializer);
            } catch (IOException e) {
                if (!this.socket.isClosed()) {
                    e.printStackTrace();
                    System.out.println("IO Exception. Returning...");
                    return;
                }
                System.out.println("Shutting down server...");
            }
        }
        this.broadcaster.stopBroadcasting();
        for (Initializer i :
                startedThreads) {
            try {
                i.socket.close();
                i.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        try {
            this.broadcaster.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopAcceptingConnections() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Initializer extends Thread {
    Socket socket;
    Broadcaster broadcaster;

    public Initializer(Socket uninitializedSocket, Broadcaster broadcaster) {
        this.socket = uninitializedSocket;
        this.broadcaster = broadcaster;
    }

    @Override
    public void run() {
        String message;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            message = reader.readLine();
        } catch (IOException e) {
            if (this.socket.isInputShutdown()) {
                e.printStackTrace();
            }
            return;
        }
        if (!message.split("::")[0].equals("/username")) {
            try {
                DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
                out.writeBytes("Expected '/username USERNAME' message in order to initialize connection.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String username = message.split("::")[1];
        Handler handler = new Handler(this.socket, username, this.broadcaster.getIncomingMessages());
        this.broadcaster.addHandler(handler);
        System.out.printf("Login completed as %s\n", username);
        handler.start();
        try {
            handler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Handler extends Thread {
    Socket socket;
    String username;
    Queue<Message> incomingMessages;

    public Handler(Socket socket, String username, Queue<Message> incomingMessages) {
        this.socket = socket;
        this.username = username;
        this.incomingMessages = incomingMessages;
    }

    @Override
    public void run() {
        BufferedReader inFromClient;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String message;
        while (!this.socket.isClosed()) {
            try {
                message = inFromClient.readLine();
            } catch (IOException e) {
                if (!this.socket.isClosed()){
                    e.printStackTrace();
                    return;
                }
                continue;
            }
            if (message == null) {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.incomingMessages.put(new Message(this.username, message));
            }
        }
    }
}

class Broadcaster extends Thread {
    final HashMap<String, HandlerData> outStreams = new HashMap<>();
    final Queue<Message> incomingMessages = new Queue<>();
    boolean broadcast = true;

    @Override
    public void run() {
        Message toSend;
        while (this.isBroadcast()) {
            toSend = this.incomingMessages.get();
            if (toSend == null) {
                break;
            }
            sendToAllButUsername(toSend.username, toSend.toString());
        }
    }

    public synchronized void stopBroadcasting() {
        this.broadcast = false;
        this.incomingMessages.close();
    }

    public synchronized boolean isBroadcast() {
        return broadcast;
    }

    public Queue<Message> getIncomingMessages() {
        return incomingMessages;
    }

    public synchronized void sendToAllButUsername(String username, String message) {
        for (Map.Entry<String, HandlerData> entry :
                this.outStreams.entrySet()) {
            if ((!username.equals(entry.getKey())) && (!entry.getValue().socket.isClosed())) {
                try {
                    entry.getValue().out.writeBytes(message + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public synchronized void addHandler(Handler h) {
        this.outStreams.put(h.username, new HandlerData(h));
    }

    private static class HandlerData {
        Socket socket;
        DataOutputStream out;

        public HandlerData(Handler h) {
            DataOutputStream out;
            try {
                out = new DataOutputStream(h.socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            this.socket = h.socket;
            this.out = out;
        }
    }
}
