package com.smartcafe.dao;

import com.smartcafe.model.CafeTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CafeTableDao {
    List<CafeTable>    findAll();
    List<CafeTable>    findAvailable();
    Optional<CafeTable> findById(int id);
    /** Update status inside an existing transaction connection. */
    void               updateStatus(Connection conn, int tableId, String status) throws SQLException;
    /** Update status with its own connection (non-transactional). */
    void               updateStatus(int tableId, String status);
}
