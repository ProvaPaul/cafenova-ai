package com.smartcafe.dao;

import com.smartcafe.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    List<Customer> findAll() throws SQLException;
    Optional<Customer> findById(int id) throws SQLException;
    List<Customer> search(String query) throws SQLException;
    Customer save(Customer customer) throws SQLException;
    void update(Customer customer) throws SQLException;
    void delete(int id) throws SQLException;
    void addLoyaltyPoints(int id, int points) throws SQLException;
    void recordPurchase(int id, double amount) throws SQLException;
}
