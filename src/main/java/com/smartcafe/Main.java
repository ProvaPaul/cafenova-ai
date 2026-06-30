package com.smartcafe;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.AppContext;
import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.dao.impl.*;
import com.smartcafe.service.impl.*;
import com.smartcafe.view.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Startup sequence:
 *  1. Apply FlatLaf theme (must precede any Swing component creation)
 *  2. Validate database connectivity (fast-fail with a helpful dialog)
 *  3. Wire the dependency graph: DAO → Service → AppContext
 *  4. Show the main window on the Event Dispatch Thread
 *  5. Register a shutdown hook to close the HikariCP pool cleanly
 */
public class Main {

    public static void main(String[] args) {

        // ① Theme — must be first, before any Swing component is created
        AppConfig.setupTheme();

        // ② Database check
        if (!DatabaseConfig.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Cannot connect to the database.</b><br/><br/>"
                    + "Please make sure MySQL is running and that<br/>"
                    + "<code>src/main/resources/config/database.properties</code><br/>"
                    + "has the correct credentials.<br/><br/>"
                    + "Then re-launch the application.</html>",
                    "Database Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ③ Dependency wiring — Step 1 (Auth)
        UserDaoImpl userDao = new UserDaoImpl();
        AuthServiceImpl authService = new AuthServiceImpl(userDao);

        // Step 2 DAOs + Services (Categories, Products, Dashboard)
        CategoryDaoImpl categoryDao = new CategoryDaoImpl();
        ProductDaoImpl  productDao  = new ProductDaoImpl();

        CategoryServiceImpl  categoryService  = new CategoryServiceImpl(categoryDao, productDao);
        ProductServiceImpl   productService   = new ProductServiceImpl(productDao, categoryDao);
        DashboardServiceImpl dashboardService = new DashboardServiceImpl();

        AppContext.initialize(categoryService, productService, dashboardService);

        // Step 3 DAOs + Services (Inventory, POS, Billing)
        SupplierDaoImpl      supplierDao      = new SupplierDaoImpl();
        InventoryItemDaoImpl inventoryDao     = new InventoryItemDaoImpl();
        CafeTableDaoImpl     cafeTableDao     = new CafeTableDaoImpl();
        OrderDaoImpl         orderDao         = new OrderDaoImpl(cafeTableDao);
        PaymentDaoImpl       paymentDao       = new PaymentDaoImpl();

        SupplierServiceImpl  supplierService  = new SupplierServiceImpl(supplierDao);
        InventoryServiceImpl inventoryService = new InventoryServiceImpl(inventoryDao);
        OrderServiceImpl     orderService     = new OrderServiceImpl(orderDao);
        BillingServiceImpl   billingService   = new BillingServiceImpl(orderDao, paymentDao, cafeTableDao);

        AppContext.initializeStep3(supplierService, inventoryService, orderService, billingService);

        // Step 4 DAOs + Services (Customer, Employee, Attendance, Salary, Reservation, Report)
        com.smartcafe.dao.impl.CustomerDaoImpl     customerDao     = new com.smartcafe.dao.impl.CustomerDaoImpl();
        com.smartcafe.dao.impl.EmployeeDaoImpl     employeeDao     = new com.smartcafe.dao.impl.EmployeeDaoImpl();
        com.smartcafe.dao.impl.AttendanceDaoImpl   attendanceDao   = new com.smartcafe.dao.impl.AttendanceDaoImpl();
        com.smartcafe.dao.impl.SalaryPaymentDaoImpl salaryDao      = new com.smartcafe.dao.impl.SalaryPaymentDaoImpl();
        com.smartcafe.dao.impl.ReservationDaoImpl  reservationDao  = new com.smartcafe.dao.impl.ReservationDaoImpl();

        com.smartcafe.service.impl.CustomerServiceImpl    customerService4   = new com.smartcafe.service.impl.CustomerServiceImpl(customerDao);
        com.smartcafe.service.impl.EmployeeServiceImpl    employeeService4   = new com.smartcafe.service.impl.EmployeeServiceImpl(employeeDao);
        com.smartcafe.service.impl.AttendanceServiceImpl  attendanceService4 = new com.smartcafe.service.impl.AttendanceServiceImpl(attendanceDao);
        com.smartcafe.service.impl.SalaryServiceImpl      salaryService4     = new com.smartcafe.service.impl.SalaryServiceImpl(salaryDao);
        com.smartcafe.service.impl.ReservationServiceImpl reservationService4= new com.smartcafe.service.impl.ReservationServiceImpl(reservationDao);
        com.smartcafe.service.impl.ReportServiceImpl      reportService4     = new com.smartcafe.service.impl.ReportServiceImpl();

        AppContext.initializeStep4(customerService4, employeeService4, attendanceService4,
                                   salaryService4, reservationService4, reportService4);

        // ④ Build and show UI on the EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            AuthController controller = new AuthController(authService, frame);
            frame.setController(controller);
            frame.init();
        });

        // ⑤ Clean pool shutdown on JVM exit
        Runtime.getRuntime().addShutdownHook(
                new Thread(DatabaseConfig::shutdown, "hikari-shutdown"));
    }
}
