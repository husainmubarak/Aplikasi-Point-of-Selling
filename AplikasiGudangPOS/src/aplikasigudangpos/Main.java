package aplikasigudangpos;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Test koneksi dulu
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Gagal terhubung ke database!");
            System.err.println("Pastikan MySQL Docker sudah running:");
            System.err.println("docker-compose up -d");
            System.exit(1);
        }
        
        // Jalankan aplikasi
        SwingUtilities.invokeLater(() -> {
            new AplikasiGudangPOS().setVisible(true);
        });
    }
}