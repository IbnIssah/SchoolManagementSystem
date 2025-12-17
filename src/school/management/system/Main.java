package school.management.system;

import static school.management.system.App.prefs;

import java.awt.Color;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import school.management.system.data.DB;
import school.management.system.data.DataSource;
import school.management.system.model.adm.Admin;
import school.management.system.ui.adm.AddAdmin;

/**
 * <h1>The Main executor of the {@link App}</h1>
 * <p>
 * This class is the entry point of the School Management System application. It
 * sets up the Look and Feel of the application, initializes the {@link App} and
 * {@link DB} classes, and retrieves the saved Look and Feel from the
 * preferences.
 * </p>
 * <h3><b>Some Information About The Author</b></h3>
 * <img src="./../../../../libs/img/me.jpg" width="120" height="180">
 * <ul>
 * <li>Name: Ibn Issah</li>
 * <li>Contact: <a href="tel:+233548570375">+233548570375</a></li>
 * <li>email:
 * <a href="mailto:issahsaalim006@gmail.com">issahsaalim006@gmail.com</a></li>
 * </ul>
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    /**
     * The Main constructor of the School Management System application.
     * <p>
     * This constructor sets up the Look and Feel of the application, initializes
     * the {@link App} and {@link DB} classes, and retrieves the saved Look and
     * Feel from the preferences.
     * </p>
     * 
     * @implNote This constructor sets the Look and Feel of the application and
     *           initializes the {@link App} and {@link DB} classes. It also
     *           retrieves the saved Look and Feel from the preferences and applies
     *           it.
     */
    Main() {
        try {
            UIManager.installLookAndFeel("Flat light laf", "com.formdev.flatlaf.FlatLightLaf");
            UIManager.installLookAndFeel("Flat dark laf", "com.formdev.flatlaf.FlatDarkLaf");
            UIManager.installLookAndFeel("Mac Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf");
            UIManager.installLookAndFeel("Mac Light", "com.formdev.flatlaf.themes.FlatMacLightLaf");
            String savedLaf = prefs.get("lookAndFeel", UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(savedLaf);

            // Apply saved table settings on startup
            UIManager.put("Table.showGrid", prefs.getBoolean("tableShowGrid", true));
            UIManager.put("Table.gridColor", new Color(prefs.getInt("tableGridColor", Color.BLACK.getRGB())));

            App app = new App();
            app.init();
            try {
                DB.setup();
                DataSource.performMigrationIfNeeded();

                // If no admins exist yet (fresh install), prompt to create the first admin
                try {
                    List<Admin> admins = app.getDb().fetchAllAdmins();
                    if (admins == null || admins.isEmpty()) {
                        // Show AddAdmin on the EDT and auto-login the created admin
                        SwingUtilities.invokeLater(() -> {
                            new AddAdmin(app.getWin(), app.getDb(), app.getAdmin(),
                                false, () -> {
                                    // After creation, mark as logged in and refresh UI
                                    try {
                                        app.getAuthService().setLoggedIn(true);
                                        app.getMainPanel().refreshDashboard();
                                    } catch (Exception exc) {
                                        exc.printStackTrace();
                                    }
                                });
                        });
                    }
                } catch (Exception ex) {
                    // If admin existence check fails, continue without blocking startup
                    System.err.println("Admin check failed: " + ex.getMessage());
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Database setup failed: " + e.getMessage(), "DB Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            IO.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
