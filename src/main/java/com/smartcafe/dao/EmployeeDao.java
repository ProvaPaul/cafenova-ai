package com.smartcafe.dao;

import com.smartcafe.model.Employee;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EmployeeDao {
    List<Employee> findAll() throws SQLException;
    List<Employee> findAllActive() throws SQLException;
    Optional<Employee> findById(int id) throws SQLException;
    Employee save(Employee employee) throws SQLException;
    void update(Employee employee) throws SQLException;
    void delete(int id) throws SQLException;
}
