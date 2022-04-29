package lez03;

import lez03.StudentProto.StudentMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDate;

public class ProtocolStudentClient {
    public static void main(String[] args) throws IOException {
        String name, surname, residence;
        int yearOfBirth;
        StudentMessage.Builder studentBuilder = StudentMessage.newBuilder();

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

        studentBuilder.setPersonalDetails(StudentMessage.PersonalDetails.newBuilder()
                .setName(name)
                .setSurname(surname)
                .setResidence(residence)
                .setYearOfBirth(yearOfBirth));

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
            studentBuilder.addExams(StudentMessage.Exam.newBuilder()
                    .setExamName(examName)
                    .setMark(mark)
                    .setDateOfVerbalization(StudentMessage.Exam.DateOfVerbalization.newBuilder()
                            .setYear(verbalization.getYear())
                            .setMonth(verbalization.getMonthValue())
                            .setDay(verbalization.getDayOfMonth())));
        }

        StudentMessage student = studentBuilder.build();

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
        student.writeTo(socket.getOutputStream());

        socket.close();
    }
}
