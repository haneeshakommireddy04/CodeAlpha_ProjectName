import java.util.ArrayList;
import java.util.Scanner;

class Student {
    String name;
    double grade;

    Student(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }
}

public class Main {
    static ArrayList<Student> students = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Student Grade Tracker ===");
            System.out.println("1. Add Student");
            System.out.println("2. Display Summary");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> addStudent();
                case "2" -> displaySummary();
                case "3" -> System.exit(0);
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    static void addStudent() {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        double grade;
        while (true) {
            try {
                System.out.print("Enter grade (0-100): ");
                grade = Double.parseDouble(sc.nextLine().trim());
                if (grade < 0 || grade > 100) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid grade. Enter a number between 0 and 100.");
            }
        }
        students.add(new Student(name, grade));
        System.out.println("Student added successfully!");
    }

    static void displaySummary() {
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }

        double total = 0;
        double highest = students.get(0).grade;
        double lowest = students.get(0).grade;

        System.out.println("\n--- Student Grades ---");
        for (Student s : students) {
            System.out.println(s.name + " : " + s.grade);
            total += s.grade;
            if (s.grade > highest) highest = s.grade;
            if (s.grade < lowest) lowest = s.grade;
        }

        double average = total / students.size();

        System.out.println("\nSummary:");
        System.out.println("Average Grade: " + String.format("%.2f", average));
        System.out.println("Highest Grade: " + highest);
        System.out.println("Lowest Grade: " + lowest);
    }
}