package aplikasigudangpos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class AplikasiGudangPOS extends JFrame {
    // Komponen GUI
    private JTextField txtKodeProduk, txtNamaProduk, txtHarga, txtStok, txtKategori;
    private JTextField txtCari, txtJumlahStok, txtKeterangan;
    private JTable tableProduk, tableRiwayat;
    private DefaultTableModel modelProduk, modelRiwayat;
    private JButton btnTambah, btnUpdate, btnHapus, btnCari, btnReset;
    private JButton btnStokMasuk, btnStokKeluar, btnLihatRiwayat;
    private JComboBox<String> cmbKategori;
    private DecimalFormat df = new DecimalFormat("#,###.00");
    
    // Database
    private Connection conn;
    private int selectedId = -1;
    
    public AplikasiGudangPOS() {
        setTitle("Aplikasi Manajemen Gudang");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Koneksi Database
        connectDatabase();
        
        // Setup GUI
        setupGUI();
        
        // Load Data
        loadProduk();
    }
    
    private void connectDatabase() {
    conn = DatabaseConnection.getConnection();
    if (conn == null) {
        JOptionPane.showMessageDialog(this, 
            "Koneksi ke database gagal!\nPastikan MySQL Docker sudah running.");
    }
}
    
    private void setupGUI() {
        // Main Panel dengan Tab
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Manajemen Produk
        JPanel panelProduk = createPanelProduk();
        tabbedPane.addTab("Manajemen Produk", panelProduk);
        
        // Tab 2: Stok Barang
        JPanel panelStok = createPanelStok();
        tabbedPane.addTab("Manajemen Stok", panelStok);
        
        // Tab 3: Laporan
        JPanel panelLaporan = createPanelLaporan();
        tabbedPane.addTab("Laporan & Riwayat", panelLaporan);
        
        add(tabbedPane);
        
        // Event Listeners
        setupEventListeners();
    }
    
    // FITUR 1: Manajemen Data Produk (CRUD)
    private JPanel createPanelProduk() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Input
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Data Produk"));
        
        txtKodeProduk = new JTextField();
        txtNamaProduk = new JTextField();
        txtHarga = new JTextField();
        txtStok = new JTextField();
        
        String[] kategoriList = {"Makanan", "Minuman", "Kebersihan", "Elektronik", "Lainnya"};
        cmbKategori = new JComboBox<>(kategoriList);
        
        btnTambah = new JButton("Tambah Produk");
        btnTambah.setBackground(new Color(76, 175, 80));
        btnTambah.setForeground(Color.WHITE);
        
        btnUpdate = new JButton("Update Produk");
        btnUpdate.setBackground(new Color(33, 150, 243));
        btnUpdate.setForeground(Color.WHITE);
        
        btnHapus = new JButton("Hapus Produk");
        btnHapus.setBackground(new Color(244, 67, 54));
        btnHapus.setForeground(Color.WHITE);
        
        btnReset = new JButton("Reset Form");
        
        inputPanel.add(new JLabel("Kode Produk:"));
        inputPanel.add(txtKodeProduk);
        inputPanel.add(new JLabel("Nama Produk:"));
        inputPanel.add(txtNamaProduk);
        inputPanel.add(new JLabel("Harga (Rp):"));
        inputPanel.add(txtHarga);
        inputPanel.add(new JLabel("Stok Awal:"));
        inputPanel.add(txtStok);
        inputPanel.add(new JLabel("Kategori:"));
        inputPanel.add(cmbKategori);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnTambah);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);
        inputPanel.add(new JLabel());
        inputPanel.add(buttonPanel);
        
        // Panel Pencarian
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Pencarian"));
        txtCari = new JTextField(30);
        btnCari = new JButton("Cari");
        searchPanel.add(new JLabel("Cari Produk:"));
        searchPanel.add(txtCari);
        searchPanel.add(btnCari);
        
        // Tabel Produk
        String[] columns = {"ID", "Kode", "Nama Produk", "Harga", "Stok", "Kategori"};
        modelProduk = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableProduk = new JTable(modelProduk);
        tableProduk.getColumnModel().getColumn(0).setMinWidth(0);
        tableProduk.getColumnModel().getColumn(0).setMaxWidth(0);
        JScrollPane scrollPane = new JScrollPane(tableProduk);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // FITUR 2: Manajemen Stok (Stok Masuk & Keluar)
    private JPanel createPanelStok() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Input Stok
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Transaksi Stok"));
        
        JTextField txtKodeProdukStok = new JTextField();
        JTextField txtNamaProdukStok = new JTextField();
        txtNamaProdukStok.setEditable(false);
        txtJumlahStok = new JTextField();
        txtKeterangan = new JTextField();
        
        btnStokMasuk = new JButton("Stok Masuk");
        btnStokMasuk.setBackground(new Color(76, 175, 80));
        btnStokMasuk.setForeground(Color.WHITE);
        
        btnStokKeluar = new JButton("Stok Keluar");
        btnStokKeluar.setBackground(new Color(255, 152, 0));
        btnStokKeluar.setForeground(Color.WHITE);
        
        inputPanel.add(new JLabel("Kode Produk:"));
        inputPanel.add(txtKodeProdukStok);
        inputPanel.add(new JLabel("Nama Produk:"));
        inputPanel.add(txtNamaProdukStok);
        inputPanel.add(new JLabel("Jumlah:"));
        inputPanel.add(txtJumlahStok);
        inputPanel.add(new JLabel("Keterangan:"));
        inputPanel.add(txtKeterangan);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnStokMasuk);
        buttonPanel.add(btnStokKeluar);
        
        // Tabel Stok Produk
        String[] columns = {"Kode", "Nama Produk", "Stok Saat Ini", "Kategori"};
        DefaultTableModel modelStok = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tableStok = new JTable(modelStok);
        JScrollPane scrollPane = new JScrollPane(tableStok);
        
        // Load data stok
        loadStok(modelStok);
        
        // Event: Pilih produk dari tabel
        tableStok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableStok.getSelectedRow();
                if (row != -1) {
                    txtKodeProdukStok.setText(modelStok.getValueAt(row, 0).toString());
                    txtNamaProdukStok.setText(modelStok.getValueAt(row, 1).toString());
                }
            }
        });
        
        // Event: Cari detail produk
        txtKodeProdukStok.addActionListener(e -> {
            cariProdukStok(txtKodeProdukStok.getText(), txtNamaProdukStok);
        });
        
        // Event: Stok Masuk
        btnStokMasuk.addActionListener(e -> {
            prosesStok(txtKodeProdukStok.getText(), txtJumlahStok.getText(), 
                      txtKeterangan.getText(), "MASUK", modelStok);
        });
        
        // Event: Stok Keluar
        btnStokKeluar.addActionListener(e -> {
            prosesStok(txtKodeProdukStok.getText(), txtJumlahStok.getText(), 
                      txtKeterangan.getText(), "KELUAR", modelStok);
        });
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // FITUR 3: Laporan dan Riwayat Stok
    private JPanel createPanelLaporan() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Riwayat"));
        
        JTextField txtKodeProdukRiwayat = new JTextField(15);
        btnLihatRiwayat = new JButton("Lihat Riwayat");
        JButton btnSemuaRiwayat = new JButton("Semua Riwayat");
        JButton btnRefresh = new JButton("Refresh");
        
        filterPanel.add(new JLabel("Kode Produk:"));
        filterPanel.add(txtKodeProdukRiwayat);
        filterPanel.add(btnLihatRiwayat);
        filterPanel.add(btnSemuaRiwayat);
        filterPanel.add(btnRefresh);
        
        // Tabel Riwayat
        String[] columns = {"Tanggal", "Kode Produk", "Nama Produk", "Jenis", "Jumlah", "Keterangan"};
        modelRiwayat = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableRiwayat = new JTable(modelRiwayat);
        JScrollPane scrollPane = new JScrollPane(tableRiwayat);
        
        // Panel Statistik
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistik Gudang"));
        
        JLabel lblTotalProduk = new JLabel("Total Produk: 0", SwingConstants.CENTER);
        JLabel lblTotalStok = new JLabel("Total Stok: 0", SwingConstants.CENTER);
        JLabel lblNilaiInventori = new JLabel("Nilai Inventori: Rp 0", SwingConstants.CENTER);
        
        lblTotalProduk.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalStok.setFont(new Font("Arial", Font.BOLD, 14));
        lblNilaiInventori.setFont(new Font("Arial", Font.BOLD, 14));
        
        statsPanel.add(lblTotalProduk);
        statsPanel.add(lblTotalStok);
        statsPanel.add(lblNilaiInventori);
        
        // Load statistik
        loadStatistik(lblTotalProduk, lblTotalStok, lblNilaiInventori);
        
        // Event listeners
        btnLihatRiwayat.addActionListener(e -> {
            loadRiwayatByProduk(txtKodeProdukRiwayat.getText());
        });
        
        btnSemuaRiwayat.addActionListener(e -> {
            loadSemuaRiwayat();
        });
        
        btnRefresh.addActionListener(e -> {
            loadSemuaRiwayat();
            loadStatistik(lblTotalProduk, lblTotalStok, lblNilaiInventori);
        });
        
        // Load riwayat awal
        loadSemuaRiwayat();
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Klik tabel produk
        tableProduk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableProduk.getSelectedRow();
                if (row != -1) {
                    selectedId = Integer.parseInt(modelProduk.getValueAt(row, 0).toString());
                    txtKodeProduk.setText(modelProduk.getValueAt(row, 1).toString());
                    txtNamaProduk.setText(modelProduk.getValueAt(row, 2).toString());
                    txtHarga.setText(modelProduk.getValueAt(row, 3).toString().replace("Rp ", "").replace(",", ""));
                    txtStok.setText(modelProduk.getValueAt(row, 4).toString());
                    cmbKategori.setSelectedItem(modelProduk.getValueAt(row, 5).toString());
                }
            }
        });
        
        // Tambah produk
        btnTambah.addActionListener(e -> tambahProduk());
        
        // Update produk
        btnUpdate.addActionListener(e -> updateProduk());
        
        // Hapus produk
        btnHapus.addActionListener(e -> hapusProduk());
        
        // Reset form
        btnReset.addActionListener(e -> resetForm());
        
        // Cari produk
        btnCari.addActionListener(e -> cariProduk());
        txtCari.addActionListener(e -> cariProduk());
    }
    
    private void loadProduk() {
        modelProduk.setRowCount(0);
        try {
            String sql = "SELECT * FROM produk ORDER BY nama_produk";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                modelProduk.addRow(new Object[]{
                    rs.getInt("id_produk"),
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    "Rp " + df.format(rs.getDouble("harga")),
                    rs.getInt("stok"),
                    rs.getString("kategori")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void tambahProduk() {
        try {
            String kode = txtKodeProduk.getText();
            String nama = txtNamaProduk.getText();
            double harga = Double.parseDouble(txtHarga.getText());
            int stok = Integer.parseInt(txtStok.getText());
            String kategori = cmbKategori.getSelectedItem().toString();
            
            if (kode.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi data produk!");
                return;
            }
            
            String sql = "INSERT INTO produk (kode_produk, nama_produk, harga, stok, kategori) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            pst.setString(2, nama);
            pst.setDouble(3, harga);
            pst.setInt(4, stok);
            pst.setString(5, kategori);
            
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            loadProduk();
            resetForm();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!");
        }
    }
    
    private void updateProduk() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan diupdate!");
            return;
        }
        
        try {
            String kode = txtKodeProduk.getText();
            String nama = txtNamaProduk.getText();
            double harga = Double.parseDouble(txtHarga.getText());
            int stok = Integer.parseInt(txtStok.getText());
            String kategori = cmbKategori.getSelectedItem().toString();
            
            String sql = "UPDATE produk SET kode_produk=?, nama_produk=?, harga=?, stok=?, kategori=? WHERE id_produk=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            pst.setString(2, nama);
            pst.setDouble(3, harga);
            pst.setInt(4, stok);
            pst.setString(5, kategori);
            pst.setInt(6, selectedId);
            
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
            loadProduk();
            resetForm();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!");
        }
    }
    
    private void hapusProduk() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus produk ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM produk WHERE id_produk=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, selectedId);
                pst.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
                loadProduk();
                resetForm();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void cariProduk() {
        modelProduk.setRowCount(0);
        String keyword = txtCari.getText();
        
        try {
            String sql = "SELECT * FROM produk WHERE nama_produk LIKE ? OR kode_produk LIKE ? ORDER BY nama_produk";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                modelProduk.addRow(new Object[]{
                    rs.getInt("id_produk"),
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    "Rp " + df.format(rs.getDouble("harga")),
                    rs.getInt("stok"),
                    rs.getString("kategori")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadStok(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String sql = "SELECT kode_produk, nama_produk, stok, kategori FROM produk ORDER BY nama_produk";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    rs.getInt("stok"),
                    rs.getString("kategori")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void cariProdukStok(String kode, JTextField txtNama) {
        try {
            String sql = "SELECT nama_produk FROM produk WHERE kode_produk = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_produk"));
            } else {
                txtNama.setText("");
                JOptionPane.showMessageDialog(this, "Produk tidak ditemukan!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void prosesStok(String kode, String jumlahStr, String keterangan, String jenis, DefaultTableModel model) {
        try {
            if (kode.isEmpty() || jumlahStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi data!");
                return;
            }
            
            int jumlah = Integer.parseInt(jumlahStr);
            
            conn.setAutoCommit(false);
            
            // Update stok produk
            String sqlUpdate;
            if (jenis.equals("MASUK")) {
                sqlUpdate = "UPDATE produk SET stok = stok + ? WHERE kode_produk = ?";
            } else {
                sqlUpdate = "UPDATE produk SET stok = stok - ? WHERE kode_produk = ?";
            }
            
            PreparedStatement pstUpdate = conn.prepareStatement(sqlUpdate);
            pstUpdate.setInt(1, jumlah);
            pstUpdate.setString(2, kode);
            int updated = pstUpdate.executeUpdate();
            
            if (updated == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Produk tidak ditemukan!");
                return;
            }
            
            // Insert riwayat stok
            String sqlRiwayat = "INSERT INTO riwayat_stok (id_produk, jenis, jumlah, keterangan) " +
                               "VALUES ((SELECT id_produk FROM produk WHERE kode_produk = ?), ?, ?, ?)";
            PreparedStatement pstRiwayat = conn.prepareStatement(sqlRiwayat);
            pstRiwayat.setString(1, kode);
            pstRiwayat.setString(2, jenis);
            pstRiwayat.setInt(3, jumlah);
            pstRiwayat.setString(4, keterangan);
            pstRiwayat.executeUpdate();
            
            conn.commit();
            conn.setAutoCommit(true);
            
            JOptionPane.showMessageDialog(this, "Stok berhasil diupdate!");
            loadStok(model);
            txtJumlahStok.setText("");
            txtKeterangan.setText("");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format jumlah tidak valid!");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadSemuaRiwayat() {
        modelRiwayat.setRowCount(0);
        try {
            String sql = "SELECT r.tanggal, p.kode_produk, p.nama_produk, r.jenis, r.jumlah, r.keterangan " +
                        "FROM riwayat_stok r " +
                        "JOIN produk p ON r.id_produk = p.id_produk " +
                        "ORDER BY r.tanggal DESC LIMIT 100";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                modelRiwayat.addRow(new Object[]{
                    sdf.format(rs.getTimestamp("tanggal")),
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    rs.getString("jenis"),
                    rs.getInt("jumlah"),
                    rs.getString("keterangan")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadRiwayatByProduk(String kode) {
        modelRiwayat.setRowCount(0);
        try {
            String sql = "SELECT r.tanggal, p.kode_produk, p.nama_produk, r.jenis, r.jumlah, r.keterangan " +
                        "FROM riwayat_stok r " +
                        "JOIN produk p ON r.id_produk = p.id_produk " +
                        "WHERE p.kode_produk = ? " +
                        "ORDER BY r.tanggal DESC";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            ResultSet rs = pst.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                modelRiwayat.addRow(new Object[]{
                    sdf.format(rs.getTimestamp("tanggal")),
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    rs.getString("jenis"),
                    rs.getInt("jumlah"),
                    rs.getString("keterangan")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void loadStatistik(JLabel lblProduk, JLabel lblStok, JLabel lblNilai) {
        try {
            // Total produk
            String sql1 = "SELECT COUNT(*) as total FROM produk";
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(sql1);
            if (rs1.next()) {
                lblProduk.setText("Total Produk: " + rs1.getInt("total"));
            }
            
            // Total stok
            String sql2 = "SELECT SUM(stok) as total FROM produk";
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sql2);
            if (rs2.next()) {
                lblStok.setText("Total Stok: " + rs2.getInt("total"));
            }
            
            // Nilai inventori
            String sql3 = "SELECT SUM(harga * stok) as nilai FROM produk";
            Statement stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery(sql3);
            if (rs3.next()) {
                lblNilai.setText("Nilai Inventori: Rp " + df.format(rs3.getDouble("nilai")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void resetForm() {
        txtKodeProduk.setText("");
        txtNamaProduk.setText("");
        txtHarga.setText("");
        txtStok.setText("");
        cmbKategori.setSelectedIndex(0);
        selectedId = -1;
        txtKodeProduk.requestFocus();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AplikasiGudangPOS().setVisible(true);
        });
    }
}