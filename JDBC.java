import java.sql.*;
import java.util.Scanner;

public class JDBC {

    public static void main(String[] args) {
        String dbName = "EventManagement";
        String urlRoot = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
        String urlWithDB = "jdbc:mysql://localhost:3306/" + dbName + "?serverTimezone=UTC";
        String user = "root";
        String password = "root";

        String[] schema = {
                "CREATE TABLE IF NOT EXISTS event (event_id INT NOT NULL PRIMARY KEY, name VARCHAR(255), start_date DATE, end_date DATE, description TEXT)",
                "CREATE TABLE IF NOT EXISTS event_organizer (organizer_id INT NOT NULL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), phone_number VARCHAR(15))",
                "CREATE TABLE IF NOT EXISTS event_event_organizer (event_id INT NOT NULL, organizer_id INT NOT NULL, PRIMARY KEY(event_id, organizer_id))",
                "CREATE TABLE IF NOT EXISTS venue (venue_id INT NOT NULL PRIMARY KEY, name VARCHAR(255), location VARCHAR(255), capacity INT)",
                "CREATE TABLE IF NOT EXISTS event_venue (event_id INT NOT NULL, venue_id INT NOT NULL, PRIMARY KEY(event_id, venue_id))",
                "CREATE TABLE IF NOT EXISTS participant (participant_id INT NOT NULL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), phone_number VARCHAR(15))",
                "CREATE TABLE IF NOT EXISTS event_participant (event_id INT NOT NULL, participant_id INT NOT NULL, registration_date DATE, PRIMARY KEY(event_id, participant_id))",
                "CREATE TABLE IF NOT EXISTS session (session_id INT NOT NULL PRIMARY KEY, event_id INT, title VARCHAR(255), start_time DATETIME, end_time DATETIME, description TEXT)",
                "CREATE TABLE IF NOT EXISTS speaker (speaker_id INT NOT NULL PRIMARY KEY, name VARCHAR(255), biography TEXT, email VARCHAR(255))",
                "CREATE TABLE IF NOT EXISTS session_speaker (session_id INT NOT NULL, speaker_id INT NOT NULL, PRIMARY KEY(session_id, speaker_id))",
                "CREATE TABLE IF NOT EXISTS session_enrollment (session_id INT NOT NULL, participant_id INT NOT NULL, enrollment_date DATE, PRIMARY KEY(session_id, participant_id))",
                "ALTER TABLE event_event_organizer ADD FOREIGN KEY(event_id) REFERENCES event(event_id)",
                "ALTER TABLE event_event_organizer ADD FOREIGN KEY(organizer_id) REFERENCES event_organizer(organizer_id)",
                "ALTER TABLE event_venue ADD FOREIGN KEY(event_id) REFERENCES event(event_id)",
                "ALTER TABLE event_venue ADD FOREIGN KEY(venue_id) REFERENCES venue(venue_id)",
                "ALTER TABLE event_participant ADD FOREIGN KEY(event_id) REFERENCES event(event_id)",
                "ALTER TABLE event_participant ADD FOREIGN KEY(participant_id) REFERENCES participant(participant_id)",
                "ALTER TABLE session ADD FOREIGN KEY(event_id) REFERENCES event(event_id)",
                "ALTER TABLE session_speaker ADD FOREIGN KEY(session_id) REFERENCES session(session_id)",
                "ALTER TABLE session_speaker ADD FOREIGN KEY(speaker_id) REFERENCES speaker(speaker_id)",
                "ALTER TABLE session_enrollment ADD FOREIGN KEY(participant_id) REFERENCES participant(participant_id)",
                "ALTER TABLE session_enrollment ADD FOREIGN KEY(session_id) REFERENCES session(session_id)"
        };

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection rootConn = DriverManager.getConnection(urlRoot, user, password);
                 Statement q = rootConn.createStatement()) {
                q.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                System.out.println("Database ensured: " + dbName);
            }

