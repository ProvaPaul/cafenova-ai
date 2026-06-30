package com.smartcafe.service.impl;

import com.smartcafe.dao.EmployeeDao;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Employee;
import com.smartcafe.service.EmployeeService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDao dao;

    public EmployeeServiceImpl(EmployeeDao dao) { this.dao = dao; }

    @Override public List<Employee> findAll() throws AppException {
        try { return dao.findAll(); }
        catch (SQLException e) { throw new AppException("Failed to load employees: " + e.getMessage(), e); }
    }

    @Override public List<Employee> findAllActive() throws AppException {
        try { return dao.findAllActive(); }
        catch (SQLException e) { throw new AppException("Failed to load employees: " + e.getMessage(), e); }
    }

    @Override public Optional<Employee> findById(int id) throws AppException {
        try { return dao.findById(id); }
        catch (SQLException e) { throw new AppException("Failed to find employee: " + e.getMessage(), e); }
    }

    @Override public Employee save(Employee emp) throws AppException {
        validate(emp);
        try { return dao.save(emp); }
        catch (SQLException e) { throw new AppException("Failed to save employee: " + e.getMessage(), e); }
    }

    @Override public void update(Employee emp) throws AppException {
        validate(emp);
        try { dao.update(emp); }
        catch (SQLException e) { throw new AppException("Failed to update employee: " + e.getMessage(), e); }
    }

    @Override public void delete(int id) throws AppException {
        try { dao.delete(id); }
        catch (SQLException e) { throw new AppException("Failed to delete employee: " + e.getMessage(), e); }
    }

    private void validate(Employee e) throws AppException {
        if (e.getFullName() == null || e.getFullName().isBlank())
            throw new AppException("Employee name is required.");
        if (e.getBaseSalary() < 0)
            throw new AppException("Base salary cannot be negative.");
    }
}
