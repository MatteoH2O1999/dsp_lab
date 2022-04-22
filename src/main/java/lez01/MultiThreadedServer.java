package lez01;

import java.io.*;
import java.net.*;

public class MultiThreadedServer {
    public static void main(String[] args) throws IOException{
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

        while(true){
            Thread newThread = new ServiceThread(acceptSocket.accept());
            newThread.start();
        }
    }
}

class ServiceThread extends Thread {
    Socket connectionSocket;

    public ServiceThread(Socket connectionSocket) {
        super();
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        float firstNumber, secondNumber, sum;
        String clientData;
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
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
            this.connectionSocket.close();
        }
        catch (IOException e) {
            System.out.println("Unexpected error happened. Sorry.");
        }
    }
}
