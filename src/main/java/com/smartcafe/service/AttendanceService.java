package com.smartcafe.service;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    List<Attendance> getByDate(LocalDate date) throws AppException;
    List<Attendance> getByEmployeeAndPeriod(int employeeId, LocalDate from, LocalDate to) throws AppException;
    Optional<Attendance> getByEmployeeAndDate(int employeeId, LocalDate date) throws AppException;
    void markAttendance(Attendance attendance) throws AppException;
}
