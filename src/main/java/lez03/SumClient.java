package lez03;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lez03.SumProtocol.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class SumClient {
    public static void main(String[] args) throws IOException {
        String address;
        int port, service;

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Insert address:");
        address = userInput.readLine();
        try {
            System.out.println("Insert port number:");
            port = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            return;
        }
        try {
            System.out.println("Insert service number (1: simpleSum 2: repeatedSum 3: streamSum):");
            service = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            return;
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build();
        switch (service) {
            case 1:
                simpleSum(userInput, channel);
                break;
            case 2:
                repeatedSum(userInput, channel);
                break;
            case 3:
                streamSum(userInput, channel);
                break;
            default:
                throw new RuntimeException("Invalid service number.");
        }
    }

    static void simpleSum(BufferedReader input, ManagedChannel channel) {
        int firstNumber, secondNumber;

        try {
            System.out.println("Insert first number:");
            firstNumber = Integer.parseInt(input.readLine());
            System.out.println("Insert second number:");
            secondNumber = Integer.parseInt(input.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO Exception happpened");
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer format.");
            return;
        }
        SumServiceGrpc.SumServiceBlockingStub stub = SumServiceGrpc.newBlockingStub(channel);
        SumProto.IntegerCouple request = SumProto.IntegerCouple.newBuilder()
                .setFistNumber(firstNumber)
                .setSecondNumber(secondNumber)
                .build();
        SumProto.IntegerSum response = stub.simpleSum(request);
        System.out.printf("%d + %d = %d\n", firstNumber, secondNumber, response.getSum());
        channel.shutdown();
    }

    static void repeatedSum(BufferedReader input,  ManagedChannel channel) {
        int firstNumber, secondNumber;

        try {
            System.out.println("Insert number to sum:");
            firstNumber = Integer.parseInt(input.readLine());
            System.out.println("Insert number of sums:");
            secondNumber = Integer.parseInt(input.readLine());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IO Exception happpened");
        } catch (NumberFormatException e) {
            System.out.println("Invalid integer format.");
            return;
        }
        SumServiceGrpc.SumServiceStub stub = SumServiceGrpc.newStub(channel);
        SumProto.IntegerCouple request = SumProto.IntegerCouple.newBuilder()
                .setFistNumber(firstNumber)
                .setSecondNumber(secondNumber)
                .build();
        stub.repeatedSum(request, new StreamObserver<SumProto.IntegerSum>() {
            @Override
            public void onNext(SumProto.IntegerSum value) {
                System.out.printf("Received value %d\n", value.getSum());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server stream ended. Quitting client...");
                channel.shutdownNow();
            }
        });
        try {
            channel.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void streamSum(BufferedReader input, ManagedChannel channel) {
        SumServiceGrpc.SumServiceStub stub = SumServiceGrpc.newStub(channel);
        StreamObserver<SumProto.IntegerCouple> serverStream = stub.streamSum(new StreamObserver<SumProto.IntegerSum>() {
            @Override
            public void onNext(SumProto.IntegerSum value) {
                System.out.printf("Received value %d\n", value.getSum());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server stream ended. Quitting client...");
                channel.shutdownNow();
            }
        });
        int firstNumber, secondNumber;
        while (true) {
            try {
                System.out.println("Insert first number:");
                firstNumber = Integer.parseInt(input.readLine());
                System.out.println("Insert second number:");
                secondNumber = Integer.parseInt(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                channel.shutdown();
                return;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number inserted. Quitting...");
                serverStream.onCompleted();
                break;
            }
            SumProto.IntegerCouple request = SumProto.IntegerCouple.newBuilder()
                    .setFistNumber(firstNumber)
                    .setSecondNumber(secondNumber)
                    .build();
            serverStream.onNext(request);
        }
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
