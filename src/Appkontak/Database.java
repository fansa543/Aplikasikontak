package Appkontak;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
     private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:kontak.db");
                System.out.println("Koneksi Berhasil!");
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Koneksi Gagal: " + e.getMessage());
            }
        }
        return conn;
    }
}
