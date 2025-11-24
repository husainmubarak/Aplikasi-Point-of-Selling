package aplikasikasirpos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AplikasiKasirPOS extends JFrame {

    // Komponen GUI
    private JTextField txtKodeProduk, txtNamaProduk, txtHarga, txtJumlah;
    private JTextField txtTotal, txtBayar, txtKembalian, txtCariProduk;
    private JTable tableKeranjang, tableProduk;
    private DefaultTableModel modelKeranjang, modelProduk;
    private JButton btnTambah, btnHapus, btnBayar, btnCari, btnReset;
    private DecimalFormat df = new DecimalFormat("#,###.00");

    // Database
    private Connection conn;
    private String kodeTransaksi;
    private double totalBelanja = 0;

    public AplikasiKasirPOS() {
        setTitle("Aplikasi Kasir POS");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Koneksi Database
        connectDatabase();

        // Generate Kode Transaksi
        generateKodeTransaksi();

        // Setup GUI
        setupGUI();

        // Load Data Produk
        loadProduk();
    }

    private void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://server-sql-1-sql-ku.g.aivencloud.com:23682/pos_db_aiven?ssl-mode=REQUIRED", "avnadmin", "AVNS_1lD7dbMHa6a_cN162W7"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi Database Gagal: " + e.getMessage());
        }
    }

    private void generateKodeTransaksi() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        kodeTransaksi = "TRX" + sdf.format(new Date());
    }

    private void setupGUI() {
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Atas - Informasi Transaksi
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Kode Transaksi: " + kodeTransaksi));
        topPanel.add(new JLabel("     Tanggal: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));

        // Panel Tengah - Split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Panel Kiri - Daftar Produk
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Daftar Produk"));

        // Pencarian Produk
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtCariProduk = new JTextField(20);
        btnCari = new JButton("Cari");
        searchPanel.add(new JLabel("Cari Produk:"));
        searchPanel.add(txtCariProduk);
        searchPanel.add(btnCari);

        // Tabel Produk
        String[] columnsProduk = {"Kode", "Nama Produk", "Harga", "Stok"};
        modelProduk = new DefaultTableModel(columnsProduk, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableProduk = new JTable(modelProduk);
        JScrollPane scrollProduk = new JScrollPane(tableProduk);

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(scrollProduk, BorderLayout.CENTER);

        // Panel Kanan - Keranjang dan Input
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        // Panel Input Produk
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Produk"));

        txtKodeProduk = new JTextField();
        txtNamaProduk = new JTextField();
        txtNamaProduk.setEditable(false);
        txtHarga = new JTextField();
        txtHarga.setEditable(false);
        txtJumlah = new JTextField();

        btnTambah = new JButton("Tambah ke Keranjang");
        btnHapus = new JButton("Hapus Item");

        inputPanel.add(new JLabel("Kode Produk:"));
        inputPanel.add(txtKodeProduk);
        inputPanel.add(new JLabel("Nama Produk:"));
        inputPanel.add(txtNamaProduk);
        inputPanel.add(new JLabel("Harga:"));
        inputPanel.add(txtHarga);
        inputPanel.add(new JLabel("Jumlah:"));
        inputPanel.add(txtJumlah);
        inputPanel.add(btnTambah);
        inputPanel.add(btnHapus);

        // Panel Keranjang
        JPanel cartPanel = new JPanel(new BorderLayout(5, 5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Keranjang Belanja"));

        String[] columnsKeranjang = {"Kode", "Nama Produk", "Harga", "Jumlah", "Subtotal"};
        modelKeranjang = new DefaultTableModel(columnsKeranjang, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableKeranjang = new JTable(modelKeranjang);
        JScrollPane scrollKeranjang = new JScrollPane(tableKeranjang);

        cartPanel.add(scrollKeranjang, BorderLayout.CENTER);

        rightPanel.add(inputPanel, BorderLayout.NORTH);
        rightPanel.add(cartPanel, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // Panel Bawah - Pembayaran
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Pembayaran"));

        txtTotal = new JTextField(12);
        txtTotal.setEditable(false);
        txtTotal.setFont(new Font("Arial", Font.BOLD, 16));
        txtBayar = new JTextField(12);
        txtBayar.setFont(new Font("Arial", Font.BOLD, 16));
        txtKembalian = new JTextField(12);
        txtKembalian.setEditable(false);
        txtKembalian.setFont(new Font("Arial", Font.BOLD, 16));

        paymentPanel.add(new JLabel("Total:"));
        paymentPanel.add(txtTotal);
        paymentPanel.add(new JLabel("Bayar:"));
        paymentPanel.add(txtBayar);
        paymentPanel.add(new JLabel("Kembalian:"));
        paymentPanel.add(txtKembalian);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBayar = new JButton("Proses Pembayaran");
        btnBayar.setFont(new Font("Arial", Font.BOLD, 14));
        btnBayar.setBackground(new Color(76, 175, 80));
        btnBayar.setForeground(Color.WHITE);
        btnReset = new JButton("Transaksi Baru");
        buttonPanel.add(btnBayar);
        buttonPanel.add(btnReset);

        bottomPanel.add(paymentPanel);
        bottomPanel.add(buttonPanel);

        // Add to Main Panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event Listeners
        setupEventListeners();
    }

    private void setupEventListeners() {
        // Cari Produk
        btnCari.addActionListener(e -> cariProduk());
        txtCariProduk.addActionListener(e -> cariProduk());

        // Klik Tabel Produk
        tableProduk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableProduk.getSelectedRow();
                if (row != -1) {
                    txtKodeProduk.setText(modelProduk.getValueAt(row, 0).toString());
                    cariDetailProduk();
                }
            }
        });

        // Kode Produk Enter
        txtKodeProduk.addActionListener(e -> cariDetailProduk());

        // Tambah ke Keranjang
        btnTambah.addActionListener(e -> tambahKeKeranjang());

        // Hapus Item
        btnHapus.addActionListener(e -> hapusItem());

        // Hitung Kembalian
        txtBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungKembalian();
            }
        });

        // Proses Pembayaran
        btnBayar.addActionListener(e -> prosesPembayaran());

        // Transaksi Baru
        btnReset.addActionListener(e -> resetTransaksi());
    }

    // FITUR 1: Pencarian Produk
    private void loadProduk() {
        modelProduk.setRowCount(0);
        try {
            String sql = "SELECT kode_produk, nama_produk, harga, stok FROM produk ORDER BY nama_produk";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                modelProduk.addRow(new Object[]{
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    "Rp " + df.format(rs.getDouble("harga")),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading produk: " + e.getMessage());
        }
    }

    private void cariProduk() {
        modelProduk.setRowCount(0);
        String keyword = txtCariProduk.getText();

        try {
            String sql = "SELECT kode_produk, nama_produk, harga, stok FROM produk "
                    + "WHERE nama_produk LIKE ? OR kode_produk LIKE ? ORDER BY nama_produk";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            pst.setString(2, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                modelProduk.addRow(new Object[]{
                    rs.getString("kode_produk"),
                    rs.getString("nama_produk"),
                    "Rp " + df.format(rs.getDouble("harga")),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void cariDetailProduk() {
        String kode = txtKodeProduk.getText();

        try {
            String sql = "SELECT nama_produk, harga, stok FROM produk WHERE kode_produk = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtNamaProduk.setText(rs.getString("nama_produk"));
                txtHarga.setText(df.format(rs.getDouble("harga")));
                txtJumlah.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Produk tidak ditemukan!");
                txtKodeProduk.setText("");
                txtNamaProduk.setText("");
                txtHarga.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // FITUR 2: Manajemen Keranjang Belanja
    private void tambahKeKeranjang() {
        try {
            String kode = txtKodeProduk.getText();
            String nama = txtNamaProduk.getText();
            double harga = Double.parseDouble(txtHarga.getText().replace(",", ""));
            int jumlah = Integer.parseInt(txtJumlah.getText());

            if (kode.isEmpty() || nama.isEmpty() || jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Lengkapi data produk!");
                return;
            }

            // Cek stok
            if (!cekStok(kode, jumlah)) {
                JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
                return;
            }

            double subtotal = harga * jumlah;

            // Cek apakah produk sudah ada di keranjang
            boolean found = false;
            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                if (modelKeranjang.getValueAt(i, 0).equals(kode)) {
                    int jumlahLama = Integer.parseInt(modelKeranjang.getValueAt(i, 3).toString());
                    int jumlahBaru = jumlahLama + jumlah;
                    double subtotalBaru = harga * jumlahBaru;

                    modelKeranjang.setValueAt(jumlahBaru, i, 3);
                    modelKeranjang.setValueAt("Rp " + df.format(subtotalBaru), i, 4);
                    found = true;
                    break;
                }
            }

            if (!found) {
                modelKeranjang.addRow(new Object[]{
                    kode, nama, "Rp " + df.format(harga), jumlah, "Rp " + df.format(subtotal)
                });
            }

            hitungTotal();
            clearInput();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input tidak valid!");
        }
    }

    private boolean cekStok(String kode, int jumlah) {
        try {
            String sql = "SELECT stok FROM produk WHERE kode_produk = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, kode);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("stok") >= jumlah;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void hapusItem() {
        int row = tableKeranjang.getSelectedRow();
        if (row != -1) {
            modelKeranjang.removeRow(row);
            hitungTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih item yang akan dihapus!");
        }
    }

    private void hitungTotal() {
        totalBelanja = 0;
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            String subtotalStr = modelKeranjang.getValueAt(i, 4).toString()
                    .replace("Rp ", "").replace(",", "");
            totalBelanja += Double.parseDouble(subtotalStr);
        }
        txtTotal.setText("Rp " + df.format(totalBelanja));
    }

    // FITUR 3: Proses Pembayaran & Cetak Struk
    private void hitungKembalian() {
        try {
            double bayar = Double.parseDouble(txtBayar.getText().replace(",", ""));
            double kembalian = bayar - totalBelanja;
            txtKembalian.setText("Rp " + df.format(kembalian));
        } catch (NumberFormatException e) {
            txtKembalian.setText("");
        }
    }

    private void prosesPembayaran() {
        if (modelKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }

        try {
            double bayar = Double.parseDouble(txtBayar.getText().replace(",", ""));

            if (bayar < totalBelanja) {
                JOptionPane.showMessageDialog(this, "Uang pembayaran kurang!");
                return;
            }

            double kembalian = bayar - totalBelanja;

            conn.setAutoCommit(false);

            // Simpan Transaksi
            String sqlTrx = "INSERT INTO transaksi (kode_transaksi, tanggal, total, bayar, kembalian, kasir) "
                    + "VALUES (?, NOW(), ?, ?, ?, 'Kasir 1')";
            PreparedStatement pstTrx = conn.prepareStatement(sqlTrx, Statement.RETURN_GENERATED_KEYS);
            pstTrx.setString(1, kodeTransaksi);
            pstTrx.setDouble(2, totalBelanja);
            pstTrx.setDouble(3, bayar);
            pstTrx.setDouble(4, kembalian);
            pstTrx.executeUpdate();

            ResultSet rs = pstTrx.getGeneratedKeys();
            int idTransaksi = 0;
            if (rs.next()) {
                idTransaksi = rs.getInt(1);
            }

            // Simpan Detail Transaksi & Update Stok
            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String kode = modelKeranjang.getValueAt(i, 0).toString();
                String nama = modelKeranjang.getValueAt(i, 1).toString();
                double harga = Double.parseDouble(modelKeranjang.getValueAt(i, 2).toString()
                        .replace("Rp ", "").replace(",", ""));
                int jumlah = Integer.parseInt(modelKeranjang.getValueAt(i, 3).toString());
                double subtotal = Double.parseDouble(modelKeranjang.getValueAt(i, 4).toString()
                        .replace("Rp ", "").replace(",", ""));

                // Insert detail transaksi
                String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_produk, nama_produk, harga, jumlah, subtotal) "
                        + "VALUES (?, (SELECT id_produk FROM produk WHERE kode_produk = ?), ?, ?, ?, ?)";
                PreparedStatement pstDetail = conn.prepareStatement(sqlDetail);
                pstDetail.setInt(1, idTransaksi);
                pstDetail.setString(2, kode);
                pstDetail.setString(3, nama);
                pstDetail.setDouble(4, harga);
                pstDetail.setInt(5, jumlah);
                pstDetail.setDouble(6, subtotal);
                pstDetail.executeUpdate();

                // Update stok
                String sqlStok = "UPDATE produk SET stok = stok - ? WHERE kode_produk = ?";
                PreparedStatement pstStok = conn.prepareStatement(sqlStok);
                pstStok.setInt(1, jumlah);
                pstStok.setString(2, kode);
                pstStok.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            cetakStruk();
            JOptionPane.showMessageDialog(this, "Transaksi berhasil!");
            resetTransaksi();

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void cetakStruk() {
        StringBuilder struk = new StringBuilder();
        struk.append("========================================\n");
        struk.append("           STRUK PEMBAYARAN\n");
        struk.append("========================================\n");
        struk.append("Kode Transaksi: ").append(kodeTransaksi).append("\n");
        struk.append("Tanggal: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
        struk.append("----------------------------------------\n");

        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            String nama = modelKeranjang.getValueAt(i, 1).toString();
            String harga = modelKeranjang.getValueAt(i, 2).toString();
            String jumlah = modelKeranjang.getValueAt(i, 3).toString();
            String subtotal = modelKeranjang.getValueAt(i, 4).toString();

            struk.append(nama).append("\n");
            struk.append("  ").append(jumlah).append(" x ").append(harga)
                    .append(" = ").append(subtotal).append("\n");
        }

        struk.append("----------------------------------------\n");
        struk.append("Total     : ").append(txtTotal.getText()).append("\n");
        struk.append("Bayar     : ").append(txtBayar.getText()).append("\n");
        struk.append("Kembalian : ").append(txtKembalian.getText()).append("\n");
        struk.append("========================================\n");
        struk.append("      TERIMA KASIH ATAS KUNJUNGAN ANDA\n");
        struk.append("========================================\n");

        JTextArea textArea = new JTextArea(struk.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Struk Pembayaran", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetTransaksi() {
        modelKeranjang.setRowCount(0);
        clearInput();
        txtTotal.setText("");
        txtBayar.setText("");
        txtKembalian.setText("");
        totalBelanja = 0;
        generateKodeTransaksi();
        setTitle("Aplikasi Kasir POS - " + kodeTransaksi);
        loadProduk();
    }

    private void clearInput() {
        txtKodeProduk.setText("");
        txtNamaProduk.setText("");
        txtHarga.setText("");
        txtJumlah.setText("");
        txtKodeProduk.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AplikasiKasirPOS().setVisible(true);
        });
    }
}
