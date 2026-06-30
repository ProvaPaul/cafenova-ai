package com.smartcafe.service.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.exception.AppException;
import com.smartcafe.service.ReportService;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportServiceImpl implements ReportService {

    @Override
    public List<DailySalesRow> getDailySales(LocalDate from, LocalDate to) throws AppException {
        String sql = """
            SELECT DATE(created_at) AS d, COUNT(*) AS cnt, SUM(total) AS rev
            FROM orders
            WHERE DATE(created_at) BETWEEN ? AND ? AND status NOT IN ('CANCELLED')
            GROUP BY DATE(created_at)
            ORDER BY d
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            List<DailySalesRow> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cnt  = rs.getInt("cnt");
                    double rev = rs.getDouble("rev");
                    list.add(new DailySalesRow(
                        rs.getDate("d").toLocalDate(), cnt, rev, cnt > 0 ? rev / cnt : 0));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Failed to load daily sales: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MonthlySalesRow> getMonthlySales(int year) throws AppException {
        String sql = """
            SELECT YEAR(created_at) AS y, MONTH(created_at) AS m, COUNT(*) AS cnt, SUM(total) AS rev
            FROM orders
            WHERE YEAR(created_at) = ? AND status NOT IN ('CANCELLED')
            GROUP BY YEAR(created_at), MONTH(created_at)
            ORDER BY m
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            List<MonthlySalesRow> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("m");
                    String mName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    list.add(new MonthlySalesRow(
                        rs.getInt("y"), m, mName, rs.getInt("cnt"), rs.getDouble("rev")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Failed to load monthly sales: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductSalesRow> getProductSales(LocalDate from, LocalDate to) throws AppException {
        String sql = """
            SELECT mi.name, COUNT(DISTINCT oi.order_id) AS times_ordered,
                   SUM(oi.quantity) AS total_qty, SUM(oi.subtotal) AS total_revenue
            FROM order_items oi
            JOIN menu_items mi ON oi.menu_item_id = mi.id
            JOIN orders o      ON oi.order_id = o.id
            WHERE DATE(o.created_at) BETWEEN ? AND ? AND o.status NOT IN ('CANCELLED')
            GROUP BY mi.id, mi.name
            ORDER BY total_revenue DESC
            LIMIT 50
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            List<ProductSalesRow> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductSalesRow(
                        rs.getString("name"),
                        rs.getInt("times_ordered"),
                        rs.getInt("total_qty"),
                        rs.getDouble("total_revenue")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Failed to load product sales: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CustomerReportRow> getTopCustomers(int limit) throws AppException {
        String sql = """
            SELECT full_name, phone, visit_count, total_spent, loyalty_points
            FROM customers
            WHERE is_active = TRUE
            ORDER BY total_spent DESC
            LIMIT ?
            """;
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            List<CustomerReportRow> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerReportRow(
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getInt("visit_count"),
                        rs.getDouble("total_spent"),
                        rs.getInt("loyalty_points")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Failed to load customer report: " + e.getMessage(), e);
        }
    }
}
