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
public class Kontak {
    private int id;
    private String nama;
    private String nomor;
    private String kategori;

    public Kontak(int id, String nama, String nomor, String kategori) {
        this.id = id;
        this.nama = nama;
        this.nomor = nomor;
        this.kategori = kategori;
    }

    // Getter & Setter
    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getNomor() { return nomor; }
    public String getKategori() { return kategori; }
}

