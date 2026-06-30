package com.smartcafe.service.impl;

import com.smartcafe.dao.ReservationDao;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Reservation;
import com.smartcafe.service.ReservationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao dao;

    public ReservationServiceImpl(ReservationDao dao) { this.dao = dao; }

    @Override public List<Reservation> findAll() throws AppException {
        try { return dao.findAll(); }
        catch (SQLException e) { throw new AppException("Failed to load reservations: " + e.getMessage(), e); }
    }

    @Override public List<Reservation> findByDateRange(LocalDate from, LocalDate to, String status) throws AppException {
        try { return dao.findByDateRange(from, to, status); }
        catch (SQLException e) { throw new AppException("Failed to load reservations: " + e.getMessage(), e); }
    }

    @Override public Optional<Reservation> findById(int id) throws AppException {
        try { return dao.findById(id); }
        catch (SQLException e) { throw new AppException("Failed to find reservation: " + e.getMessage(), e); }
    }

    @Override public Reservation save(Reservation r) throws AppException {
        validate(r);
        try { return dao.save(r); }
        catch (SQLException e) { throw new AppException("Failed to save reservation: " + e.getMessage(), e); }
    }

    @Override public void update(Reservation r) throws AppException {
        validate(r);
        try { dao.update(r); }
        catch (SQLException e) { throw new AppException("Failed to update reservation: " + e.getMessage(), e); }
    }

    @Override public void updateStatus(int id, String status) throws AppException {
        try { dao.updateStatus(id, status); }
        catch (SQLException e) { throw new AppException("Failed to update status: " + e.getMessage(), e); }
    }

    private void validate(Reservation r) throws AppException {
        if (r.getCustomerName() == null || r.getCustomerName().isBlank())
            throw new AppException("Customer name is required.");
        if (r.getReservationDate() == null)
            throw new AppException("Reservation date is required.");
        if (r.getReservationTime() == null)
            throw new AppException("Reservation time is required.");
        if (r.getPartySize() < 1)
            throw new AppException("Party size must be at least 1.");
    }
}
