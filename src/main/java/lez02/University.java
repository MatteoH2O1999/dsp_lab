package lez02;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class University {
    public static void main(String[] args) throws IOException {
        int port;

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Insert port number:");
        try {
            port = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            return;
        }

        ServerSocket socket = new ServerSocket(port);
        ServerThread serverThread = new ServerThread(socket);
        serverThread.start();

        userInput.readLine();

        socket.close();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    final ServerSocket socket;
    final ArrayList<StudentThread> threads = new ArrayList<>();

    public ServerThread(ServerSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Socket clientSocket;
        while (true) {
            try {
                clientSocket = this.socket.accept();
            } catch (IOException e) {
                if (!this.socket.isClosed()) {
                    e.printStackTrace();
                }
                break;
            }
            StudentThread thread = new StudentThread(clientSocket);
            thread.start();
            this.threads.add(thread);
        }
        for (StudentThread t :
                this.threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Something happened while waiting for all threads to finish");
            }
        }
    }
}

class StudentThread extends Thread {
    Socket socket;

    public StudentThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader inFromClient;
        String jsonStudent;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            jsonStudent = inFromClient.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Gson gson = new Gson();
        Student student = gson.fromJson(jsonStudent, Student.class);
        System.out.println(student);
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
