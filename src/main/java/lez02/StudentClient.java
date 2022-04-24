package lez02;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;

public class StudentClient {
    public static void main(String[] args) throws IOException {
        String name, surname, residence;
        int yearOfBirth;
        ArrayList<Exam> exams = new ArrayList<>();

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Insert name of student:");
        name = userInput.readLine();
        System.out.println("Insert surname of student:");
        surname = userInput.readLine();
        System.out.println("Insert residence of student:");
        residence = userInput.readLine();
        System.out.println("Insert year of birth of student:");
        try {
            yearOfBirth = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid year of birth. Must be an integer.");
            return;
        }

        while (true) {
            String examName;
            int mark;
            LocalDate verbalization;

            System.out.println("Insert exam name:");
            examName = userInput.readLine();
            if (examName.length() == 0) {
                break;
            }
            System.out.println("Insert exam mark:");
            try {
                mark = Integer.parseInt(userInput.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid mark. Must be an integer.");
                return;
            }
            System.out.println("Insert date of verbalization:");
            try {
                verbalization = LocalDate.parse(userInput.readLine());
            } catch (Exception e) {
                System.out.println("Invalid date format. Expected YYYY-MM-DD");
                return;
            }
            exams.add(new Exam(name, mark, verbalization));
        }

        Student student = new Student(name, surname, residence, yearOfBirth, exams);

        String address;
        int port;

        System.out.println("Insert address:");
        address = userInput.readLine();
        System.out.println("Insert port:");
        try {
            port = Integer.parseInt(userInput.readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Must be an integer.");
            return;
        }

        Socket socket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());

        Gson gson = new Gson();
        String jsonStudent = gson.toJson(student);

        outToServer.writeBytes(jsonStudent + "\n");

        socket.close();
    }
}
