import java.sql.*;
import java.util.Scanner;

public class CourseEnrollmentSystem {
    // Remote database connection (Course Catalog)
    private static final String REMOTE_DB_URL = "jdbc:mysql://34.133.41.160:3306/Registration_db";
    private static final String REMOTE_DB_USER = "root";
    private static final String REMOTE_DB_PASS = "root123";

    // Local database connection (Enrollment Records)
    private static final String LOCAL_DB_URL = "jdbc:mysql://localhost:3306/academic_db";
    private static final String LOCAL_DB_USER = "root";
    private static final String LOCAL_DB_PASS = "password";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            listAvailableCourses();

            System.out.print("\nEnter Student ID: ");
            int studentId = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            System.out.print("Enter the Course Name you wish to enroll in: ");
            String selectedCourse = scanner.nextLine();

            System.out.print("Specify the Semester (e.g., Spring 2025): ");
            String semester = scanner.nextLine();


            registerEnrollment(studentId, selectedCourse, semester);


            modifyAvailableSeats(selectedCourse);

            System.out.println("Enrollment processed successfully!");
        } catch (Exception e) {
            System.err.println("An error occurred while processing enrollment.");
            e.printStackTrace();
        }
    }

    public static void listAvailableCourses() {
        String query = "SELECT * FROM Course_Catalog";

        try (Connection conn = DriverManager.getConnection(REMOTE_DB_URL, REMOTE_DB_USER, REMOTE_DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\nList of Available Courses:");
            System.out.println("--------------------------------------------");

            while (rs.next()) {
                System.out.printf("Course ID: %d | Course: %s | Remaining Seats: %d\n",
                        rs.getInt("course_id"), rs.getString("course_name"), rs.getInt("seats_available"));
            }
            System.out.println("--------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Failed to retrieve course catalog.");
            e.printStackTrace();
        }
    }

    public static void registerEnrollment(int studentId, String course, String semester) {
        String enrollmentQuery = "INSERT INTO Course_Enrollment (student_id, course_name, semester, enrollment_date) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(LOCAL_DB_URL, LOCAL_DB_USER, LOCAL_DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(enrollmentQuery)) {

            pstmt.setInt(1, studentId);
            pstmt.setString(2, course);
            pstmt.setString(3, semester);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error occurred while enrolling the student.");
            e.printStackTrace();
        }
    }

    public static void modifyAvailableSeats(String course) {
        String updateSeatsQuery = "UPDATE Course_Catalog SET seats_available = seats_available - 1 WHERE course_name = ? AND seats_available > 0";

        try (Connection conn = DriverManager.getConnection(REMOTE_DB_URL, REMOTE_DB_USER, REMOTE_DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(updateSeatsQuery)) {

            pstmt.setString(1, course);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Warning: No available seats remaining for the selected course.");
            }

        } catch (SQLException e) {
            System.err.println("Failed to update seat availability.");
            e.printStackTrace();
        }
    }
}
