package com.smartcafe.dao;

import com.smartcafe.model.SalaryPayment;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SalaryPaymentDao {
    List<SalaryPayment> findByPeriod(int month, int year) throws SQLException;
    List<SalaryPayment> findByEmployee(int employeeId) throws SQLException;
    Optional<SalaryPayment> findByEmployeeAndPeriod(int employeeId, int month, int year) throws SQLException;
    SalaryPayment save(SalaryPayment payment) throws SQLException;
    void update(SalaryPayment payment) throws SQLException;
}
