package aplikasigudangpos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static Connection conn;
    
    // Konfigurasi Database Docker
    private static final String HOST = "localhost"; // atau IP Docker host
    private static final String PORT = "3306";
    private static final String DATABASE = "pos_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;
    
    // Singleton Pattern
    public static Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Koneksi Database Berhasil!");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, 
                    "MySQL Driver tidak ditemukan!\n" + e.getMessage());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Koneksi Database Gagal!\n" + 
                    "Pastikan MySQL Docker sudah running.\n" + 
                    e.getMessage());
            }
        }
        return conn;
    }
    
    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Koneksi Database Ditutup");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Test koneksi
    public static boolean testConnection() {
        try {
            Connection testConn = getConnection();
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}