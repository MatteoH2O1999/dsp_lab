package lez01;

import java.security.InvalidParameterException;
import java.io.*;
import java.net.*;

public class TheatreServer {
    public static void main(String[] args) throws IOException{
        int portNumber, numberOfTickets;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Port number:");
            portNumber = Integer.parseInt(userInput.readLine());
            System.out.println("Number of tickets");
            numberOfTickets = Integer.parseInt(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }

        ServerSocket acceptSocket = new ServerSocket(portNumber);

        Reservations reservations = new Reservations(numberOfTickets);

        while (true) {
            Thread newThread = new ServiceTheatreThread(reservations, acceptSocket.accept());
            newThread.start();
        }
    }
}

class ServiceTheatreThread extends Thread {
    Reservations reservations;
    Socket connectionSocket;

    public ServiceTheatreThread(Reservations res, Socket socket) {
        super();
        this.reservations = res;
        this.connectionSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
            System.out.println("Connection established...");

            String received = inFromClient.readLine();
            System.out.println("Received string from client: " + received);
            int action = Integer.parseInt(received);

            if (action == 0) {
                int booked = this.reservations.getNumberOfTickets();
                outToClient.writeBytes(booked + "\n");
            } else {
                boolean success = this.reservations.reserveTicket();
                outToClient.writeBytes(success + "\n");
            }

            this.connectionSocket.close();
        }
        catch (IOException e) {
            System.out.println("Unexpected error happened. Sorry.");
        }
    }
}

class Reservations {
    int numberOfTickets;
    int totalTickets;

    public Reservations(int numberOfTickets) {
        if (numberOfTickets < 0) {
            throw new InvalidParameterException("Number of tickets must be > 0");
        }
        this.numberOfTickets = numberOfTickets;
        this.totalTickets = numberOfTickets;
    }

    synchronized public int getNumberOfTickets() {
        if (this.numberOfTickets == 0) {
            return 0;
        }
        int booked =  this.totalTickets - this.numberOfTickets;
        if (booked == 0) {
            booked--;
        }
        return booked;
    }

    synchronized public boolean reserveTicket() {
        if (numberOfTickets == 0) {
            return false;
        }
        numberOfTickets--;
        return true;
    }
}
