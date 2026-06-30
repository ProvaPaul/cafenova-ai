package com.smartcafe.view.panel;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.smartcafe.config.AppConfig;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SettingsPanel extends JPanel {

    private static final String PREFS_FILE =
        System.getProperty("user.home") + "/.smartcafe/settings.properties";

    private final JToggleButton darkModeToggle;
    private final JTextField    mysqldumpPath;
    private final JTextField    mysqlPath;
    private final JTextField    backupFolder;

    public SettingsPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Settings");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Configure application appearance and database tools.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        Properties prefs = loadPrefs();
        boolean isDark = !"light".equals(prefs.getProperty("theme", "dark"));

        darkModeToggle = new JToggleButton(isDark ? "Dark Mode  ON" : "Dark Mode  OFF", isDark);
        darkModeToggle.setFont(AppConfig.FONT_BUTTON);
        darkModeToggle.addActionListener(e -> toggleTheme(darkModeToggle.isSelected()));

        mysqldumpPath = pathField(prefs.getProperty("mysqldump",
            detectMysqlTool("mysqldump.exe")));
        mysqlPath     = pathField(prefs.getProperty("mysql",
            detectMysqlTool("mysql.exe")));
        backupFolder  = pathField(prefs.getProperty("backup_folder",
            System.getProperty("user.home") + File.separator + "CafeBackups"));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(card("🌙  Appearance", buildAppearancePanel()));
        content.add(Box.createVerticalStrut(16));
        content.add(card("🗄️  Database Backup & Restore", buildDatabasePanel()));
        content.add(Box.createVerticalStrut(16));
        content.add(card("ℹ️  About", buildAboutPanel()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppConfig.COLOR_BG);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadData() { /* static panel, no DB data needed */ }

    // ── APPEARANCE ────────────────────────────────────────────────────────────

    private JPanel buildAppearancePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        p.setOpaque(false);
        p.add(darkModeToggle);
        JLabel hint = new JLabel("Restart may be needed for full effect on some components.");
        hint.setFont(AppConfig.FONT_SMALL);
        hint.setForeground(AppConfig.COLOR_TEXT_HINT);
        p.add(hint);
        return p;
    }

    // ── DATABASE ──────────────────────────────────────────────────────────────

    private JPanel buildDatabasePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(4, 0, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1; fc.insets = new Insets(4, 0, 4, 4);
        GridBagConstraints bc = new GridBagConstraints();
        bc.insets = new Insets(4, 0, 4, 0);

        int row = 0;
        addPathRow(p, lc, fc, bc, row++, "mysqldump path:", mysqldumpPath);
        addPathRow(p, lc, fc, bc, row++, "mysql path:",     mysqlPath);
        addPathRow(p, lc, fc, bc, row++, "Backup folder:",  backupFolder);

        // Action buttons
        JButton backupBtn  = new JButton("Backup Now");
        JButton restoreBtn = new JButton("Restore…");
        JButton saveBtn    = new JButton("Save Paths");
        backupBtn.setBackground(AppConfig.COLOR_PRIMARY);
        backupBtn.setForeground(Color.WHITE);
        backupBtn.addActionListener(e -> runBackup());
        restoreBtn.addActionListener(e -> runRestore());
        saveBtn.addActionListener(e -> savePaths());

        GridBagConstraints full = new GridBagConstraints();
        full.gridwidth = GridBagConstraints.REMAINDER;
        full.anchor = GridBagConstraints.WEST;
        full.insets = new Insets(10, 0, 4, 0);
        lc.gridy = row; fc.gridy = row; bc.gridy = row;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(backupBtn); btnRow.add(restoreBtn); btnRow.add(saveBtn);
        full.gridy = row;
        p.add(btnRow, full);
        return p;
    }

    private static void addPathRow(JPanel p, GridBagConstraints lc, GridBagConstraints fc,
                                    GridBagConstraints bc, int row, String label, JTextField field) {
        lc.gridy = row; fc.gridy = row; bc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(AppConfig.FONT_LABEL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        p.add(l, lc);
        p.add(field, fc);
        JButton browse = new JButton("…");
        browse.setMargin(new Insets(2, 6, 2, 6));
        browse.addActionListener(e -> {
            JFileChooser fc2 = new JFileChooser();
            fc2.setFileSelectionMode(label.contains("folder") ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            if (fc2.showOpenDialog(p) == JFileChooser.APPROVE_OPTION)
                field.setText(fc2.getSelectedFile().getAbsolutePath());
        });
        p.add(browse, bc);
    }

    // ── ABOUT ─────────────────────────────────────────────────────────────────

    private JPanel buildAboutPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 0, 8, 0));
        for (String line : new String[]{
                "Smart AI-Based Cafe Management System",
                "Version: " + AppConfig.APP_VERSION,
                "Java: " + System.getProperty("java.version"),
                "Platform: " + System.getProperty("os.name")}) {
            JLabel l = new JLabel(line);
            l.setFont(line.contains("Smart AI") ? AppConfig.FONT_BODY : AppConfig.FONT_SMALL);
            l.setForeground(line.contains("Smart AI") ? AppConfig.COLOR_TEXT_PRIMARY : AppConfig.COLOR_TEXT_SECONDARY);
            l.setBorder(new EmptyBorder(2, 0, 2, 0));
            p.add(l);
        }
        return p;
    }

    // ── THEME TOGGLE ──────────────────────────────────────────────────────────

    private void toggleTheme(boolean dark) {
        darkModeToggle.setText(dark ? "Dark Mode  ON" : "Dark Mode  OFF");
        try {
            if (dark) FlatDarkLaf.setup(); else FlatLightLaf.setup();
            for (Window w : Window.getWindows()) SwingUtilities.updateComponentTreeUI(w);
            Properties prefs = loadPrefs();
            prefs.setProperty("theme", dark ? "dark" : "light");
            savePrefs(prefs);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not switch theme: " + ex.getMessage());
        }
    }

    // ── BACKUP ────────────────────────────────────────────────────────────────

    private void runBackup() {
        String dump   = mysqldumpPath.getText().trim();
        String folder = backupFolder.getText().trim();
        if (dump.isEmpty() || folder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Set mysqldump path and backup folder first."); return;
        }
        try {
            Files.createDirectories(Paths.get(folder));
            String ts = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outFile = folder + File.separator + "smart_cafe_" + ts + ".sql";

            // Parse DB URL from properties
            Properties dbProps = loadDbProperties();
            String url = dbProps.getProperty("db.url", "jdbc:mysql://localhost:3307/smart_cafe_db");
            String user = dbProps.getProperty("db.username", "root");
            String pass = dbProps.getProperty("db.password", "");
            String host = parseUrlPart(url, "host");
            String port = parseUrlPart(url, "port");
            String db   = parseUrlPart(url, "db");

            ProcessBuilder pb = new ProcessBuilder(
                dump, "-u", user, "-h", host, "-P", port,
                (pass.isEmpty() ? "" : "-p" + pass), db
            );
            pb.redirectOutput(new File(outFile));
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            int exit = proc.waitFor();
            if (exit == 0)
                JOptionPane.showMessageDialog(this, "Backup saved to:\n" + outFile);
            else {
                String err = new String(proc.getInputStream().readAllBytes());
                JOptionPane.showMessageDialog(this, "Backup failed:\n" + err,
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Backup error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── RESTORE ───────────────────────────────────────────────────────────────

    private void runRestore() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL files", "sql"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File sqlFile = fc.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Restore will OVERWRITE current database data.\nProceed with:\n" + sqlFile.getName() + "?",
            "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String mysql = mysqlPath.getText().trim();
        if (mysql.isEmpty()) { JOptionPane.showMessageDialog(this, "Set mysql path first."); return; }
        try {
            Properties dbProps = loadDbProperties();
            String url  = dbProps.getProperty("db.url", "jdbc:mysql://localhost:3307/smart_cafe_db");
            String user = dbProps.getProperty("db.username", "root");
            String pass = dbProps.getProperty("db.password", "");
            String host = parseUrlPart(url, "host");
            String port = parseUrlPart(url, "port");
            String db   = parseUrlPart(url, "db");

            ProcessBuilder pb = new ProcessBuilder(
                mysql, "-u", user, "-h", host, "-P", port,
                (pass.isEmpty() ? "" : "-p" + pass), db
            );
            pb.redirectInput(sqlFile);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            int exit = proc.waitFor();
            if (exit == 0)
                JOptionPane.showMessageDialog(this, "Database restored successfully.");
            else {
                String err = new String(proc.getInputStream().readAllBytes());
                JOptionPane.showMessageDialog(this, "Restore failed:\n" + err, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void savePaths() {
        Properties prefs = loadPrefs();
        prefs.setProperty("mysqldump",     mysqldumpPath.getText().trim());
        prefs.setProperty("mysql",         mysqlPath.getText().trim());
        prefs.setProperty("backup_folder", backupFolder.getText().trim());
        savePrefs(prefs);
        JOptionPane.showMessageDialog(this, "Paths saved.");
    }

    private static Properties loadPrefs() {
        Properties p = new Properties();
        try { p.load(new FileReader(PREFS_FILE)); } catch (Exception ignored) {}
        return p;
    }

    private static void savePrefs(Properties p) {
        try {
            Files.createDirectories(Paths.get(PREFS_FILE).getParent());
            p.store(new FileWriter(PREFS_FILE), "SmartCafe settings");
        } catch (Exception ignored) {}
    }

    private static Properties loadDbProperties() {
        Properties p = new Properties();
        try (var in = SettingsPanel.class.getResourceAsStream("/config/database.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) {}
        return p;
    }

    private static String parseUrlPart(String url, String part) {
        // jdbc:mysql://host:port/db?...
        try {
            String stripped = url.replace("jdbc:mysql://", "");
            String hostPort = stripped.split("/")[0];
            String dbAndQuery = stripped.split("/")[1].split("\\?")[0];
            String[] hp = hostPort.split(":");
            return switch (part) {
                case "host" -> hp[0];
                case "port" -> hp.length > 1 ? hp[1] : "3306";
                case "db"   -> dbAndQuery;
                default     -> "";
            };
        } catch (Exception e) {
            return switch (part) { case "host" -> "localhost"; case "port" -> "3307"; case "db" -> "smart_cafe_db"; default -> ""; };
        }
    }

    private static String detectMysqlTool(String exe) {
        for (String base : new String[]{"C:\\xampp\\mysql\\bin\\", "C:\\mysql\\bin\\", "/usr/bin/"}) {
            File f = new File(base + exe);
            if (f.exists()) return f.getAbsolutePath();
        }
        return exe; // fallback: assume on PATH
    }

    private static JTextField pathField(String value) {
        JTextField f = new JTextField(value);
        f.setFont(AppConfig.FONT_SMALL);
        f.setPreferredSize(new Dimension(320, 30));
        return f;
    }

    private static JPanel card(String heading, JPanel content) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(AppConfig.COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConfig.COLOR_BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel h = new JLabel(heading);
        h.setFont(AppConfig.FONT_LABEL);
        h.setForeground(AppConfig.COLOR_PRIMARY);
        h.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(h, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }
}
