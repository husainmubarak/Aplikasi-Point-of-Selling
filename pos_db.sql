-- Database POS (Point of Sale)
CREATE DATABASE IF NOT EXISTS pos_db_aiven;
USE pos_db_aiven;

-- Tabel Produk
CREATE TABLE produk (
    id_produk INT PRIMARY KEY AUTO_INCREMENT,
    kode_produk VARCHAR(50) UNIQUE NOT NULL,
    nama_produk VARCHAR(100) NOT NULL,
    harga DECIMAL(10,2) NOT NULL,
    stok INT NOT NULL DEFAULT 0,
    kategori VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Transaksi
CREATE TABLE transaksi (
    id_transaksi INT PRIMARY KEY AUTO_INCREMENT,
    kode_transaksi VARCHAR(50) UNIQUE NOT NULL,
    tanggal DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    bayar DECIMAL(10,2) NOT NULL,
    kembalian DECIMAL(10,2) NOT NULL,
    kasir VARCHAR(50) NOT NULL
);

-- Tabel Detail Transaksi
CREATE TABLE detail_transaksi (
    id_detail INT PRIMARY KEY AUTO_INCREMENT,
    id_transaksi INT NOT NULL,
    id_produk INT NOT NULL,
    nama_produk VARCHAR(100) NOT NULL,
    harga DECIMAL(10,2) NOT NULL,
    jumlah INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi) ON DELETE CASCADE,
    FOREIGN KEY (id_produk) REFERENCES produk(id_produk)
);

-- Tabel Riwayat Stok
CREATE TABLE riwayat_stok (
    id_riwayat INT PRIMARY KEY AUTO_INCREMENT,
    id_produk INT NOT NULL,
    jenis VARCHAR(20) NOT NULL, -- 'MASUK' atau 'KELUAR'
    jumlah INT NOT NULL,
    keterangan VARCHAR(200),
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_produk) REFERENCES produk(id_produk)
);

-- Insert Data Sample
INSERT INTO produk (kode_produk, nama_produk, harga, stok, kategori) VALUES
('P001', 'Indomie Goreng', 3500.00, 100, 'Makanan'),
('P002', 'Aqua 600ml', 3000.00, 50, 'Minuman'),
('P003', 'Teh Botol Sosro', 4000.00, 75, 'Minuman'),
('P004', 'Roti Tawar Sari Roti', 12000.00, 30, 'Makanan'),
('P005', 'Sabun Lifebuoy', 5000.00, 40, 'Kebersihan');