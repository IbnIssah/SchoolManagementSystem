package school.management.system.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import school.management.system.App;
import school.management.system.data.DB;
import school.management.system.model.DashboardStats;
import school.management.system.model.adm.Admin;
import school.management.system.service.AuthService;
import school.management.system.ui.adm.AddAdmin;
import school.management.system.ui.adm.AdminLog;

/**
 * The main panel
 */
public class MainPanel extends JPanel {

    /** The parent JFrame. */
    private final JFrame parentFrame;
    /** The database instance. */
    private final DB db;
    /** The admin model representing the logged-in admin. */
    private final Admin admin;
    /** The main application instance. */
    private final App app; // Reference to the main App for panel switching

    /** Button to log in as an admin. */
    private JButton btnLogin;
    /** Button to add another admin. */
    private JButton btnRegister;
    /** Button to view school information. */
    private JButton btnNext;
    /** Button to log out. */
    private JButton btnLogout;
    /** Label to greet the logged-in admin. */
    private JLabel greetingLabel;
    /** Label to display total students count. */
    private JLabel totalStudentsLabel;
    /** Label to display total teachers count. */
    private JLabel totalTeachersLabel;
    /** Label to display total fees collected. */
    private JLabel totalFeesLabel;
    /** The UIManager for UI utilities. */
    private final UIManager uiManager;
    /** The authentication service for handling login logic. */
    private final AuthService authService;

    /**
     * Constructs the MainPanel with references to the parent frame, application,
     * utility functions, database, and admin model.
     * 
     * @param parentFrame The parent JFrame.
     * @param app         The main application instance for panel switching.
     * @param uiManager   The UIManager for UI utilities.
     * @param db          The database instance for data access.
     * @param admin       The Admin model representing the logged-in admin.
     */
    public MainPanel(JFrame parentFrame, App app, UIManager uiManager, DB db, Admin admin) {
        this.parentFrame = parentFrame;
        this.app = app;
        this.uiManager = uiManager;
        this.db = db;
        this.admin = admin;
        this.authService = app.getAuthService();

        setLayout(null);
        setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        initComponents();
        addComponents();
        addListeners();

        // Check initial login state and update UI accordingly
        setAdminLoggedIn(authService.isLoggedIn());
    }

    /**
     * Initializes the components of the main panel.
     * 
     * @see #addComponents()
     * @see #addListeners()
     */
    private void initComponents() {
        btnLogin = new JButton("Login As Admin");
        btnRegister = new JButton("Add Another Admin");
        greetingLabel = new JLabel("");
        btnLogout = new JButton("Logout");
        btnNext = new JButton("View School Info");

        btnLogin.setBounds(30, 30, 200, 30);
        btnLogin.setFont(uiManager.fontMain(15, Font.BOLD));

        // Greeting label - hidden until admin logs in
        greetingLabel.setBounds(30, 65, 400, 50);
        greetingLabel.setFont(uiManager.fontMain(20, Font.PLAIN));
        greetingLabel.setForeground(new Color(33, 33, 33));
        greetingLabel.setVisible(false);

        btnRegister.setBounds(250, 30, 200, 30);
        btnRegister.setFont(btnLogin.getFont());
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setEnabled(false); // Disabled until admin logs in

        btnLogout.setBounds(470, 30, 120, 30);
        btnLogout.setFont(btnLogin.getFont());
        btnLogout.setVisible(false); // Hidden until admin logs in

        btnNext.setFont(uiManager.fontMain(15, Font.PLAIN));
        btnNext.setBounds(650, 30, 250, 30);
        btnNext.setEnabled(false); // Disabled until admin logs in

        // --- Dashboard Panels ---
        JPanel studentStatPanel = createStatPanel("Total Students", "0", new Color(0, 150, 136));
        studentStatPanel.setBounds(50, 150, 250, 120);
        totalStudentsLabel = (JLabel) ((JPanel) studentStatPanel.getComponent(0)).getComponent(1); // Get value label

        JPanel teacherStatPanel = createStatPanel("Total Teachers", "0", new Color(233, 30, 99));
        teacherStatPanel.setBounds(340, 150, 250, 120);
        totalTeachersLabel = (JLabel) ((JPanel) teacherStatPanel.getComponent(0)).getComponent(1); // Get value label

        JPanel feesStatPanel = createStatPanel("Total Fees Collected", "0.00", new Color(33, 150, 243));
        feesStatPanel.setBounds(50, 320, 540, 120);
        totalFeesLabel = (JLabel) ((JPanel) feesStatPanel.getComponent(0)).getComponent(1); // Get value label

        add(studentStatPanel);
        add(teacherStatPanel);
        add(feesStatPanel);
        add(greetingLabel);
    }

