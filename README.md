# 🗂️ Aplikasi Pengelolaan Kontak (FormKontak.java)

## 📘 Deskripsi
Aplikasi **Pengelolaan Kontak** ini dibuat menggunakan **Java Swing** dan **SQLite** untuk menyimpan data kontak seperti nama, nomor telepon, dan kategori (Keluarga, Teman, Kerja).  
Fitur utama mencakup:
- Tambah, Edit, dan Hapus data kontak.
- Pencarian data berdasarkan nama atau nomor.
- Ekspor & Impor data dalam format CSV.
- Backup otomatis saat penghapusan semua data.
- Tampilan data dalam bentuk `JTable` dan `JList`.

---

### 🧩 Struktur Komponen GUI
Komponen utama yang digunakan:
- `JTextField`: Input untuk **Nama** dan **Nomor Telepon**
- `JComboBox`: Pilihan kategori kontak (`Keluarga`, `Teman`, `Kerja`)
- `JButton`: Tombol aksi (`Tambah`, `Edit`, `Hapus`, `Cari`, `Export`, `Import`, `Keluar`)
- `JTable`: Menampilkan data kontak
- `JList`: Menampilkan daftar singkat nama kontak
- `JFileChooser`: Untuk memilih file CSV saat impor data
- `JOptionPane`: Untuk menampilkan pesan konfirmasi atau notifikasi

---


## Inisialisasi Awal
```java
conn = Database.getConnection();
model = new DefaultTableModel(new String[]{"ID", "Nama", "Nomor", "Kategori"}, 0);
tableKontak.setModel(model);
```
## Tombol Keluar
```java
btnKeluar.addActionListener(e -> {
    int pilih = JOptionPane.showConfirmDialog(this,
        "Yakin ingin keluar aplikasi?",
        "Konfirmasi Keluar",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (pilih == JOptionPane.YES_OPTION) {
        System.exit(0);
    }
});
```
Menampilkan konfirmasi sebelum menutup aplikasi.

## Pembuatan Tabel Otomatis
```java
private void ensureTableExists() {
    String sql = "CREATE TABLE IF NOT EXISTS kontak (id INTEGER PRIMARY KEY AUTOINCREMENT, nama TEXT, nomor TEXT, kategori TEXT)";
    Statement st = conn.createStatement();
    st.execute(sql);
}
```


Jika tabel kontak belum ada di database, maka otomatis akan dibuat.

## Memuat Data ke Tabel dan List
```java
private void loadData(String filter) {
    model.setRowCount(0);
    listModel.clear();
    ResultSet rs = st.executeQuery("SELECT * FROM kontak ORDER BY nama");
}
```

Data dari database diambil dan ditampilkan di JTable serta JList.

## Menambah Data Baru
```java
private void tambahData() {
    String sql = "INSERT INTO kontak (nama, nomor, kategori) VALUES (?, ?, ?)";
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, nama);
    ps.setString(2, nomor);
    ps.setString(3, kategori);
    ps.executeUpdate();
}
```

Data baru disimpan ke database setelah dilakukan validasi nomor telepon (hanya angka, panjang 8–15 digit).

## Mengedit Data
```java
private void editData() {
    String sql = "UPDATE kontak SET nama=?, nomor=?, kategori=? WHERE id=?";
}
```

Mengubah data kontak berdasarkan baris yang dipilih di tabel.

## Menghapus Data
```java
private void hapusData() {
    String sql = "DELETE FROM kontak WHERE id=?";
}
```

Dapat menghapus satu atau beberapa baris sekaligus.
Jika tidak ada baris dipilih, muncul opsi:

Backup & Hapus Semua

Hapus Tanpa Backup

Batal

## Mencari Data
```java
private void cariData() {
    String keyword = JOptionPane.showInputDialog(this, "Masukkan nama atau nomor untuk mencari:");
    loadData(keyword);
}
```

Memfilter tampilan data berdasarkan nama atau nomor telepon.

## Ekspor ke CSV

```java
private void exportCSV() {
    FileWriter fw = new FileWriter("kontak_export.csv");
}
```


Semua data di tabel diekspor ke file kontak_export.csv.

## Impor dari CSV
```java
private void importCSV() {
    JFileChooser chooser = new JFileChooser();
    BufferedReader br = new BufferedReader(new FileReader(file));
}
```


Membaca data dari file CSV dan menambahkannya ke tabel kontak.

## Backup Data
```java
private boolean backupToCSV(String filename) { ... }
```


Digunakan sebelum menghapus semua data, menyimpan cadangan otomatis dalam format .csv.

## 🧠 Validasi Data

Program memeriksa input:

Nama dan Nomor wajib diisi.

Nomor hanya boleh berisi angka.

Panjang nomor harus 8–15 digit.

## 🗄️ Database

Database menggunakan SQLite, dibuat otomatis jika belum ada.
Nama tabel: kontak

| Kolom    |	Tipe Data |	Keterangan                   |
|----------|------------|------------------------------|
| id	     | INTEGER    |	Primary Key (Auto Increment) |
| nama     |	TEXT	    | Nama kontak                  |
| nomor	   |  TEXT	    | Nomor telepon                |
| kategori |	TEXT	    | Jenis kontak                 | 

---

## 📤 Format File CSV 
ID,Nama,Nomor,Kategori
1,Andi,08123456789,Keluarga
2,Budi,082233445566,Teman

---

## tambah:
<img width="958" height="708" alt="image" src="https://github.com/user-attachments/assets/23483b16-610f-4d46-8038-c0c65e78fd3c" />
<img width="962" height="702" alt="image" src="https://github.com/user-attachments/assets/b337f75b-6230-4c66-9b44-76d0a8d93c42" />
---

## edit:
<img width="972" height="707" alt="image" src="https://github.com/user-attachments/assets/8344b1b6-f236-409c-93bf-4cdd4a5c1057" />

---

## hapus bisa semua di backup bisa tidak, bisa juga pilih salah satu:
<img width="1086" height="675" alt="image" src="https://github.com/user-attachments/assets/ea92869f-e47e-4879-97fe-174611e797b9" />

---

## eksprot ketika tidak ada isinya bakal muncul seperti ini, kalau ada isinya maka isinya berhasil:
<img width="974" height="713" alt="image" src="https://github.com/user-attachments/assets/69066afe-5f33-433e-a1f5-2538a5ba36da" />

---

## ketika klik import maka seperti ini:
<img width="1175" height="715" alt="image" src="https://github.com/user-attachments/assets/b9b1bc7d-5c63-4201-b8f0-4bd2793257dd" />

---

## ketika klik cari:
<img width="1167" height="710" alt="image" src="https://github.com/user-attachments/assets/9652080a-3234-42dc-a678-5d70bd71d630" />
<img width="956" height="703" alt="image" src="https://github.com/user-attachments/assets/7ffb0eb5-c12a-441d-930f-8ef51077887f" />

---

## ketika klik keluar:

<img width="958" height="722" alt="image" src="https://github.com/user-attachments/assets/69868d85-c570-4234-924e-0ff62b9762a2" />

---


## 👨‍💻 Pembuat

Nama: Al-Fadilah Nur Sahdan Al-Biya
Project: Latihan 3 – Aplikasi Pengelolaan Kontak
