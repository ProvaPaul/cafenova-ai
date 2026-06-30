package com.smartcafe.service;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> findAll() throws AppException;
    List<Employee> findAllActive() throws AppException;
    Optional<Employee> findById(int id) throws AppException;
    Employee save(Employee employee) throws AppException;
    void update(Employee employee) throws AppException;
    void delete(int id) throws AppException;
}
