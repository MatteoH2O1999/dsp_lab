package lez01;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException{
        int port;
        float firstNumber, secondNumber;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Address:");
        String address = userInput.readLine();
        System.out.println("Port:");
        try {
            port = Integer.parseInt(userInput.readLine());
            System.out.println("First number:");
            firstNumber = Float.parseFloat(userInput.readLine());
            System.out.println("Second number:");
            secondNumber = Float.parseFloat(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }

        Socket clientSocket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String toSend = firstNumber + ";" + secondNumber + "\n";
        System.out.println("Sending string: " + toSend);
        outToServer.writeBytes(toSend);
        String result = inFromServer.readLine();
        float floatResult;
        try {
            floatResult = Float.parseFloat(result);
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid response from server. Expected a float, got " + result);
            throw e;
        }
        System.out.printf("%f + %f = %f%n", firstNumber, secondNumber, floatResult);

        clientSocket.close();
    }
}
