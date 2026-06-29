package com.smartcafe.service;

import com.smartcafe.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierService {
    List<Supplier>    findAll();
    List<Supplier>    findAllActive();
    Optional<Supplier> findById(int id);
    Supplier          create(String name, String contact, String phone, String email, String address);
    Supplier          update(Supplier supplier);
    void              delete(int id);
}
