package com.smartcafe.service;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Employee;
import com.smartcafe.model.SalaryPayment;

import java.util.List;

public interface SalaryService {
    List<SalaryPayment> getByPeriod(int month, int year) throws AppException;
    List<SalaryPayment> getByEmployee(int employeeId) throws AppException;
    SalaryPayment generate(Employee employee, int month, int year) throws AppException;
    void generateAll(List<Employee> employees, int month, int year) throws AppException;
    void markAsPaid(int id) throws AppException;
    void update(SalaryPayment payment) throws AppException;
}
