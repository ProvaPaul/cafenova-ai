package com.smartcafe.dao;

import com.smartcafe.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierDao {
    List<Supplier>    findAll();
    List<Supplier>    findAllActive();
    Optional<Supplier> findById(int id);
    boolean           existsByName(String name);
    boolean           existsByNameExcludingId(String name, int excludeId);
    Supplier          save(Supplier supplier);
    void              update(Supplier supplier);
    void              delete(int id);   // soft delete
    long              count();
}
