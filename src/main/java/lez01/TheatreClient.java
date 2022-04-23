package lez01;

import java.io.*;
import java.net.*;

public class TheatreClient {
    public static void main(String[] args) throws IOException{
        int port;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Address:");
        String address = userInput.readLine();
        System.out.println("Port:");
        try {
            port = Integer.parseInt(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }

        Socket clientSocket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println("Action (0: check, 1: create reservation): ");
        int action;
        try {
            action = Integer.parseInt(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }

        outToServer.writeBytes(action + "\n");

        if (action == 0) {
            int booked = Integer.parseInt(inFromServer.readLine());
            if (booked == 0){
                System.out.println("There are no more available seats.");
            } else {
                if (booked == -1) {
                    booked++;
                }
                System.out.printf("There are %d seats already reserved.", booked);
            }
        } else {
            boolean success = Boolean.parseBoolean(inFromServer.readLine());
            if (success) {
                System.out.println("Reservation successful.");
            } else {
                System.out.println("Reservation failed.");
            }
        }

        clientSocket.close();
    }
}
