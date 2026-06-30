package com.smartcafe.service.impl;

import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.dao.CustomerDao;
import com.smartcafe.exception.AppException;
import com.smartcafe.model.Customer;
import com.smartcafe.model.Order;
import com.smartcafe.service.CustomerService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao dao;

    public CustomerServiceImpl(CustomerDao dao) { this.dao = dao; }

    @Override
    public List<Customer> findAll() throws AppException {
        try { return dao.findAll(); }
        catch (SQLException e) { throw new AppException("Failed to load customers: " + e.getMessage(), e); }
    }

    @Override
    public Optional<Customer> findById(int id) throws AppException {
        try { return dao.findById(id); }
        catch (SQLException e) { throw new AppException("Failed to find customer: " + e.getMessage(), e); }
    }

    @Override
    public List<Customer> search(String query) throws AppException {
        if (query == null || query.isBlank()) return findAll();
        try { return dao.search(query.trim()); }
        catch (SQLException e) { throw new AppException("Search failed: " + e.getMessage(), e); }
    }

    @Override
    public Customer save(Customer customer) throws AppException {
        validate(customer);
        try { return dao.save(customer); }
        catch (SQLException e) { throw new AppException("Failed to save customer: " + e.getMessage(), e); }
    }

    @Override
    public void update(Customer customer) throws AppException {
        validate(customer);
        try { dao.update(customer); }
        catch (SQLException e) { throw new AppException("Failed to update customer: " + e.getMessage(), e); }
    }

    @Override
    public void delete(int id) throws AppException {
        try { dao.delete(id); }
        catch (SQLException e) { throw new AppException("Failed to delete customer: " + e.getMessage(), e); }
    }

    @Override
    public void addLoyaltyPoints(int id, int points) throws AppException {
        try { dao.addLoyaltyPoints(id, points); }
        catch (SQLException e) { throw new AppException("Failed to update loyalty points: " + e.getMessage(), e); }
    }

    @Override
    public void recordPurchase(int customerId, double amount) throws AppException {
        try { dao.recordPurchase(customerId, amount); }
        catch (SQLException e) { throw new AppException("Failed to record purchase: " + e.getMessage(), e); }
    }

    @Override
    public List<Order> getPurchaseHistory(int customerId) throws AppException {
        String sql = "SELECT o.*, u.full_name AS cashier_name, ct.table_number " +
                     "FROM orders o " +
                     "LEFT JOIN users u ON o.cashier_id = u.id " +
                     "LEFT JOIN cafe_tables ct ON o.table_id = ct.id " +
                     "WHERE o.customer_id = ? ORDER BY o.created_at DESC LIMIT 100";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            List<Order> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setOrderNumber(rs.getString("order_number"));
                    o.setOrderType(rs.getString("order_type"));
                    o.setStatus(rs.getString("status"));
                    o.setSubtotal(rs.getDouble("subtotal"));
                    o.setTax(rs.getDouble("tax"));
                    o.setDiscount(rs.getDouble("discount"));
                    o.setTotal(rs.getDouble("total"));
                    o.setCustomerName(rs.getString("customer_name"));
                    o.setCashierName(rs.getString("cashier_name"));
                    o.setTableNumber(rs.getString("table_number"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
                    list.add(o);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new AppException("Failed to load purchase history: " + e.getMessage(), e);
        }
    }

    private void validate(Customer c) throws AppException {
        if (c.getFullName() == null || c.getFullName().isBlank())
            throw new AppException("Customer name is required.");
    }
}
