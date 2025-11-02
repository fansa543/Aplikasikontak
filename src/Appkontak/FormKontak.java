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
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;



public class FormKontak extends javax.swing.JFrame {

    /**
     * Creates new form FormKontak
     */
    
     Connection conn;
    DefaultTableModel model;
    private DefaultListModel<String> listModel;

    
    public FormKontak() {
        initComponents();
        
        setLocationRelativeTo(null);
        
        // pasang setelah initComponents();
btnKeluar.addActionListener(e -> System.exit(0));

// pasang setelah initComponents();
btnKeluar.addActionListener(e -> {
    int pilih = JOptionPane.showConfirmDialog(this,
        "Yakin ingin keluar aplikasi?",
        "Konfirmasi Keluar",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (pilih == JOptionPane.YES_OPTION) {
        System.exit(0); // atau gunakan dispose(); jika mau hanya tutup window ini
    }
});


        
        conn = Database.getConnection();
        conn = Database.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal. Pastikan sqlite-jdbc ada di Libraries.", "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ensureTableExists();
        
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Nomor", "Kategori"}, 0);
        tableKontak.setModel(model);
        
        // izinkan memilih beberapa baris sekaligus
tableKontak.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        
        listModel = new DefaultListModel<>();
        listKontak.setModel(listModel);
        
        loadData("");
        
        btnTambah.addActionListener(e -> tambahData());
        btnEdit.addActionListener(e -> editData());
        btnHapus.addActionListener(e -> hapusData());
        btnCari.addActionListener(e -> cariData());
        btnExportCSV.addActionListener(e -> exportCSV());
        btnImportCSV.addActionListener(e -> importCSV());
        
         tableKontak.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariTabel();
        });
         
