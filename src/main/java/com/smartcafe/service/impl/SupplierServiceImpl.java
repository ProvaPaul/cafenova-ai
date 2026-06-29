package com.smartcafe.service.impl;

import com.smartcafe.dao.SupplierDao;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.Supplier;
import com.smartcafe.service.SupplierService;

import java.util.List;
import java.util.Optional;

public class SupplierServiceImpl implements SupplierService {

    private final SupplierDao supplierDao;

    public SupplierServiceImpl(SupplierDao supplierDao) {
        this.supplierDao = supplierDao;
    }

    @Override
    public List<Supplier> findAll() { return supplierDao.findAll(); }

    @Override
    public List<Supplier> findAllActive() { return supplierDao.findAllActive(); }

    @Override
    public Optional<Supplier> findById(int id) { return supplierDao.findById(id); }

    @Override
    public Supplier create(String name, String contact, String phone,
                           String email, String address) {
        name = requireName(name);
        if (supplierDao.existsByName(name))
            throw new ValidationException("A supplier named '" + name + "' already exists");

        Supplier s = new Supplier(name);
        s.setContact(contact != null ? contact.trim() : null);
        s.setPhone(phone     != null ? phone.trim()   : null);
        s.setEmail(email     != null ? email.trim()   : null);
        s.setAddress(address != null ? address.trim() : null);
        return supplierDao.save(s);
    }

    @Override
    public Supplier update(Supplier s) {
        requireName(s.getName());
        if (supplierDao.existsByNameExcludingId(s.getName().trim(), s.getId()))
            throw new ValidationException("A supplier named '" + s.getName() + "' already exists");
        s.setName(s.getName().trim());
        supplierDao.update(s);
        return s;
    }

    @Override
    public void delete(int id) {
        supplierDao.findById(id)
                .orElseThrow(() -> new ValidationException("Supplier not found"));
        supplierDao.delete(id);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Supplier name is required");
        return name.trim();
    }
}
