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
 * Admin AI Analytics panel.
 *
 * Shows:
 *   - Model status (loaded / not loaded)
 *   - Key metrics: Total Rules, Avg Confidence, Avg Lift, Training Time
 *   - Top 10 association rules table (antecedents -> consequents)
 *   - Demo notice banner
 *
 * Data source: GET /api/v1/analytics/ai (Python FastAPI)
 */
public class AiAnalyticsPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(AiAnalyticsPanel.class.getName());

    // ── Metric labels ─────────────────────────────────────────────────────────
    private final JLabel statusLbl     = valueLbl("—", false);
    private final JLabel totalRulesLbl = valueLbl("—", true);
    private final JLabel avgConfLbl    = valueLbl("—", true);
    private final JLabel avgLiftLbl    = valueLbl("—", true);
    private final JLabel trainTimeLbl  = valueLbl("—", false);
    private final JLabel datasetLbl    = valueLbl("—", false);
    private final JLabel trainedAtLbl  = valueLbl("—", false);
    private final JLabel noteLbl       = new JLabel(" ");

    // ── Rules table ───────────────────────────────────────────────────────────
    private final DefaultTableModel rulesModel = new DefaultTableModel(
        new String[]{"Antecedents", "Consequents", "Confidence", "Lift", "Support", "Reason"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable rulesTable = new JTable(rulesModel);

    // ── Status bar ────────────────────────────────────────────────────────────
    private final JLabel statusBar = new JLabel("Press Refresh to load AI data");

    public AiAnalyticsPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),       BorderLayout.NORTH);
        add(buildBody(),         BorderLayout.CENTER);
        add(buildStatusBar(),    BorderLayout.SOUTH);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(16, 20, 12, 20));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("AI Analytics");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub   = new JLabel("Apriori Association Rules — Groceries Dataset (Phase 2 Validation)");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        titles.add(title); titles.add(sub);

        RoundedButton refreshBtn = new RoundedButton("Refresh", RoundedButton.Style.PRIMARY);
        refreshBtn.addActionListener(e -> loadData());

        h.add(titles,    BorderLayout.WEST);
        h.add(refreshBtn, BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        // Demo notice banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(0xFFF3CD));
        banner.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 1, 0, new Color(0xFFD700)),
            new EmptyBorder(8, 20, 8, 20)));
        JLabel bannerLbl = new JLabel(
            "<html><b>Demo AI Recommendation</b> — Trained on the public Groceries dataset for validation. " +
            "This model will be replaced by one trained on the Cafe's own Orders and OrderItems tables.</html>");
        bannerLbl.setFont(AppConfig.FONT_SMALL);
        bannerLbl.setForeground(new Color(0x856404));
        banner.add(bannerLbl, BorderLayout.CENTER);

        // Metrics row
        JPanel metrics = buildMetricsPanel();

        // Rules table
        JPanel tablePanel = buildRulesTable();

        // Note at bottom
        noteLbl.setFont(new Font(AppConfig.FONT_SMALL.getName(), Font.ITALIC, 11));
        noteLbl.setForeground(AppConfig.COLOR_TEXT_HINT);
        noteLbl.setBorder(new EmptyBorder(6, 20, 6, 20));

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setOpaque(false);
        body.add(banner,     BorderLayout.NORTH);
        body.add(metrics,    BorderLayout.CENTER);
        body.add(tablePanel, BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildMetricsPanel() {
        Object[][] cards = {
            {"AI Status",      statusLbl},
            {"Total Rules",    totalRulesLbl},
            {"Avg Confidence", avgConfLbl},
            {"Avg Lift",       avgLiftLbl},
            {"Training Time",  trainTimeLbl},
            {"Trained At",     trainedAtLbl},
        };

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(14, 20, 14, 20));
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
            new EmptyBorder(12, 14, 12, 14)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppConfig.FONT_SMALL);
        lbl.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRulesTable() {
        // Style table
        rulesTable.setFont(AppConfig.FONT_SMALL);
        rulesTable.setRowHeight(30);
        rulesTable.setShowVerticalLines(false);
        rulesTable.setShowHorizontalLines(true);
        rulesTable.setGridColor(AppConfig.COLOR_BORDER);
        rulesTable.getTableHeader().setFont(AppConfig.FONT_LABEL);
        rulesTable.getTableHeader().setBackground(AppConfig.COLOR_SURFACE);
        rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Column widths
        int[] widths = {200, 160, 90, 70, 70, 160};
        for (int i = 0; i < widths.length; i++) {
            rulesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(rulesTable);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 20, 16, 20));

        JLabel tblTitle = new JLabel("Top Association Rules (by Lift)");
        tblTitle.setFont(new Font(AppConfig.FONT_LABEL.getName(), Font.BOLD, AppConfig.FONT_LABEL.getSize()));
        tblTitle.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        tblTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        wrapper.add(tblTitle, BorderLayout.NORTH);
        wrapper.add(scroll,   BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 340));
        return wrapper;
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

        new SwingWorker<JSONObject, Void>() {
            @Override protected JSONObject doInBackground() throws Exception {
                RestApiClient client = new RestApiClient();
                String json = client.get("/analytics/ai");
                return new JSONObject(json);
            }
            @Override protected void done() {
                try {
                    JSONObject data = get();
                    applyData(data);
                    statusBar.setText("AI data loaded successfully from FastAPI.");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to load AI analytics: {0}", e.getMessage());
                    statusBar.setText("AI service unavailable. Start FastAPI server and retry.");
                    statusLbl.setText("Offline");
                    statusLbl.setForeground(AppConfig.COLOR_ERROR);
                }
            }
        }.execute();
    }

    private void applyData(JSONObject d) {
        boolean ready = d.optBoolean("ready", false);

        statusLbl.setText(ready ? "Online - Model Loaded" : "Model Not Loaded");
        statusLbl.setForeground(ready ? AppConfig.COLOR_SUCCESS : AppConfig.COLOR_WARNING);

        totalRulesLbl.setText(String.valueOf(d.optInt("total_rules", 0)));
        avgConfLbl.setText(String.format("%.1f%%", d.optDouble("avg_confidence", 0) * 100));
        avgLiftLbl.setText(String.format("%.3f", d.optDouble("avg_lift", 0)));
        trainTimeLbl.setText(String.format("%.2f s", d.optDouble("training_time_sec", 0)));
        datasetLbl.setText(d.optString("dataset", "—"));
        trainedAtLbl.setText(d.optString("trained_at", "—").replace("T", "  "));
        noteLbl.setText("<html><i>" + d.optString("note", "") + "</i></html>");

        // Rebuild rules table
        rulesModel.setRowCount(0);
        JSONArray rules = d.optJSONArray("top_rules");
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                JSONObject r = rules.getJSONObject(i);
                String ant  = jsonArrayToString(r.optJSONArray("antecedents"));
                String con  = jsonArrayToString(r.optJSONArray("consequents"));
                rulesModel.addRow(new Object[]{
                    ant,
                    con,
                    String.format("%.1f%%", r.optDouble("confidence", 0) * 100),
                    String.format("%.3f",   r.optDouble("lift", 0)),
                    String.format("%.3f",   r.optDouble("support", 0)),
                    r.optString("reason", "—"),
                });
            }
        }
    }

    private static String jsonArrayToString(JSONArray arr) {
        if (arr == null) return "—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) sb.append(" + ");
            sb.append(arr.getString(i));
        }
        return sb.toString();
    }

    // ── Label factories ───────────────────────────────────────────────────────

    private static JLabel valueLbl(String text, boolean big) {
        JLabel l = new JLabel(text);
        l.setFont(big
            ? new Font(AppConfig.FONT_TITLE.getName(), Font.BOLD, 22)
            : new Font(AppConfig.FONT_BODY.getName(), Font.BOLD, 13));
        l.setForeground(big ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_TEXT_PRIMARY);
        return l;
    }
}
