/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Appkontak;

/**
 *
 * @author LENOVO
 */

import java.sql.Connection;
import java.sql.Statement;

public class InitDB {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS kontak (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "nama TEXT NOT NULL," +
                         "nomor TEXT NOT NULL," +
                         "kategori TEXT NOT NULL)";
            Statement st = conn.createStatement();
            st.execute(sql);
            System.out.println("Tabel kontak siap digunakan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    
