package com.smartcafe.service.impl;

import com.smartcafe.dao.SalaryPaymentDao;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Employee;
import com.smartcafe.model.SalaryPayment;
import com.smartcafe.service.SalaryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SalaryServiceImpl implements SalaryService {

    private final SalaryPaymentDao dao;

    public SalaryServiceImpl(SalaryPaymentDao dao) { this.dao = dao; }

    @Override public List<SalaryPayment> getByPeriod(int month, int year) throws AppException {
        try { return dao.findByPeriod(month, year); }
        catch (SQLException e) { throw new AppException("Failed to load payroll: " + e.getMessage(), e); }
    }

    @Override public List<SalaryPayment> getByEmployee(int empId) throws AppException {
        try { return dao.findByEmployee(empId); }
        catch (SQLException e) { throw new AppException("Failed to load salary history: " + e.getMessage(), e); }
    }

    @Override public SalaryPayment generate(Employee emp, int month, int year) throws AppException {
        try {
            Optional<SalaryPayment> existing = dao.findByEmployeeAndPeriod(emp.getId(), month, year);
            if (existing.isPresent()) return existing.get();
            SalaryPayment p = new SalaryPayment();
            p.setEmployeeId(emp.getId());
            p.setEmployeeName(emp.getFullName());
            p.setPeriodMonth(month);
            p.setPeriodYear(year);
            p.setBaseSalary(emp.getBaseSalary());
            p.setNetSalary(emp.getBaseSalary());
            return dao.save(p);
        } catch (SQLException e) {
            throw new AppException("Failed to generate salary: " + e.getMessage(), e);
        }
    }

    @Override public void generateAll(List<Employee> employees, int month, int year) throws AppException {
        for (Employee e : employees) generate(e, month, year);
    }

    @Override public void markAsPaid(int id) throws AppException {
        try {
            updateStatusPaidAt(id);
        } catch (SQLException e) {
            throw new AppException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    @Override public void update(SalaryPayment payment) throws AppException {
        try { dao.update(payment); }
        catch (SQLException e) { throw new AppException("Failed to update salary: " + e.getMessage(), e); }
    }

    private void updateStatusPaidAt(int id) throws SQLException {
        String sql = "UPDATE salary_payments SET status='PAID', paid_at=NOW() WHERE id=?";
        try (var c = com.smartcafe.config.DatabaseConfig.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
