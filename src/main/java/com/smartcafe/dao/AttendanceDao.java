package com.smartcafe.dao;

import com.smartcafe.model.Attendance;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceDao {
    List<Attendance> findByDate(LocalDate date) throws SQLException;
    List<Attendance> findByEmployeeAndPeriod(int employeeId, LocalDate from, LocalDate to) throws SQLException;
    Optional<Attendance> findByEmployeeAndDate(int employeeId, LocalDate date) throws SQLException;
    Attendance save(Attendance attendance) throws SQLException;
    void update(Attendance attendance) throws SQLException;
}
