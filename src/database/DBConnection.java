package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "health_assistance";
    private static final String USER = "health_user";
    private static final String PASSWORD = "health_pass_2026";
    
    private static boolean databaseInitialized = false;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
    }
    
    private static void initializeDatabase() {
        if (databaseInitialized) return;
        
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create database if not exists
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.execute("USE " + DB_NAME);
            
            // Check if users table exists
            java.sql.ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users'");
            if (!rs.next()) {
                System.out.println("Database not found. Creating tables...");
                runSchemaScript(conn);
            } else {
                System.out.println("Database already exists.");
            }
            
            databaseInitialized = true;
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
    
    private static void runSchemaScript(Connection conn) {
        StringBuilder sql = new StringBuilder();
        
        // Try to read schema.sql from different locations
        try (BufferedReader reader = getSchemaReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            
            // Execute SQL script
            try (Statement stmt = conn.createStatement()) {
                for (String query : sql.toString().split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            System.out.println("Schema executed successfully!");
        } catch (Exception e) {
            System.err.println("Failed to run schema.sql: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static BufferedReader getSchemaReader() throws Exception {
        // Try external file first (same folder as EXE)
        File externalFile = new File("schema.sql");
        if (externalFile.exists()) {
            System.out.println("Loading schema from external file");
            return new BufferedReader(new FileReader(externalFile));
        }
        
        // Try from JAR resources
        InputStream is = DBConnection.class.getResourceAsStream("/schema.sql");
        if (is != null) {
            System.out.println("Loading schema from JAR resources");
            return new BufferedReader(new InputStreamReader(is));
        }
        
        // Try from root of classpath
        is = ClassLoader.getSystemResourceAsStream("schema.sql");
        if (is != null) {
            System.out.println("Loading schema from classpath");
            return new BufferedReader(new InputStreamReader(is));
        }
        
        throw new Exception("schema.sql not found anywhere!");
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(BASE_URL + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}