package com.smartcafe.dao.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.ReservationDao;
import com.smartcafe.model.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDaoImpl implements ReservationDao {

    private static final String BASE =
        "SELECT r.*, ct.table_number, c.full_name AS customer_full_name " +
        "FROM reservations r " +
        "LEFT JOIN cafe_tables ct ON r.table_id = ct.id " +
        "LEFT JOIN customers  c  ON r.customer_id = c.id ";

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = BASE + "ORDER BY r.reservation_date DESC, r.reservation_time DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Reservation> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate from, LocalDate to, String status) throws SQLException {
        boolean allStatus = status == null || "ALL".equals(status);
        String sql = BASE + "WHERE r.reservation_date BETWEEN ? AND ?" +
                     (allStatus ? "" : " AND r.status = ?") +
                     " ORDER BY r.reservation_date, r.reservation_time";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            if (!allStatus) ps.setString(3, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    @Override
    public Optional<Reservation> findById(int id) throws SQLException {
        String sql = BASE + "WHERE r.id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Reservation save(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations (table_id, customer_id, customer_name, party_size, reservation_date, reservation_time, status, notes) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, r);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
            return r;
        }
    }

    @Override
    public void update(Reservation r) throws SQLException {
        String sql = "UPDATE reservations SET table_id=?, customer_id=?, customer_name=?, party_size=?, reservation_date=?, reservation_time=?, status=?, notes=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, r);
            ps.setInt(9, r.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Reservation r) throws SQLException {
        if (r.getTableId() != null) ps.setInt(1, r.getTableId());
        else ps.setNull(1, Types.INTEGER);
        if (r.getCustomerId() != null) ps.setInt(2, r.getCustomerId());
        else ps.setNull(2, Types.INTEGER);
        ps.setString(3, r.getCustomerName());
        ps.setInt(4, r.getPartySize());
        ps.setDate(5, Date.valueOf(r.getReservationDate()));
        ps.setTime(6, Time.valueOf(r.getReservationTime()));
        ps.setString(7, r.getStatus());
        ps.setString(8, r.getNotes());
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        int tid = rs.getInt("table_id"); if (!rs.wasNull()) r.setTableId(tid);
        int cid = rs.getInt("customer_id"); if (!rs.wasNull()) r.setCustomerId(cid);
        r.setCustomerName(rs.getString("customer_name"));
        r.setPartySize(rs.getInt("party_size"));
        Date rd = rs.getDate("reservation_date"); if (rd != null) r.setReservationDate(rd.toLocalDate());
        Time rt = rs.getTime("reservation_time"); if (rt != null) r.setReservationTime(rt.toLocalTime());
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at"); if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        try { r.setTableNumber(rs.getString("table_number")); } catch (SQLException ignored) {}
        try { r.setCustomerFullName(rs.getString("customer_full_name")); } catch (SQLException ignored) {}
        return r;
    }
}
