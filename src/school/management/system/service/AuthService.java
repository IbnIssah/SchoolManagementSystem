package school.management.system.service;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import school.management.system.App;
import school.management.system.data.DB;
import school.management.system.model.adm.Admin;
import school.management.system.ui.UIManager;

/**
 * Handles authentication and session management for administrators.
 * 
 * @author Ibn Issah
 */
public class AuthService {
    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    /** The parent JFrame for dialog positioning */
    private final JFrame parentFrame;
    /** The main application instance */
    private final App app;
    /** The database access object */
    private final DB db;
    /** The admin model */
    private final Admin admin;
    /** The UI manager for dialogs and progress */
    private final UIManager uiManager;

    /**
     * Constructor for AuthService.
     * 
     * @param parentFrame the parent JFrame for dialog positioning
     * @param app         the main application instance
     * @param db          the database access object
     * @param admin       the admin model
     * @param uiManager   the UI manager for dialogs and progress
     */
    public AuthService(JFrame parentFrame, App app, DB db, Admin admin, UIManager uiManager) {
        this.parentFrame = parentFrame;
        this.app = app;
        this.db = db;
        this.admin = admin;
        this.uiManager = uiManager;
    }

    /**
     * Checks if an admin is still logged in.
     * 
     * @return {@code true} if an admin is still logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return App.prefs.getBoolean("isAdminLoggedIn", false);
    }

    /**
     * Sets the logged-in status of the admin.
     * 
     * @param loggedIn whether the admin is logged in or not
     */
    public void setLoggedIn(boolean loggedIn) {
        App.prefs.putBoolean("isAdminLoggedIn", loggedIn);
        if (loggedIn) {
            // Persist which admin is logged in so we can restore on restart
            if (admin.getUserName() != null) {
                App.prefs.put("lastAdminUser", admin.getUserName());
            }
        } else {
            App.prefs.remove("lastAdminUser");
        }

        // Update UI accordingly
        if (app.getMainPanel() != null) {
            app.getMainPanel().setAdminLoggedIn(loggedIn);
        }
    }

    /**
     * Initiates the login process for the admin.
     */
    public void login() {
        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return db.validateLogin(admin);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setLoggedIn(true);
                        JOptionPane.showMessageDialog(parentFrame, "Login successful!", "Login",
                                JOptionPane.INFORMATION_MESSAGE);
                        app.getDataService().fetchData();
                    } else {
                        setLoggedIn(false);
                        JOptionPane.showMessageDialog(parentFrame, "Login failed! Invalid username or password.",
                                "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    setLoggedIn(false);
                    uiManager.showErrorDialog("Login Error",
                            "A problem occurred while trying to log in.\nPlease check the database connection.",
                            (Exception) ex.getCause());
                }
            }
        };
        uiManager.startProgress(worker, "Logging in...", "Please wait");
    }

    /**
     * Logs out the current admin after confirmation.
     * 
     * @param e the action event triggering the logout
     */
    public void logout(ActionEvent e) {
        var choice = JOptionPane.showConfirmDialog(parentFrame, "Confirm logout?", "Logout",
                JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
            setLoggedIn(false);
            admin.setUserName(null);
            admin.setPassword(null);
            admin.setName(null);
            admin.setId(0);

            // Clear table data for security
            if (app.getSchoolPanel() != null) {
                app.getSchoolPanel().getStdMod().setRowCount(0);
                app.getSchoolPanel().getTchMod().setRowCount(0);
            }
        }
    }

    /**
     * Attempts to automatically log in the last admin user if previously logged in.
     * 
     */
    public void attemptAutoLogin() {
        if (isLoggedIn()) {
            String lastUser = App.prefs.get("lastAdminUser", null);
            if (lastUser != null && !lastUser.trim().isEmpty()) {
                try {
                    List<Admin> admins = db.fetchAllAdmins();
                    admins.stream().filter(a -> lastUser.equals(a.getUserName())).findFirst().ifPresent(a -> {
                        admin.setId(a.getId());
                        admin.setName(a.getName());
                        admin.setUserName(a.getUserName());
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to load admin list for auto-login", e);
                }
            }
            setLoggedIn(true);
            app.getDataService().fetchData();
        }
    }
}