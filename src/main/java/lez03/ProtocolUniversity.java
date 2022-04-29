package lez03;

import lez03.StudentProto.StudentMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;

public class ProtocolUniversity {
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
        StudentMessage studentMessage;
        try {
            studentMessage = StudentMessage.parseFrom(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<Exam> exams = new ArrayList<>();
        for (StudentMessage.Exam exam :
                studentMessage.getExamsList()) {
            StudentMessage.Exam.DateOfVerbalization dateOfVerbalization = exam.getDateOfVerbalization();
            exams.add(new Exam(exam.getExamName(), exam.getMark(), LocalDate.of(dateOfVerbalization.getYear(), dateOfVerbalization.getMonth(), dateOfVerbalization.getDay())));
        }
        String name, surname, residence;
        int yearOfBirth = studentMessage.getPersonalDetails().getYearOfBirth();
        name = studentMessage.getPersonalDetails().getName();
        surname = studentMessage.getPersonalDetails().getSurname();
        residence = studentMessage.getPersonalDetails().getResidence();
        ProtocolStudent protocolStudent = new ProtocolStudent(name, surname, residence, yearOfBirth, exams);
        System.out.println(protocolStudent);
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
