package lez03;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lez03.SumProtocol.*;

import java.io.*;

public class SumServer {
    public static void main(String[] args) throws IOException {
        int port;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Insert port number:");
        try {
            port = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Server server = ServerBuilder.forPort(port).addService(new SumService()).build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.printf("Server started on port %d.\n", port);
        System.out.println("Press any key to stop.");
        userInput.readLine();
        System.out.println("Stopping...");
        server.shutdown();
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class SumService extends SumServiceGrpc.SumServiceImplBase {

    @Override
    public void simpleSum(SumProto.IntegerCouple request, StreamObserver<SumProto.IntegerSum> responseObserver) {
        int sum = request.getFistNumber() + request.getSecondNumber();
        SumProto.IntegerSum integerSum = SumProto.IntegerSum.newBuilder().setSum(sum).build();
        responseObserver.onNext(integerSum);
        responseObserver.onCompleted();
    }

    @Override
    public void repeatedSum(SumProto.IntegerCouple request, StreamObserver<SumProto.IntegerSum> responseObserver) {
        int n = request.getFistNumber();
        int t = request.getSecondNumber();
        int sum = 0;
        for (int i = 1; i <= t; i++) {
            sum += n;
            SumProto.IntegerSum integerSum = SumProto.IntegerSum.newBuilder().setSum(sum).build();
            responseObserver.onNext(integerSum);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<SumProto.IntegerCouple> streamSum(StreamObserver<SumProto.IntegerSum> responseObserver) {
        return new StreamObserver<SumProto.IntegerCouple>() {
            @Override
            public void onNext(SumProto.IntegerCouple value) {
                int sum = value.getFistNumber() + value.getSecondNumber();
                SumProto.IntegerSum integerSum = SumProto.IntegerSum.newBuilder().setSum(sum).build();
                responseObserver.onNext(integerSum);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}