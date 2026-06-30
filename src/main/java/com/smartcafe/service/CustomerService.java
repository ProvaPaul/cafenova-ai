package com.smartcafe.service;

import com.smartcafe.exception.AppException;
import com.smartcafe.model.Customer;
import com.smartcafe.model.Order;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<Customer> findAll() throws AppException;
    Optional<Customer> findById(int id) throws AppException;
    List<Customer> search(String query) throws AppException;
    Customer save(Customer customer) throws AppException;
    void update(Customer customer) throws AppException;
    void delete(int id) throws AppException;
    void addLoyaltyPoints(int id, int points) throws AppException;
    void recordPurchase(int customerId, double amount) throws AppException;
    List<Order> getPurchaseHistory(int customerId) throws AppException;
}
