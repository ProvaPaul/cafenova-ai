package com.smartcafe.service.impl;

import com.smartcafe.dao.AttendanceDao;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Attendance;
import com.smartcafe.service.AttendanceService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceDao dao;

    public AttendanceServiceImpl(AttendanceDao dao) { this.dao = dao; }

    @Override public List<Attendance> getByDate(LocalDate date) throws AppException {
        try { return dao.findByDate(date); }
        catch (SQLException e) { throw new AppException("Failed to load attendance: " + e.getMessage(), e); }
    }

    @Override public List<Attendance> getByEmployeeAndPeriod(int empId, LocalDate from, LocalDate to) throws AppException {
        try { return dao.findByEmployeeAndPeriod(empId, from, to); }
        catch (SQLException e) { throw new AppException("Failed to load attendance: " + e.getMessage(), e); }
    }

    @Override public Optional<Attendance> getByEmployeeAndDate(int empId, LocalDate date) throws AppException {
        try { return dao.findByEmployeeAndDate(empId, date); }
        catch (SQLException e) { throw new AppException("Failed to load attendance: " + e.getMessage(), e); }
    }

    @Override public void markAttendance(Attendance a) throws AppException {
        if (a.getDate() == null) throw new AppException("Attendance date is required.");
        if (a.getEmployeeId() <= 0) throw new AppException("Employee is required.");
        try {
            Optional<Attendance> existing = dao.findByEmployeeAndDate(a.getEmployeeId(), a.getDate());
            if (existing.isPresent()) {
                a.setId(existing.get().getId());
                dao.update(a);
            } else {
                dao.save(a);
            }
        } catch (SQLException e) {
            throw new AppException("Failed to mark attendance: " + e.getMessage(), e);
        }
    }
}
