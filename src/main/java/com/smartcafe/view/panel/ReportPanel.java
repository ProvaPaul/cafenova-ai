package com.smartcafe.view.panel;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.exception.AppException;
import com.smartcafe.service.ReportService.*;
import com.smartcafe.view.components.RoundedButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReportPanel extends JPanel {

    public ReportPanel() {
        setBackground(AppConfig.COLOR_BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(AppConfig.FONT_TITLE);
        title.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        JLabel sub = new JLabel("Sales trends, product performance, and customer insights.");
        sub.setFont(AppConfig.FONT_SMALL);
        sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(title); header.add(sub);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConfig.FONT_LABEL);
        tabs.addTab("📅  Daily Sales",    buildDailyTab());
        tabs.addTab("📈  Monthly Sales",  buildMonthlyTab());
        tabs.addTab("📦  Product Sales",  buildProductTab());
        tabs.addTab("👥  Top Customers",  buildCustomerTab());
        add(tabs, BorderLayout.CENTER);
    }

    public void loadData() { /* tabs load on demand */ }

    // ── DAILY SALES ───────────────────────────────────────────────────────────

    private JPanel buildDailyTab() {
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();
        JSpinner fromSpin = new JSpinner(fromModel);
        JSpinner toSpin   = new JSpinner(toModel);
        fromSpin.setEditor(new JSpinner.DateEditor(fromSpin, "yyyy-MM-dd"));
        toSpin.setEditor(new JSpinner.DateEditor(toSpin,   "yyyy-MM-dd"));
        fromSpin.setPreferredSize(new Dimension(130, 32));
        toSpin.setPreferredSize(new Dimension(130, 32));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        toModel.setValue(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
        fromModel.setValue(cal.getTime());

        String[] cols = {"Date", "Orders", "Revenue (₱)", "Avg Order (₱)"};
        DefaultTableModel tModel = emptyModel(cols);
        JTable tbl = styledTable(tModel);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(AppConfig.COLOR_BG);
        chartHolder.setPreferredSize(new Dimension(0, 280));

        RoundedButton loadBtn = new RoundedButton("Load", RoundedButton.Style.PRIMARY);
        loadBtn.addActionListener(e -> {
            LocalDate from = toLD(fromModel.getDate());
            LocalDate to   = toLD(toModel.getDate());
            try {
                List<DailySalesRow> rows = AppContext.reportService().getDailySales(from, to);
                tModel.setRowCount(0);
                DefaultCategoryDataset ds = new DefaultCategoryDataset();
                for (DailySalesRow r : rows) {
                    tModel.addRow(new Object[]{r.date(), r.orderCount(),
                        String.format("%.2f", r.revenue()), String.format("%.2f", r.avgOrder())});
                    ds.addValue(r.revenue(), "Revenue", r.date().toString());
                }
                JFreeChart chart = ChartFactory.createBarChart(
                    "Daily Revenue", "Date", "Revenue (₱)", ds,
                    PlotOrientation.VERTICAL, false, true, false);
                styleChart(chart);
                chartHolder.removeAll();
                chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
                chartHolder.revalidate();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(ReportPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setOpaque(false);
        filter.add(lbl("From:")); filter.add(fromSpin);
        filter.add(lbl("To:"));   filter.add(toSpin);
        filter.add(loadBtn);

        return tabPanel(filter, chartHolder, tbl);
    }

    // ── MONTHLY SALES ─────────────────────────────────────────────────────────

    private JPanel buildMonthlyTab() {
        JSpinner yearSpin = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2100, 1));
        yearSpin.setPreferredSize(new Dimension(90, 32));

        String[] cols = {"Month", "Year", "Orders", "Revenue (₱)"};
        DefaultTableModel tModel = emptyModel(cols);
        JTable tbl = styledTable(tModel);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(AppConfig.COLOR_BG);
        chartHolder.setPreferredSize(new Dimension(0, 280));

        RoundedButton loadBtn = new RoundedButton("Load", RoundedButton.Style.PRIMARY);
        loadBtn.addActionListener(e -> {
            int year = (int) yearSpin.getValue();
            try {
                List<MonthlySalesRow> rows = AppContext.reportService().getMonthlySales(year);
                tModel.setRowCount(0);
                DefaultCategoryDataset ds = new DefaultCategoryDataset();
                for (MonthlySalesRow r : rows) {
                    tModel.addRow(new Object[]{r.monthName(), r.year(), r.orderCount(),
                        String.format("%.2f", r.revenue())});
                    ds.addValue(r.revenue(), "Revenue", r.monthName());
                }
                JFreeChart chart = ChartFactory.createLineChart(
                    "Monthly Revenue — " + year, "Month", "Revenue (₱)", ds,
                    PlotOrientation.VERTICAL, false, true, false);
                styleChart(chart);
                chartHolder.removeAll();
                chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
                chartHolder.revalidate();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(ReportPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setOpaque(false);
        filter.add(lbl("Year:")); filter.add(yearSpin);
        filter.add(loadBtn);

        return tabPanel(filter, chartHolder, tbl);
    }

    // ── PRODUCT SALES ─────────────────────────────────────────────────────────

    private JPanel buildProductTab() {
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();
        JSpinner fromSpin = new JSpinner(fromModel);
        JSpinner toSpin   = new JSpinner(toModel);
        fromSpin.setEditor(new JSpinner.DateEditor(fromSpin, "yyyy-MM-dd"));
        toSpin.setEditor(new JSpinner.DateEditor(toSpin, "yyyy-MM-dd"));
        fromSpin.setPreferredSize(new Dimension(130, 32));
        toSpin.setPreferredSize(new Dimension(130, 32));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        toModel.setValue(cal.getTime()); cal.add(java.util.Calendar.MONTH, -1); fromModel.setValue(cal.getTime());

        String[] cols = {"Product", "Orders", "Qty Sold", "Revenue (₱)"};
        DefaultTableModel tModel = emptyModel(cols);
        JTable tbl = styledTable(tModel);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(AppConfig.COLOR_BG);
        chartHolder.setPreferredSize(new Dimension(0, 280));

        RoundedButton loadBtn = new RoundedButton("Load", RoundedButton.Style.PRIMARY);
        loadBtn.addActionListener(e -> {
            LocalDate from = toLD(fromModel.getDate());
            LocalDate to   = toLD(toModel.getDate());
            try {
                List<ProductSalesRow> rows = AppContext.reportService().getProductSales(from, to);
                tModel.setRowCount(0);
                DefaultPieDataset<String> ds = new DefaultPieDataset<>();
                int limit = 0;
                for (ProductSalesRow r : rows) {
                    tModel.addRow(new Object[]{r.productName(), r.timesOrdered(), r.totalQty(),
                        String.format("%.2f", r.totalRevenue())});
                    if (limit++ < 10) ds.setValue(r.productName(), r.totalRevenue());
                }
                JFreeChart chart = ChartFactory.createPieChart(
                    "Top Products by Revenue", ds, true, true, false);
                styleChart(chart);
                chartHolder.removeAll();
                chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
                chartHolder.revalidate();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(ReportPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setOpaque(false);
        filter.add(lbl("From:")); filter.add(fromSpin);
        filter.add(lbl("To:"));   filter.add(toSpin);
        filter.add(loadBtn);

        return tabPanel(filter, chartHolder, tbl);
    }

    // ── CUSTOMER REPORT ───────────────────────────────────────────────────────

    private JPanel buildCustomerTab() {
        JSpinner limitSpin = new JSpinner(new SpinnerNumberModel(20, 5, 100, 5));
        limitSpin.setPreferredSize(new Dimension(70, 32));

        String[] cols = {"Customer", "Phone", "Visits", "Total Spent (₱)", "Loyalty Points"};
        DefaultTableModel tModel = emptyModel(cols);
        JTable tbl = styledTable(tModel);

        JPanel chartHolder = new JPanel(new BorderLayout());
        chartHolder.setBackground(AppConfig.COLOR_BG);
        chartHolder.setPreferredSize(new Dimension(0, 280));

        RoundedButton loadBtn = new RoundedButton("Load", RoundedButton.Style.PRIMARY);
        loadBtn.addActionListener(e -> {
            int limit = (int) limitSpin.getValue();
            try {
                List<CustomerReportRow> rows = AppContext.reportService().getTopCustomers(limit);
                tModel.setRowCount(0);
                DefaultCategoryDataset ds = new DefaultCategoryDataset();
                int shown = 0;
                for (CustomerReportRow r : rows) {
                    tModel.addRow(new Object[]{r.customerName(), r.phone() != null ? r.phone() : "—",
                        r.visitCount(), String.format("%.2f", r.totalSpent()), r.loyaltyPoints()});
                    if (shown++ < 10) ds.addValue(r.totalSpent(), "Spent", r.customerName());
                }
                JFreeChart chart = ChartFactory.createBarChart(
                    "Top Customers by Spending", "Customer", "Total Spent (₱)", ds,
                    PlotOrientation.VERTICAL, false, true, false);
                styleChart(chart);
                chartHolder.removeAll();
                chartHolder.add(new ChartPanel(chart), BorderLayout.CENTER);
                chartHolder.revalidate();
            } catch (AppException ex) {
                JOptionPane.showMessageDialog(ReportPanel.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setOpaque(false);
        filter.add(lbl("Top N:")); filter.add(limitSpin);
        filter.add(loadBtn);

        return tabPanel(filter, chartHolder, tbl);
    }

    // ── SHARED HELPERS ────────────────────────────────────────────────────────

    private static JPanel tabPanel(JPanel filter, JPanel chartHolder, JTable tbl) {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            chartHolder, new JScrollPane(tbl));
        split.setDividerLocation(280);
        split.setResizeWeight(0.5);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(AppConfig.COLOR_BG);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(filter, BorderLayout.NORTH);
        p.add(split,  BorderLayout.CENTER);
        return p;
    }

    private static DefaultTableModel emptyModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(AppConfig.FONT_BODY);
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(AppConfig.COLOR_BORDER);
        t.getTableHeader().setFont(AppConfig.FONT_LABEL);
        return t;
    }

    private static void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(AppConfig.COLOR_SURFACE);
        chart.getTitle().setPaint(AppConfig.COLOR_TEXT_PRIMARY);
        if (chart.getPlot() != null) {
            chart.getPlot().setBackgroundPaint(AppConfig.COLOR_SURFACE);
            chart.getPlot().setOutlinePaint(AppConfig.COLOR_BORDER);
        }
    }

    private static JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppConfig.FONT_SMALL);
        l.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        return l;
    }

    private static LocalDate toLD(java.util.Date d) {
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