         listKontak.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = listKontak.getSelectedValue();
                if (sel == null) return;
                String[] parts = sel.split(" - ", 2);
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    for (int i = 0; i < tableKontak.getRowCount(); i++) {
                        Object val = tableKontak.getValueAt(i, 0);
                        if (val != null) {
                            int tid = Integer.parseInt(String.valueOf(val));
                            if (tid == id) {
                                tableKontak.setRowSelectionInterval(i, i);
                                tableKontak.scrollRectToVisible(tableKontak.getCellRect(i, 0, true));
                                isiFormDariTabel();
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Gagal parse id dari list: " + ex.getMessage());
                }
            }
        });
         
        
    }
    
    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS kontak ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nama TEXT NOT NULL,"
                + "nomor TEXT NOT NULL,"
                + "kategori TEXT NOT NULL)";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("Tabel kontak dicek/ dibuat (if not exists).");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memastikan tabel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadData(String filter) {
        model.setRowCount(0);
        listModel.clear();

        if (filter != null && !filter.isEmpty()) {
            // gunakan prepared statement untuk filter
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM kontak WHERE nama LIKE ? OR nomor LIKE ? ORDER BY nama COLLATE NOCASE")) {
                ps.setString(1, "%" + filter + "%");
                ps.setString(2, "%" + filter + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String nama = rs.getString("nama");
                        String nomor = rs.getString("nomor");
                        String kategori = rs.getString("kategori");
                        model.addRow(new Object[]{id, nama, nomor, kategori});
                        listModel.addElement(id + " - " + nama);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal memuat data (filtered): " + e.getMessage());
            }
            return;
      
        
    }
    
    try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM kontak ORDER BY nama COLLATE NOCASE")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                String nomor = rs.getString("nomor");
                String kategori = rs.getString("kategori");
                model.addRow(new Object[]{id, nama, nomor, kategori});
                listModel.addElement(id + " - " + nama);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }
    
    
    private void tambahData() {
        String nama = txtNama.getText().trim();
        String nomor = txtNomor.getText().trim();
        String kategori = cbKategori.getSelectedItem().toString();

        if (nama.isEmpty() || nomor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Nomor wajib diisi!");
            return;
        }

        if (!nomor.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh angka!");
            return;
        }

        if (nomor.length() < 8 || nomor.length() > 15) {
            JOptionPane.showMessageDialog(this, "Nomor telepon harus 8–15 digit!");
            return;
        }

        try {
            String sql = "INSERT INTO kontak (nama, nomor, kategori) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, nomor);
            ps.setString(3, kategori);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");
            loadData("");
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah data: " + e.getMessage());
        }
    }
    
    private void editData() {
        int baris = tableKontak.getSelectedRow();
        if (baris == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit!");
            return;
        }

        int id = (int) model.getValueAt(baris, 0);
        String nama = txtNama.getText().trim();
        String nomor = txtNomor.getText().trim();
        String kategori = cbKategori.getSelectedItem().toString();

        if (!nomor.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh angka!");
            return;
        }

        try {
            String sql = "UPDATE kontak SET nama=?, nomor=?, kategori=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, nomor);
            ps.setString(3, kategori);
            ps.setInt(4, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            loadData("");
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update: " + e.getMessage());
        }
    }
    
   private void hapusData() {
    int[] selectedRows = tableKontak.getSelectedRows();

    // Jika ada baris terpilih -> hapus yang dipilih (bisa banyak)
    if (selectedRows != null && selectedRows.length > 0) {
        String msg = selectedRows.length == 1
                ? "Yakin ingin menghapus kontak yang dipilih?"
                : "Yakin ingin menghapus " + selectedRows.length + " kontak yang dipilih?";
        int konfirmasi = JOptionPane.showConfirmDialog(this, msg, "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try {
            conn.setAutoCommit(false);
            String sql = "DELETE FROM kontak WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int viewRow : selectedRows) {
                    int modelRow = tableKontak.convertRowIndexToModel(viewRow);
                    Object idObj = model.getValueAt(modelRow, 0);
                    if (idObj == null) continue;
                    int id = Integer.parseInt(String.valueOf(idObj));
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Kontak terpilih berhasil dihapus.");
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception ex) { /* ignore */ }
        }

        loadData("");
        resetForm();
        return;
    }

    // Tidak ada yang dipilih -> opsi hapus semua (dengan/ tanpa backup / batal)
    Object[] options = {"Backup & Hapus", "Hapus Tanpa Backup", "Batal"};
    int choice = JOptionPane.showOptionDialog(this,
            "Tidak ada kontak dipilih.\nApa yang ingin Anda lakukan?",
            "Konfirmasi Hapus Semua",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);

    if (choice == 2 || choice == JOptionPane.CLOSED_OPTION || choice == -1) {
        // Batal
        return;
    }

    boolean proceed = true;
    if (choice == 0) { // Backup & Hapus
        // buat filename timestamped
        String fname = "kontak_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
        boolean ok = backupToCSV(fname);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Backup berhasil disimpan sebagai: " + fname);
        } else {
            int cont = JOptionPane.showConfirmDialog(this, "Backup gagal. Tetap lanjut hapus semua?", "Backup Gagal", JOptionPane.YES_NO_OPTION);
            if (cont != JOptionPane.YES_OPTION) proceed = false;
        }
    } else if (choice == 1) {
        // Hapus tanpa backup => proceed true
    }

    if (!proceed) return;

    // Lakukan penghapusan semua
    try (Statement st = conn.createStatement()) {
        int deleted = st.executeUpdate("DELETE FROM kontak");
        JOptionPane.showMessageDialog(this, "Berhasil menghapus semua kontak (" + deleted + " baris).");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal menghapus semua: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    loadData("");
    resetForm();
}

    
    private void cariData() {
        String keyword = JOptionPane.showInputDialog(this, "Masukkan nama atau nomor untuk mencari:");
        if (keyword != null) {
            loadData(keyword.trim());
        }
    }
    
    private void exportCSV() {
    // jika tidak ada data, tidak melakukan apa-apa (tampil pesan)
    if (model == null || model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor.", "Ekspor CSV", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    try (FileWriter fw = new FileWriter("kontak_export.csv")) {
        // tulis header
        fw.write("ID,Nama,Nomor,Kategori\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            Object idObj = model.getValueAt(i, 0);
            Object namaObj = model.getValueAt(i, 1);
            Object nomorObj = model.getValueAt(i, 2);
            Object kategoriObj = model.getValueAt(i, 3);

            String id = idObj == null ? "" : String.valueOf(idObj);
            String nama = namaObj == null ? "" : String.valueOf(namaObj).replace(",", " ");
            String nomor = nomorObj == null ? "" : String.valueOf(nomorObj).replace(",", " ");
            String kategori = kategoriObj == null ? "" : String.valueOf(kategoriObj).replace(",", " ");

            fw.write(id + "," + nama + "," + nomor + "," + kategori + "\n");
        }
        fw.flush();
        JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke kontak_export.csv");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal ekspor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    
    private void importCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length == 3) {
                        String sql = "INSERT INTO kontak (nama, nomor, kategori) VALUES (?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, data[0]);
                        ps.setString(2, data[1]);
                        ps.setString(3, data[2]);
                        ps.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Import CSV selesai!");
                loadData("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal import: " + e.getMessage());
            }
        }
    }
    
    private void isiFormDariTabel() {
        int baris = tableKontak.getSelectedRow();
        if (baris != -1) {
            txtNama.setText(model.getValueAt(baris, 1).toString());
            txtNomor.setText(model.getValueAt(baris, 2).toString());
            cbKategori.setSelectedItem(model.getValueAt(baris, 3).toString());
        }
    }
    
    private void resetForm() {
        txtNama.setText("");
        txtNomor.setText("");
        cbKategori.setSelectedIndex(0);
        tableKontak.clearSelection();
    }
    
    private boolean backupToCSV(String filename) {
    try (FileWriter fw = new FileWriter(filename)) {
        // header
        fw.write("ID,Nama,Nomor,Kategori\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            Object idObj = model.getValueAt(i, 0);
            Object namaObj = model.getValueAt(i, 1);
            Object nomorObj = model.getValueAt(i, 2);
            Object kategoriObj = model.getValueAt(i, 3);

            String id = idObj == null ? "" : String.valueOf(idObj);
            String nama = namaObj == null ? "" : String.valueOf(namaObj).replace(",", " ");
            String nomor = nomorObj == null ? "" : String.valueOf(nomorObj).replace(",", " ");
            String kategori = kategoriObj == null ? "" : String.valueOf(kategoriObj).replace(",", " ");

            fw.write(id + "," + nama + "," + nomor + "," + kategori + "\n");
        }
        fw.flush();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


    
    

        
        
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblKategori = new javax.swing.JLabel();
        lblNama = new javax.swing.JLabel();
        lblNomor = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        txtNomor = new javax.swing.JTextField();
        cbKategori = new javax.swing.JComboBox<>();
        btnCari = new javax.swing.JButton();
        btnTambah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnImportCSV = new javax.swing.JButton();
        btnExportCSV = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        scrollTable = new javax.swing.JScrollPane();
        tableKontak = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        listKontak = new javax.swing.JList<>();
        btnKeluar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(237, 231, 177));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblKategori.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblKategori.setText("Kategori");

        lblNama.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblNama.setText("Nama");

        lblNomor.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblNomor.setText("Nomor");

        txtNama.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtNama.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txtNomor.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtNomor.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("")));

        cbKategori.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga", "Teman", "Kerja" }));
        cbKategori.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        cbKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbKategoriActionPerformed(evt);
            }
        });

        btnCari.setBackground(new java.awt.Color(163, 191, 168));
        btnCari.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnCari.setText("Cari");
        btnCari.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnTambah.setBackground(new java.awt.Color(203, 161, 216));
        btnTambah.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnTambah.setText("Tambah");
        btnTambah.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        btnTambah.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        btnHapus.setBackground(new java.awt.Color(221, 157, 72));
        btnHapus.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnImportCSV.setBackground(new java.awt.Color(46, 196, 182));
        btnImportCSV.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnImportCSV.setText("Import");
        btnImportCSV.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnExportCSV.setBackground(new java.awt.Color(177, 204, 116));
        btnExportCSV.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnExportCSV.setText("Export");
        btnExportCSV.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        btnEdit.setBackground(new java.awt.Color(15, 182, 133));
        btnEdit.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tableKontak.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        tableKontak.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        scrollTable.setViewportView(tableKontak);

        listKontak.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        listKontak.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jScrollPane1.setViewportView(listKontak);

        btnKeluar.setBackground(new java.awt.Color(217, 118, 181));
        btnKeluar.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnKeluar.setText("Keluar");
        btnKeluar.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnExportCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(btnImportCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnTambah)
                        .addGap(26, 26, 26)
                        .addComponent(btnCari, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(scrollTable)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNama)
                                .addComponent(lblNomor)
                                .addComponent(lblKategori))
                            .addGap(31, 31, 31)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtNomor, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNama, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)))
                            .addGap(45, 45, 45)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNama)
                            .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNomor)
                            .addComponent(txtNomor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblKategori)
                            .addComponent(cbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCari)
                    .addComponent(btnHapus)
                    .addComponent(btnKeluar)
                    .addComponent(btnEdit)
                    .addComponent(btnTambah))
                .addGap(20, 20, 20)
                .addComponent(scrollTable, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportCSV)
                    .addComponent(btnImportCSV))
                .addContainerGap(183, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Aplikasi Pengelola Kontak");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
            .addGroup(layout.createSequentialGroup()
                .addGap(328, 328, 328)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(140, 140, 140)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(662, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbKategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbKategoriActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FormKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormKontak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new FormKontak().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExportCSV;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImportCSV;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JLabel lblNama;
    private javax.swing.JLabel lblNomor;
    private javax.swing.JList<String> listKontak;
    private javax.swing.JScrollPane scrollTable;
    private javax.swing.JTable tableKontak;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNomor;
    // End of variables declaration//GEN-END:variables
}
