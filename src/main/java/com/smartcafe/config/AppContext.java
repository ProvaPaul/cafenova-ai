package com.smartcafe.config;

import com.smartcafe.ai.AiRecommendationService;
import com.smartcafe.ai.AiRecommendationServiceStub;
import com.smartcafe.service.*;

import java.util.Objects;

public final class AppContext {

    // Step 1-2 services
    private static volatile CategoryService  categoryService;
    private static volatile ProductService   productService;
    private static volatile DashboardService dashboardService;

    // Step 3 services
    private static volatile SupplierService  supplierService;
    private static volatile InventoryService inventoryService;
    private static volatile OrderService     orderService;
    private static volatile BillingService   billingService;

    // Step 4 services
    private static volatile CustomerService    customerService;
    private static volatile EmployeeService    employeeService;
    private static volatile AttendanceService  attendanceService;
    private static volatile SalaryService      salaryService;
    private static volatile ReservationService reservationService;
    private static volatile ReportService      reportService;

    // Step 5: AI (stub by default; swap for AiRecommendationServiceImpl when FastAPI is live)
    private static volatile AiRecommendationService aiService;

    private AppContext() {}

    public static void initialize(CategoryService cs, ProductService ps, DashboardService ds) {
        categoryService  = cs;
        productService   = ps;
        dashboardService = ds;
    }

    public static void initializeStep3(SupplierService ss, InventoryService is,
                                       OrderService os, BillingService bs) {
        supplierService  = ss;
        inventoryService = is;
        orderService     = os;
        billingService   = bs;
    }

    public static void initializeStep4(CustomerService cs, EmployeeService es,
                                       AttendanceService as, SalaryService ss,
                                       ReservationService rs, ReportService rps) {
        customerService    = cs;
        employeeService    = es;
        attendanceService  = as;
        salaryService      = ss;
        reservationService = rs;
        reportService      = rps;
    }

    // Step 1-2 accessors
    public static CategoryService  categoryService()  { return require(categoryService,  "categoryService"); }
    public static ProductService   productService()   { return require(productService,   "productService"); }
    public static DashboardService dashboardService() { return require(dashboardService, "dashboardService"); }

    // Step 3 accessors
    public static SupplierService  supplierService()  { return require(supplierService,  "supplierService"); }
    public static InventoryService inventoryService() { return require(inventoryService, "inventoryService"); }
    public static OrderService     orderService()     { return require(orderService,     "orderService"); }
    public static BillingService   billingService()   { return require(billingService,   "billingService"); }

    // Step 4 accessors
    public static CustomerService    customerService()    { return require(customerService,    "customerService"); }
    public static EmployeeService    employeeService()    { return require(employeeService,    "employeeService"); }
    public static AttendanceService  attendanceService()  { return require(attendanceService,  "attendanceService"); }
    public static SalaryService      salaryService()      { return require(salaryService,      "salaryService"); }
    public static ReservationService reservationService() { return require(reservationService, "reservationService"); }
    public static ReportService      reportService()      { return require(reportService,      "reportService"); }

    // Step 5 init + accessor (never null — falls back to stub)
    public static void initializeStep5(AiRecommendationService ai) {
        aiService = ai;
    }

    public static AiRecommendationService aiService() {
        AiRecommendationService svc = aiService;
        return svc != null ? svc : new AiRecommendationServiceStub();
    }

    private static <T> T require(T svc, String name) {
        return Objects.requireNonNull(svc, "AppContext: " + name + " not initialized");
    }
}
