package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL =
        "jdbc:mysql://38.242.246.126:3306/health_assistance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "health_user";
    private static final String DEFAULT_PASSWORD = "StrongPassword123!";

    private static final String URL =
        System.getenv().getOrDefault("HAS_DB_URL", DEFAULT_URL);
    private static final String USER =
        System.getenv().getOrDefault("HAS_DB_USER", DEFAULT_USER);
    private static final String PASSWORD =
        System.getenv().getOrDefault("HAS_DB_PASSWORD", DEFAULT_PASSWORD);

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}