package lez03;

import java.time.LocalDate;
import java.util.ArrayList;

public class ProtocolStudent {
    PersonalDetail details;
    ArrayList<Exam> exams;

    public ProtocolStudent(String name, String surname, String residence, int yearOfBirth, ArrayList<Exam> exams) {
        this.details = new PersonalDetail(name, surname, residence, yearOfBirth);
        this.exams = exams;
    }

    @Override
    public String toString() {
        StringBuilder examsString = new StringBuilder();
        for (Exam e :
                exams) {
            examsString.append(e.toString());
            examsString.append("\n");
        }
        return String.format("%s\n%s---------------------------------------", this.details.toString(), examsString);
    }
}

class PersonalDetail {
    String name, surname, residence;
    int yearOfBirth;

    public PersonalDetail(String name, String surname, String residence, int yearOfBirth) {
        this.name = name;
        this.surname = surname;
        this.residence = residence;
        this.yearOfBirth = yearOfBirth;
    }

    @Override
    public String toString() {
        return String.format("Name: %s\nSurname: %s\nYear of birth: %d\nResidence: %s", name, surname, yearOfBirth, residence);
    }
}

class Exam {
    String examName;
    int mark;
    LocalDate dayOfVerbalization;

    public Exam(String name, int mark, LocalDate verbalization) {
        this.examName = name;
        this.mark = mark;
        this.dayOfVerbalization = verbalization;
    }

    @Override
    public String toString() {
        return "\tExam name: " + this.examName
                + "\n\tExam mark: " + this.mark
                + "\n\tDate of verbalization: " + this.dayOfVerbalization.toString() + "\n"
                + "\t-----------------------------------";
    }
}
