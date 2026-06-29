package com.smartcafe;

import com.smartcafe.config.AppConfig;
import com.smartcafe.config.DatabaseConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.dao.impl.UserDaoImpl;
import com.smartcafe.service.impl.AuthServiceImpl;
import com.smartcafe.view.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Startup sequence:
 *  1. Apply FlatLaf theme (must precede any Swing component creation)
 *  2. Validate database connectivity (fast fail with a helpful message)
 *  3. Wire the dependency graph: DAO → Service → Controller
 *  4. Show the main window on the Event Dispatch Thread
 *  5. Register shutdown hook to close the HikariCP pool cleanly
 */
public class Main {

    public static void main(String[] args) {
        // ① Theme — call before the first Swing component is touched
        AppConfig.setupTheme();

        // ② Database check — show a friendly dialog on failure so the user
        //   knows to start MySQL and check database.properties
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

        // ③ Dependency wiring (manual DI — no framework needed at this scale)
        //    DAO is stateless so one instance suffices for the whole app.
        UserDaoImpl     userDao     = new UserDaoImpl();
        AuthServiceImpl authService = new AuthServiceImpl(userDao);

        // ④ Build and show UI on the EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();

            // Controller needs the frame reference to drive navigation
            AuthController controller = new AuthController(authService, frame);
            frame.setController(controller);

            frame.init();   // constructs all panels and shows the login screen
        });

        // ⑤ Clean pool shutdown when the JVM exits (window closed, Ctrl-C, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConfig::shutdown,
                "hikari-shutdown"));
    }
}
