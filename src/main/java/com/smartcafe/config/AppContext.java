package com.smartcafe.config;

import com.smartcafe.service.CategoryService;
import com.smartcafe.service.DashboardService;
import com.smartcafe.service.ProductService;

import java.util.Objects;

/**
 * Lightweight static service-locator for a single-process desktop app.
 *
 * Why not pass services through every constructor?
 * At this scale (one JVM, one user) the constructor chain would propagate
 * CategoryService / ProductService / DashboardService through MainFrame →
 * AdminDashboard → CategoryPanel → CategoryFormDialog, creating deep coupling.
 * A single static registry avoids the chain while still being deterministic
 * (initialized once in Main before the EDT starts).
 *
 * Thread-safety: volatile fields + the single invocation in Main.java guarantee
 * correct publication across threads (EDT + SwingWorker).
 */
public final class AppContext {

    private static volatile CategoryService  categoryService;
    private static volatile ProductService   productService;
    private static volatile DashboardService dashboardService;

    private AppContext() {}

    public static void initialize(CategoryService cs, ProductService ps, DashboardService ds) {
        categoryService  = cs;
        productService   = ps;
        dashboardService = ds;
    }

    public static CategoryService categoryService() {
        return Objects.requireNonNull(categoryService, "AppContext.initialize() not called");
    }

    public static ProductService productService() {
        return Objects.requireNonNull(productService, "AppContext.initialize() not called");
    }

    public static DashboardService dashboardService() {
        return Objects.requireNonNull(dashboardService, "AppContext.initialize() not called");
    }
}
