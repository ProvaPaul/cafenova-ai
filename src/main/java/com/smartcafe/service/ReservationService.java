package com.smartcafe.service;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationService {
    List<Reservation> findAll() throws AppException;
    List<Reservation> findByDateRange(LocalDate from, LocalDate to, String status) throws AppException;
    Optional<Reservation> findById(int id) throws AppException;
    Reservation save(Reservation reservation) throws AppException;
    void update(Reservation reservation) throws AppException;
    void updateStatus(int id, String status) throws AppException;
}