    /**
     * Creates a styled statistics panel with a title and initial value.
     * 
     * @param title        The title of the statistic.
     * @param initialValue The initial value to display.
     * @param color        The background color of the panel.
     * @return A JPanel containing the styled statistic.
     */
    private JPanel createStatPanel(String title, String initialValue, Color color) {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(color);

        JPanel innerPanel = new JPanel(new GridLayout(2, 1));
        innerPanel.setOpaque(false);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(uiManager.fontMain(16, Font.BOLD));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(uiManager.fontMain(36, Font.BOLD));
        valueLabel.setForeground(Color.WHITE);

        innerPanel.add(titleLabel);
        innerPanel.add(valueLabel);
        outerPanel.add(innerPanel, BorderLayout.CENTER);

        return outerPanel;
    }

    /**
     * Adds the components to the main panel.
     * 
     * @see #initComponents()
     * @see #addListeners()
     */
    private void addComponents() {
        add(btnLogin);
        add(btnLogout);
        add(btnRegister);
        add(btnNext);
    }

    /**
     * Refreshes the dashboard statistics by fetching data from the database in a
     * background thread to avoid blocking the UI.
     */
    public void refreshDashboard() {
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() throws Exception {
                return db.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    if (authService.isLoggedIn()) {
                        DashboardStats stats = get();
                        totalStudentsLabel.setText(String.valueOf(stats.getTotalStudents()));
                        totalTeachersLabel.setText(String.valueOf(stats.getTotalTeachers()));
                        totalFeesLabel.setText(String.format("%,.2f", stats.getTotalFeesCollected()));
                    }
                } catch (Exception e) {
                    // Silently fail or log error, dashboard is non-critical
                    System.err.println("Failed to refresh dashboard: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Adds action listeners to the buttons.
     * 
     * @see #initComponents()
     * @see #addComponents()
     */
    private void addListeners() {
        btnLogin.addActionListener(e -> new Login("Login", parentFrame, authService, db, admin));
        btnLogout.addActionListener(authService::logout);
        btnRegister.addActionListener(e -> {
            new AddAdmin(parentFrame, db, new Admin(), false, IO::println);
        });
        btnRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only allow opening admin log when actually logged in
                if (!authService.isLoggedIn())
                    return;
                if (SwingUtilities.isRightMouseButton(e)) {
                    new AdminLog(parentFrame, db, admin);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Only show the hint when enabled/logged in
                if (!authService.isLoggedIn())
                    return;
                btnRegister.setText("Right click to view all admins");
                btnRegister.setFont(uiManager.fontMain(10, Font.BOLD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnRegister.setText("Add Another Admin");
                btnRegister.setFont(btnLogin.getFont());
            }
        });
        btnNext.addActionListener(app::showSchoolPanel);
    }

    /**
     * Update main panel UI when admin logs in or out. When loggedIn==true: show
     * logout button, enable admin actions. When loggedIn==false: hide logout
     * button, disable admin actions except login.
     * 
     * @param loggedIn {@code true} to enable, {@code false} to disable
     */
    public void setAdminLoggedIn(boolean loggedIn) {
        // Update button states
        btnLogout.setVisible(loggedIn);
        btnRegister.setEnabled(loggedIn);
        btnNext.setEnabled(loggedIn);
        // The login button should be hidden when logged in, visible otherwise
        btnLogin.setVisible(!loggedIn);

        // Update menu items
        app.setAdminMenusEnabled(loggedIn);

        // If logged in, refresh dashboard and fetch data; otherwise clear sensitive UI
        if (loggedIn) {
            refreshDashboard();
            // Update greeting label with admin name or username
            String name = admin.getName();
            if (name == null || name.trim().isEmpty())
                name = admin.getUserName();
            if (name == null)
                name = "Admin";
            greetingLabel.setText("Welcome, " + name);
            greetingLabel.setVisible(true);
            // Only fetch data if the school panel has been initialized to avoid NPE during
            // startup
            if (app.getSchoolPanel() != null) {
                app.getDataService().fetchData();
            }
        } else {
            // Clear dashboard stats
            totalStudentsLabel.setText("0");
            totalTeachersLabel.setText("0");
            totalFeesLabel.setText("0.00");
            // Hide/reset greeting when logged out
            greetingLabel.setText("");
            greetingLabel.setVisible(false);
        }
    }
}
