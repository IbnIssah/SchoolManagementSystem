package school.management.system;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import lombok.Data;
import school.management.system.data.DB;
import school.management.system.data.DataService;
import school.management.system.data.DataSource;
import school.management.system.model.adm.Admin;
import school.management.system.util.IconUtils;
import school.management.system.model.student.Student;
import school.management.system.model.teacher.Teacher;
import school.management.system.service.AuthService;
import school.management.system.service.FileService;
import school.management.system.ui.AboutDialog;
import school.management.system.ui.MainPanel;
import school.management.system.ui.SchoolPanel;
import school.management.system.ui.Settings;
import school.management.system.ui.UIManager;
import school.management.system.ui.ViewPanel;

/**
 * The main application class for the School Management System. This class
 * initializes the main window, panels, menu bar, and shared components. It also
 * manages the navigation between different panels and holds shared data
 * objects.
 * 
 * @author Ibn Issah
 */
@SuppressWarnings("unused")
@Data
public class App {
    // main window and components

    /** The main application frame */
    private final JFrame win = new JFrame(); 

    /** Panel for login/register/initial view */
    private MainPanel mainPanel; 

    /** Panel for student/teacher tables and actions */
    private SchoolPanel schoolPanel; 
    /** Panel for detailed view */
    private ViewPanel viewPanel; 

    // Common UI components (e.g., menu bar)
    /** Menu bar for the application */
    private JMenuBar menuBar; 

    /** Menus for the application */
    private JMenu fileMenu, editmenu, viewMenu, settingsMenu, helpMenu;
    
    /** Menu items for the application */
    private JMenuItem stdtItem, tchItem, settingsItem;

    // Shared data and utility objects

    /** Teacher data model */
    private final Teacher teacher = new Teacher();
    /** Student data model */
    private final Student student = new Student();
    /** Admin data model */
    private final Admin admin = new Admin();
    /** Database access object */
    private final DB db = new DB();

    // Services
    /** UI manager for handling UI components */
    private UIManager uiManager;
    /** Service for data operations */
    private DataService dataService;
    /** Service for authentication */
    private AuthService authService;
    /** Service for file operations */
    private FileService fileService;

    // Application preferences
    /** User preferences for the application */
    public static final Preferences prefs = Preferences.userRoot().node("school-management-system");

    /** Author image */
    private static final Image author = IconUtils.loadScaledImage("libs/img/me.jpg", -1, -1).getImage();
    /** Icon image */
    private static final Image icon = IconUtils.getAppIcon().getImage();

    /**
     * Nullifies the name of the selected teacher and student
     */
    public void nullifyName() {
        teacher.setName(null);
        student.setFirstName(null);
    }

    /**
     * Shows the main login/register panel and hides others.
     * @param e the action event triggering the view
     */
    public void showMainPanel(ActionEvent e) {
        mainPanel.setVisible(true);
        schoolPanel.setVisible(false);
        viewPanel.setVisible(false);
        mainPanel.refreshDashboard(); // Refresh stats when showing main panel
    }

    /**
     * Shows the school management panel (tables) and hides others.
     * @param e the action event triggering the view
     */
    public void showSchoolPanel(ActionEvent e) {
        mainPanel.setVisible(false);
        schoolPanel.setVisible(true);
        viewPanel.setVisible(false);
        dataService.fetchData(); // Refresh data when showing school panel
    }

    /**
     * Shows the detailed view panel and hides others.
     * @param e the action event triggering the view
     */
    public void showViewPanel(ActionEvent e) {
        mainPanel.setVisible(false);
        schoolPanel.setVisible(false);
        viewPanel.setVisible(true);
    }

    /**
     * Shows the detailed view panel for a specific student.
     * 
     * @param student The student to display.
     */
    public void showViewPanelForStudent(Student student) {
        if (student == null || student.getFirstName() == null)
            return;
        viewPanel.displayStudent(student);
        showViewPanel(null);
    }

    /**
     * Shows the detailed view panel for a specific teacher.
     * 
     * @param teacher The teacher to display.
     */
    public void showViewPanelForTeacher(Teacher teacher) {
        if (teacher == null || teacher.getName() == null)
            return;
        viewPanel.displayTeacher(teacher);
        showViewPanel(null);
    }

