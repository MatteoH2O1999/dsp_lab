package lez01;

import java.io.*;
import java.net.*;

public class IterativeServer {
    public static void main(String[] args) throws IOException{
        float firstNumber, secondNumber, sum;
        String clientData;
        int portNumber;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Port number:");
            portNumber = Integer.parseInt(userInput.readLine());
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            throw e;
        }

        ServerSocket acceptSocket = new ServerSocket(portNumber);

        while(true) {
            System.out.println("Waiting for connections...");
            Socket connectionSocket = acceptSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            System.out.println("Connection established...");
            clientData = inFromClient.readLine();
            System.out.println("Received data: " + clientData);
            String[] splitData = clientData.split(";");
            try {
                firstNumber = Float.parseFloat(splitData[0]);
                secondNumber = Float.parseFloat(splitData[1]);
            }
            catch (NumberFormatException e) {
                outToClient.writeBytes("Invalid float numbers in input");
                throw e;
            }
            sum = firstNumber + secondNumber;
            outToClient.writeBytes(Float.toString(sum) + '\n');
            connectionSocket.close();
        }
    }
}
