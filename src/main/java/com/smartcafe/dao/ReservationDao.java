package com.smartcafe.dao;

import com.smartcafe.model.Reservation;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    List<Reservation> findAll() throws SQLException;
    List<Reservation> findByDateRange(LocalDate from, LocalDate to, String status) throws SQLException;
    Optional<Reservation> findById(int id) throws SQLException;
    Reservation save(Reservation reservation) throws SQLException;
    void update(Reservation reservation) throws SQLException;
    void updateStatus(int id, String status) throws SQLException;
}