    /**
     * Initializes the main window and its components.
     */
    public void init() {
        win.setTitle("School Management System");
        win.setIconImage(icon);
        win.setSize(950, 610);
        win.setLocationRelativeTo(null);
        win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                App.this.handleExit();
            }
        });
        win.setLayout(null);
        win.setResizable(false);

        // Initialize services
        uiManager = new UIManager(win);
        dataService = new DataService(this, db, uiManager); // DataService needs App and UIManager
        authService = new AuthService(win, this, db, admin, uiManager);
        fileService = new FileService(win, uiManager, db, this);

        // Initialize menubar and components first
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        editmenu = new JMenu("Edit");
        viewMenu = new JMenu("View");
        settingsMenu = new JMenu("Settings");
        helpMenu = new JMenu("Help");

        JMenuItem exportStudentsItem = new JMenuItem("Export Students to CSV");
        exportStudentsItem.addActionListener(e -> fileService.exportModelToCsv(schoolPanel.getStdMod(), "students.csv"));

        JMenuItem exportTeachersItem = new JMenuItem("Export Teachers to CSV");
        exportTeachersItem.addActionListener(e -> fileService.exportModelToCsv(schoolPanel.getTchMod(), "teachers.csv"));

        fileMenu.add(exportStudentsItem);
        fileMenu.add(exportTeachersItem);
        fileMenu.addSeparator();

        JMenuItem importStudentsItem = new JMenuItem("Import Students from CSV");
        importStudentsItem.addActionListener(e -> fileService.importFromCsv("student"));

        JMenuItem importTeachersItem = new JMenuItem("Import Teachers from CSV");
        importTeachersItem.addActionListener(e -> fileService.importFromCsv("teacher"));

        fileMenu.addSeparator(); // Add a separator before backup
        JMenuItem backupItem = new JMenuItem("Backup Database...");
        backupItem.addActionListener(fileService::backupDatabase);
        fileMenu.add(backupItem);

        JMenuItem restoreItem = new JMenuItem("Restore Database..."); // Restore database menu item
        restoreItem.addActionListener(fileService::restoreDatabase);
        fileMenu.add(restoreItem);

        fileMenu.add(importStudentsItem);
        fileMenu.add(importTeachersItem);

        stdtItem = new JMenuItem("Student"); // Student menu item
        stdtItem.addActionListener(e -> showViewPanelForStudent(student));
        tchItem = new JMenuItem("Teacher"); // Teacher menu item
        tchItem.addActionListener(e -> showViewPanelForTeacher(teacher));

        settingsItem = new JMenuItem("Open Settings..."); // Settings menu item
        settingsItem.addActionListener(e -> SwingUtilities.invokeLater(() -> new Settings(win, uiManager, dataService)));

        viewMenu.add(stdtItem);
        viewMenu.add(tchItem);
        settingsMenu.add(settingsItem);

        menuBar.add(fileMenu);
        menuBar.add(editmenu);
        menuBar.add(viewMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> new AboutDialog(win, icon).setVisible(true));
        helpMenu.add(aboutItem);

        // Initialize menu items as disabled
        setAdminMenusEnabled(false);

        win.setJMenuBar(menuBar);

        // Initialize the panels after menu setup
        mainPanel = new MainPanel(win, this, uiManager, db, admin);
        schoolPanel = new SchoolPanel(win, this, uiManager, dataService, student, teacher);
        viewPanel = new ViewPanel(win, this, uiManager, dataService);

        win.add(mainPanel);
        win.add(schoolPanel);
        win.add(viewPanel);

        // Set initial panel visibility
        showMainPanel(null); // Pass null for ActionEvent

        // Apply any persisted login state (if admin was logged in last time)
        try {
            authService.attemptAutoLogin();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(win, ex.getMessage(), "Auto Log in", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        win.setVisible(true);
    }

    /**
     * Enable or disable admin-only menu items
     * 
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setAdminMenusEnabled(boolean enabled) {
        // File menu items
        for (int i = 0; i < fileMenu.getItemCount(); i++) {
            JMenuItem item = fileMenu.getItem(i);
            if (item != null) { // Separators are null
                item.setEnabled(enabled);
            }
        }

        // View menu items
        stdtItem.setEnabled(enabled);
        tchItem.setEnabled(enabled);

        // Settings menu
        settingsItem.setEnabled(enabled);

        // Edit menu
        editmenu.setEnabled(enabled);
    }

    /**
     * The method for implementing exiting logic
     */
    public void handleExit() {
        int choice = JOptionPane.showConfirmDialog(win, "Are you sure you want to exit?", "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            DataSource.close();
            System.exit(0);
        }
    }
}