            try (Connection conn = DriverManager.getConnection(urlWithDB, user, password);
                 Statement q = conn.createStatement()) {

                for (String sql : schema) {
                    try {
                        q.executeUpdate(sql);
                    } catch (SQLException e) {
                        if (!e.getMessage().contains("already exists"))
                            System.out.println("Error running SQL: " + sql + "\n" + e.getMessage());
                    }
                }

                System.out.println("Tables and constraints created successfully!");

                insertData(conn);
                System.out.println("Dummy data inserted.\n");

                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.println("\nMenu:");
                    System.out.println("1. Insert Event");
                    System.out.println("2. View Events");
                    System.out.println("3. Update Event Name");
                    System.out.println("4. Exit");
                    System.out.print("Choose: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1 -> insertEvent(conn, scanner);
                        case 2 -> selectEvents(conn);
                        case 3 -> {
                            System.out.print("Event ID: ");
                            int id = scanner.nextInt(); scanner.nextLine();
                            System.out.print("New Name: ");
                            String name = scanner.nextLine();
                            updateEventName(conn, id, name);
                        }
                        case 4 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid option");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void insertData(Connection conn) throws SQLException {
        Statement q = conn.createStatement();

        q.executeUpdate("INSERT IGNORE INTO event VALUES (1, 'Tech Fest', '2025-08-10', '2025-08-12', 'Annual tech conference')");
        q.executeUpdate("INSERT IGNORE INTO event VALUES (2, 'Music Gala', '2025-09-01', '2025-09-02', 'Open mic & concerts')");

        q.executeUpdate("INSERT IGNORE INTO event_organizer VALUES (1, 'John Doe', 'john@example.com', '9876543210')");
        q.executeUpdate("INSERT IGNORE INTO event_event_organizer VALUES (1, 1)");

        q.executeUpdate("INSERT IGNORE INTO venue VALUES (1, 'Auditorium A', 'Main Campus', 300)");
        q.executeUpdate("INSERT IGNORE INTO event_venue VALUES (1, 1)");

        q.executeUpdate("INSERT IGNORE INTO participant VALUES (101, 'Alice', 'alice@mail.com', '9876543211')");
        q.executeUpdate("INSERT IGNORE INTO event_participant VALUES (1, 101, '2025-07-25')");

        q.executeUpdate("INSERT IGNORE INTO session VALUES (10, 1, 'AI Workshop', '2025-08-10 10:00:00', '2025-08-10 12:00:00', 'Intro to AI')");
        q.executeUpdate("INSERT IGNORE INTO speaker VALUES (201, 'Dr. Smith', 'AI Researcher', 'smith@ai.org')");
        q.executeUpdate("INSERT IGNORE INTO session_speaker VALUES (10, 201)");
        q.executeUpdate("INSERT IGNORE INTO session_enrollment VALUES (10, 101, '2025-07-26')");
    }

    private static void insertEvent(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Event ID: ");
        int id = scanner.nextInt(); scanner.nextLine();
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Start Date (YYYY-MM-DD): ");
        String start = scanner.nextLine();
        System.out.print("End Date (YYYY-MM-DD): ");
        String end = scanner.nextLine();
        System.out.print("Description: ");
        String desc = scanner.nextLine();

        String query = "INSERT INTO event VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            ps.setString(5, desc);
            ps.executeUpdate();
            System.out.println("Event inserted.");
        }
    }

    private static void selectEvents(Connection conn) throws SQLException {
        String query = "SELECT * FROM event";
        try (Statement q = conn.createStatement(); ResultSet rs = q.executeQuery(query)) {
            System.out.println("\nEvents:");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s | Start: %s | End: %s | Desc: %s\n",
                        rs.getInt("event_id"), rs.getString("name"),
                        rs.getDate("start_date"), rs.getDate("end_date"),
                        rs.getString("description"));
            }
        }
    }

    private static void updateEventName(Connection conn, int eventId, String newName) {
        String query = "UPDATE event SET name = ? WHERE event_id = ?";
        try (PreparedStatement pq = conn.prepareStatement(query)) {
            pq.setString(1, newName);
            pq.setInt(2, eventId);
            int affected = pq.executeUpdate();
            System.out.println("Event updated: " + affected + " row(s) affected.");
        } catch (SQLException e) {
            System.out.println("Failed to update event: " + e.getMessage());
        }
    }
}
