package com.smartcafe.view.panel;

import com.smartcafe.ai.client.RestApiClient;
import com.smartcafe.config.AppConfig;
import com.smartcafe.view.components.RoundedButton;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin AI Analytics Dashboard.
 *
 * Shows:
 *   - 8 metric cards  (Status, Rules, Avg Confidence, Avg Lift,
 *                       Training Time, Dataset Source, Version, Total Sessions)
 *   - Action buttons  (Generate Demo, Retrain AI, Delete Demo, Export Report)
 *   - Top association rules table
 *   - Training history table
 *
 * All data loaded from FastAPI via RestApiClient.
 */
public class AiAnalyticsPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(AiAnalyticsPanel.class.getName());

    // ── Metric labels ─────────────────────────────────────────────────────────
    private final JLabel statusLbl      = valueLbl("—", false);
    private final JLabel totalRulesLbl  = valueLbl("—", true);
    private final JLabel avgConfLbl     = valueLbl("—", true);
    private final JLabel avgLiftLbl     = valueLbl("—", true);
    private final JLabel trainTimeLbl   = valueLbl("—", false);
    private final JLabel datasetSrcLbl  = valueLbl("—", false);
    private final JLabel versionLbl     = valueLbl("—", false);
    private final JLabel sessionsLbl    = valueLbl("—", false);

    // ── Notice banner ─────────────────────────────────────────────────────────
    private final JLabel bannerLbl = new JLabel(" ");
    private final JPanel banner    = new JPanel(new BorderLayout());

    // ── Rules table ───────────────────────────────────────────────────────────
    private final DefaultTableModel rulesModel = new DefaultTableModel(
        new String[]{"Antecedents", "Consequents", "Confidence", "Lift", "Support", "Reason"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable rulesTable = new JTable(rulesModel);

    // ── History table ─────────────────────────────────────────────────────────
    private final DefaultTableModel histModel = new DefaultTableModel(
        new String[]{"#", "Version", "Source", "Rules", "Avg Conf", "Trained At", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable histTable = new JTable(histModel);

    // ── Action buttons ────────────────────────────────────────────────────────
    private final RoundedButton generateBtn = new RoundedButton("Generate Demo Dataset", RoundedButton.Style.PRIMARY);
    private final RoundedButton retrainBtn  = new RoundedButton("Retrain AI",            RoundedButton.Style.PRIMARY);
    private final RoundedButton deleteBtn   = new RoundedButton("Delete Demo Dataset",   RoundedButton.Style.GHOST);
    private final RoundedButton exportBtn   = new RoundedButton("Export AI Report",      RoundedButton.Style.GHOST);
    private final RoundedButton refreshBtn  = new RoundedButton("Refresh",               RoundedButton.Style.GHOST);

    // ── Status bar ────────────────────────────────────────────────────────────
    private final JLabel statusBar = new JLabel("Click 'Refresh' to load AI data.");

    public AiAnalyticsPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));

        JPanel body = buildBody();
        JScrollPane scroll = new JScrollPane(body,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(buildHeader(),    BorderLayout.NORTH);
        add(scroll,           BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        wireButtons();
    }

    // ── Button wiring ─────────────────────────────────────────────────────────

    private void wireButtons() {
        refreshBtn.addActionListener(e -> loadData());

        generateBtn.addActionListener(e -> runAction(
            "Generating demo data (200 customers, 100 products, ~2500 orders)...",
            "admin/generate-demo", "POST", null
        ));

        retrainBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Retrain the AI recommendation engine from the database?\n" +
                "This will replace the current model.",
                "Retrain AI", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                runAction("Training AI model from database (may take 10-60s)...",
                    "admin/retrain", "POST", null);
            }
        });

        deleteBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete ALL demo data (customers, orders, menu items)?\n" +
                "This cannot be undone.",
                "Delete Demo Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                runAction("Deleting demo data...", "admin/demo-data", "DELETE", null);
            }
        });

        exportBtn.addActionListener(e -> exportReport());
    }

    private void setButtonsEnabled(boolean enabled) {
        generateBtn.setEnabled(enabled);
        retrainBtn.setEnabled(enabled);
        deleteBtn.setEnabled(enabled);
        exportBtn.setEnabled(enabled);
        refreshBtn.setEnabled(enabled);
    }

    // ── Generic action runner (POST/DELETE to FastAPI) ────────────────────────

    private void runAction(String loadingMsg, String endpoint, String method, String body) {
        setButtonsEnabled(false);
        statusBar.setText(loadingMsg);

        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                RestApiClient client = new RestApiClient();
                String resp;
                if ("DELETE".equals(method)) {
                    resp = client.delete("/api/v1/" + endpoint);
                } else {
                    resp = client.post("/api/v1/" + endpoint, body != null ? body : "{}");
                }
                return new JSONObject(resp);
            }

            @Override
            protected void done() {
                try {
                    JSONObject result = get();
                    boolean ok = result.optBoolean("success", true);
                    String msg = result.optString("message",
                        result.optString("detail", "Done."));
                    statusBar.setText(msg);
                    if (ok) {
                        JOptionPane.showMessageDialog(AiAnalyticsPanel.this,
                            "<html>" + msg.replace("\n", "<br>") + "</html>",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(AiAnalyticsPanel.this,
                            msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Action failed: {0}", ex.getMessage());
                    statusBar.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(AiAnalyticsPanel.this,
                        "Failed to connect to AI service.\n" + ex.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    // ── Export report (download JSON and save to file chooser) ────────────────

    private void exportReport() {
        setButtonsEnabled(false);
        statusBar.setText("Exporting AI report...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return new RestApiClient().get("/api/v1/admin/export-report");
            }
            @Override
            protected void done() {
                try {
                    String json = get();
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(new java.io.File("ai_report.json"));
                    int r = fc.showSaveDialog(AiAnalyticsPanel.this);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        java.nio.file.Files.writeString(
                            fc.getSelectedFile().toPath(), json,
                            java.nio.charset.StandardCharsets.UTF_8);
                        statusBar.setText("Report saved to " + fc.getSelectedFile().getName());
                        JOptionPane.showMessageDialog(AiAnalyticsPanel.this,
                            "AI report saved successfully.", "Export Done",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusBar.setText("Export cancelled.");
                    }
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Export failed: {0}", ex.getMessage());
                    statusBar.setText("Export failed: " + ex.getMessage());
                }
                setButtonsEnabled(true);
            }
        }.execute();
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(16, 20, 0, 20));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("AI Analytics Dashboard");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Apriori Association Rules — Trained on Cafe Database");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titles.add(title);
        titles.add(sub);

        h.add(titles,     BorderLayout.WEST);
        h.add(refreshBtn, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel bannerPanel = buildBanner();
        JPanel actionPanel = buildActionBar();
        JPanel metricsPanel = buildMetrics();
        JPanel tablesPanel = buildTablesRow();

        // Ensure children stretch to full width in BoxLayout Y_AXIS
        bannerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tablesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(bannerPanel);
        body.add(actionPanel);
        body.add(metricsPanel);
        body.add(tablesPanel);
        return body;
    }

    private JPanel buildBanner() {
        banner.setBackground(new Color(0xFFF3CD));
        banner.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 1, 0, new Color(0xFFD700)),
            new EmptyBorder(8, 20, 8, 20)));
        bannerLbl.setFont(AppConfig.FONT_SMALL);
        bannerLbl.setForeground(new Color(0x856404));
        banner.add(bannerLbl, BorderLayout.CENTER);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        wrapper.add(banner);
        return wrapper;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(6, 12, 2, 12));

        // Style generate + retrain as accent-coloured
        styleActionBtn(generateBtn, new Color(0x28A745), Color.WHITE);
        styleActionBtn(retrainBtn,  new Color(0x007BFF), Color.WHITE);
        styleActionBtn(deleteBtn,   new Color(0xDC3545), Color.WHITE);
        styleActionBtn(exportBtn,   new Color(0x6C757D), Color.WHITE);

        bar.add(generateBtn);
        bar.add(retrainBtn);
        bar.add(deleteBtn);
        bar.add(exportBtn);
        return bar;
    }

    private static void styleActionBtn(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font(AppConfig.FONT_BODY.getName(), Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
    }

    private JPanel buildMetrics() {
        Object[][] cards = {
            {"AI Status",        statusLbl},
            {"Total Rules",      totalRulesLbl},
            {"Avg Confidence",   avgConfLbl},
            {"Avg Lift",         avgLiftLbl},
            {"Training Time",    trainTimeLbl},
            {"Dataset Source",   datasetSrcLbl},
            {"Model Version",    versionLbl},
            {"Training Sessions",sessionsLbl},
        };

        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 20, 8, 20));
        for (Object[] c : cards) {
            grid.add(metricCard((String) c[0], (JLabel) c[1]));
        }
        return grid;
    }

    private JPanel metricCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(AppConfig.COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppConfig.COLOR_BORDER),
            new EmptyBorder(10, 12, 10, 12)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppConfig.FONT_SMALL);
        lbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTablesRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 20, 14, 20));
        row.add(buildRulesTable());
        row.add(buildHistoryTable());
        row.setPreferredSize(new Dimension(0, 320));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        return row;
    }

    private JPanel buildRulesTable() {
        styleTable(rulesTable);
        int[] widths = {180, 140, 80, 70, 70, 140};
        for (int i = 0; i < widths.length; i++) {
            rulesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return tablePanel("Top Association Rules", rulesTable);
    }

    private JPanel buildHistoryTable() {
        styleTable(histTable);
        int[] widths = {30, 120, 90, 60, 75, 130, 70};
        for (int i = 0; i < widths.length; i++) {
            histTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return tablePanel("Training History", histTable);
    }

    private static void styleTable(JTable table) {
        table.setFont(AppConfig.FONT_SMALL);
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(AppConfig.COLOR_BORDER);
        table.getTableHeader().setFont(AppConfig.FONT_LABEL);
        table.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private JPanel tablePanel(String title, JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.COLOR_BORDER));

        JLabel tblTitle = new JLabel(title);
        tblTitle.setFont(new Font(AppConfig.FONT_LABEL.getName(), Font.BOLD,
            AppConfig.FONT_LABEL.getSize()));
        tblTitle.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        tblTitle.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.add(tblTitle, BorderLayout.NORTH);
        p.add(scroll,   BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        p.setBackground(AppConfig.COLOR_SURFACE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.COLOR_BORDER));
        statusBar.setFont(AppConfig.FONT_SMALL);
        statusBar.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        p.add(statusBar);
        return p;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    public void loadData() {
        statusBar.setText("Connecting to AI service...");
        statusLbl.setText("Loading...");

        // Load AI analytics + training history in parallel SwingWorkers
        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                RestApiClient client = new RestApiClient();
                String aiJson   = client.get("/api/v1/analytics/ai");
                String histJson = client.get("/api/v1/admin/training-history");
                String statJson = client.get("/api/v1/admin/ai-status");
                JSONObject combined = new JSONObject();
                combined.put("ai",      new JSONObject(aiJson));
                combined.put("history", new JSONObject(histJson));
                combined.put("status",  new JSONObject(statJson));
                return combined;
            }
            @Override
            protected void done() {
                try {
                    JSONObject all = get();
                    applyAiData(all.optJSONObject("ai"));
                    applyStatus(all.optJSONObject("status"));
                    applyHistory(all.optJSONObject("history"));
                    statusBar.setText("AI data refreshed successfully.");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to load AI analytics: {0}", e.getMessage());
                    // Reset all metric labels to a clean offline state
                    statusLbl.setText("Offline");
                    statusLbl.setForeground(AppConfig.COLOR_ERROR);
                    totalRulesLbl.setText("—");
                    avgConfLbl.setText("—");
                    avgLiftLbl.setText("—");
                    trainTimeLbl.setText("—");
                    datasetSrcLbl.setText("—");
                    versionLbl.setText("—");
                    sessionsLbl.setText("—");
                    rulesModel.setRowCount(0);
                    histModel.setRowCount(0);
                    updateBanner("");
                    statusBar.setText("AI service offline. Start FastAPI (python run.py) then click Refresh.");
                }
            }
        }.execute();
    }

    private void applyAiData(JSONObject d) {
        if (d == null) return;
        boolean ready = d.optBoolean("ready", false);

        statusLbl.setText(ready ? "Online" : "Not Loaded");
        statusLbl.setForeground(ready ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_WARNING);

        totalRulesLbl.setText(String.valueOf(d.optInt("total_rules", 0)));
        avgConfLbl.setText(String.format("%.1f%%", d.optDouble("avg_confidence", 0) * 100));
        avgLiftLbl.setText(String.format("%.3f",   d.optDouble("avg_lift", 0)));
        trainTimeLbl.setText(String.format("%.2f s", d.optDouble("training_time_sec", 0)));

        // Update banner based on dataset source
        String src = d.optString("dataset", "");
        updateBanner(src);

        // Rules table
        rulesModel.setRowCount(0);
        JSONArray rules = d.optJSONArray("top_rules");
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                JSONObject r = rules.getJSONObject(i);
                rulesModel.addRow(new Object[]{
                    jsonArrayToString(r.optJSONArray("antecedents")),
                    jsonArrayToString(r.optJSONArray("consequents")),
                    String.format("%.1f%%", r.optDouble("confidence", 0) * 100),
                    String.format("%.3f",   r.optDouble("lift", 0)),
                    String.format("%.3f",   r.optDouble("support", 0)),
                    r.optString("reason", "—"),
                });
            }
        }
    }

    private void applyStatus(JSONObject d) {
        if (d == null) return;
        versionLbl.setText(d.optString("active_version", "—"));
        String src = d.optString("dataset_source", "—");
        datasetSrcLbl.setText(switch (src) {
            case "CAFE_ORDERS"   -> "Cafe Orders (Real)";
            case "DEMO_DATA"     -> "Demo Dataset";
            case "GROCERIES_CSV" -> "Groceries CSV (Public)";
            default -> src;
        });
    }

    private void applyHistory(JSONObject d) {
        if (d == null) return;
        histModel.setRowCount(0);
        JSONArray sessions = d.optJSONArray("sessions");
        if (sessions == null) return;
        sessionsLbl.setText(String.valueOf(sessions.length()));
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject s = sessions.getJSONObject(i);
            String src = s.optString("dataset_source", "—");
            String srcLabel = switch (src) {
                case "CAFE_ORDERS"   -> "Cafe Orders";
                case "DEMO_DATA"     -> "Demo Data";
                case "GROCERIES_CSV" -> "Groceries CSV";
                default -> src;
            };
            String trained = s.optString("trained_at", "—").replace("T", " ");
            if (trained.length() > 19) trained = trained.substring(0, 19);
            histModel.addRow(new Object[]{
                s.optInt("id", 0),
                s.optString("version", "—"),
                srcLabel,
                s.optInt("n_rules", 0),
                String.format("%.1f%%", s.optDouble("avg_confidence", 0) * 100),
                trained,
                s.optString("status", "—"),
            });
        }
    }

    private void updateBanner(String dataset) {
        String msg;
        Color  bg;
        Color  fg;
        Color  border;

        if (dataset.contains("Groceries") || dataset.contains("public")) {
            msg    = "<html><b>Groceries Dataset (Demo)</b> — "
                   + "Generate demo data and retrain to use cafe-specific recommendations.</html>";
            bg     = new Color(0xFFF3CD);
            fg     = new Color(0x856404);
            border = new Color(0xFFD700);
        } else if (dataset.contains("Demo") || dataset.contains("demo")) {
            msg    = "<html><b>Demo Dataset</b> — "
                   + "AI trained on generated demo data. Retrain once you have real customer orders.</html>";
            bg     = new Color(0xD1ECF1);
            fg     = new Color(0x0C5460);
            border = new Color(0xBEE5EB);
        } else if (dataset.contains("Cafe") || dataset.contains("cafe")) {
            msg    = "<html><b>Production Model</b> — "
                   + "AI trained on real cafe order data. Retrain periodically for best accuracy.</html>";
            bg     = new Color(0xD4EDDA);
            fg     = new Color(0x155724);
            border = new Color(0xC3E6CB);
        } else {
            msg    = "<html>Click <b>Generate Demo Dataset</b> then <b>Retrain AI</b> "
                   + "to start the recommendation engine.</html>";
            bg     = new Color(0xFFF3CD);
            fg     = new Color(0x856404);
            border = new Color(0xFFD700);
        }

        banner.setBackground(bg);
        banner.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 1, 0, border),
            new EmptyBorder(8, 20, 8, 20)));
        bannerLbl.setText(msg);
        bannerLbl.setForeground(fg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String jsonArrayToString(JSONArray arr) {
        if (arr == null) return "—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) sb.append(" + ");
            sb.append(arr.getString(i));
        }
        return sb.toString();
    }

    private static JLabel valueLbl(String text, boolean big) {
        JLabel l = new JLabel(text);
        l.setFont(big
            ? new Font(AppConfig.FONT_TITLE.getName(), Font.BOLD, 20)
            : new Font(AppConfig.FONT_BODY.getName(),  Font.BOLD, 13));
        l.setForeground(big ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_TEXT_PRIMARY);
        return l;
    }
}
