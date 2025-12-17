package school.management.system.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import lombok.Getter;
import school.management.system.App;
import school.management.system.data.DB;
import school.management.system.data.DataService;
import school.management.system.model.student.Student;
import school.management.system.model.teacher.Teacher;
import school.management.system.ui.student.AddStd;
import school.management.system.ui.teacher.AddTch;

/**
 * A JPanel that represents the main school panel with tabs for different
 * functionalities.
 * 
 */
public class SchoolPanel extends JPanel {
    /** The parent JFrame. */
    private final JFrame parentFrame;
    /** The main application instance. */
    private final App app; // Reference to the main App for panel switching
    /** The UIManager for consistent styling. */
    private final UIManager uiManager;
    /** The DataService for data operations. */
    private final DataService dataService;
    /** The student model instance. */
    private final Student student;
    /** The teacher model instance. */
    private final Teacher teacher;

    /** The tabbed pane for different sections. */
    @Getter
    private JTabbedPane tabPane;
    /** Tables and their models for students and teachers. */
    private JTable stdTable, tchTable;
    /** Scroll panes for student and teacher tables. */
    private JScrollPane stdScroll, tchScroll;
    /** Models for student and teacher tables. */
    @Getter
    private DefaultTableModel stdMod, tchMod;
    /** The subjects panel. */
    @Getter
    private SubjectsPanel subjectsPanel;
    /** The assignments panel. */
    @Getter
    private AssignmentsPanel assignmentsPanel;
    /** The classes panel. */
    @Getter
    private ClassesPanel classesPanel;
    /** The search field for filtering tables. */
    private JTextField searchField;
    /** The dashboard panel. */
    private DashboardPanel dashboardPanel;
    /** Sorter for student table filtering. */
    private TableRowSorter<DefaultTableModel> studentSorter;
    /** Sorter for teacher table filtering. */
    private TableRowSorter<DefaultTableModel> teacherSorter;
    /** Buttons for various actions. */
    private JButton btnPrev, btnRefresh, btnAddStd, btnAddTch, btnDelStd, btnDelTch, btnSchTch, btnSchStd, btnStdInfo,
            btnTchInfo;
    // Add a label for the search field to control visibility
    /** Label for the search/filter field. */
    private JLabel searchLabel;

    /** 
     * Constructs the SchoolPanel.
     * 
     * @param parentFrame the main application JFrame
     * @param app         the main App instance for panel switching
     * @param uiManager   the UIManager for consistent styling
     * @param dataService the DataService for data operations
     * @param student     the Student model instance
     * @param teacher     the Teacher model instance
     */
    public SchoolPanel(JFrame parentFrame, App app, UIManager uiManager, DataService dataService, Student student,
            Teacher teacher) {
        this.parentFrame = parentFrame;
        this.app = app;
        this.uiManager = uiManager;
        this.dataService = dataService;
        this.student = student;
        this.teacher = teacher;

        setLayout(null);
        setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        initComponents();
        addComponents();
        addListeners();
    }

    /** 
     * Initializes UI components.
     */
    private void initComponents() {
        stdMod = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tchMod = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stdTable = new JTable(stdMod);
        stdScroll = new JScrollPane(stdTable);
        tchTable = new JTable(tchMod);
        tchScroll = new JScrollPane(tchTable);

        btnPrev = new JButton("Back");
        btnRefresh = new JButton("Refresh Table");
        btnAddStd = new JButton("Add Student");
        btnAddTch = new JButton("Add Teacher");
        btnDelStd = new JButton("Delete Student");
        btnDelTch = new JButton("Delete Teacher");
        btnSchStd = new JButton("Search Student");
        btnSchTch = new JButton("Search Teacher");
        btnStdInfo = new JButton("Student's Details");
        btnTchInfo = new JButton("Teacher's Details");

        stdMod.addColumn("NO.");
        stdMod.addColumn("Student ID");
        stdMod.addColumn("First Name");
        stdMod.addColumn("Middle Name");
        stdMod.addColumn("Surname");
        stdMod.addColumn("Gender");
        stdMod.addColumn("Date of Birth");
        stdMod.addColumn("Class");
        // Hidden column for profile picture data
        stdMod.addColumn("Profile Picture");

        tchMod.addColumn("NO.");
        tchMod.addColumn("Teacher Id");
        tchMod.addColumn("Name");
        tchMod.addColumn("Contact");
        tchMod.addColumn("Gender");
        tchMod.addColumn("Address");
        tchMod.addColumn("Email");
        // Hidden column for profile picture data
        tchMod.addColumn("Profile Picture");

        // --- Sorters for real-time filtering ---
        studentSorter = new TableRowSorter<>(stdMod);
        stdTable.setRowSorter(studentSorter);

        teacherSorter = new TableRowSorter<>(tchMod);
        tchTable.setRowSorter(teacherSorter);

        // --- Search Field ---
        searchLabel = new JLabel("Filter:");
        searchLabel.setFont(uiManager.fontMain(15, Font.BOLD));
        searchLabel.setBounds(30, 0, 50, 30);
        searchLabel.setVisible(false);
        searchField = new JTextField();
        searchField.setBounds(85, 0, 250, 30);
        searchField.setVisible(false);

        tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.setBounds(30, 30, 700, 500);
        tabPane.setBorder(new TitledBorder(""));

        dashboardPanel = new DashboardPanel(new DB());
        tabPane.addTab("Dashboard", dashboardPanel);

        tabPane.addTab("Students", stdScroll);
        tabPane.addTab("Teachers", tchScroll);

        // Add new panels for Attendance and Fees, passing DataService
        AttendancePanel attendancePanel = new AttendancePanel(uiManager, dataService);
        tabPane.addTab("Attendance", attendancePanel);
        FeesPanel feesPanel = new FeesPanel(uiManager, dataService);
        tabPane.addTab("Fees", feesPanel);
        subjectsPanel = new SubjectsPanel(uiManager, dataService);
        tabPane.addTab("Subjects", subjectsPanel);
        assignmentsPanel = new AssignmentsPanel(uiManager, dataService);
        tabPane.addTab("Assignments", assignmentsPanel);
        ReportsPanel reportsPanel = new ReportsPanel(uiManager, dataService);
        tabPane.addTab("Reports", reportsPanel);
        classesPanel = new ClassesPanel(app, uiManager, dataService);
        tabPane.addTab("Classes", classesPanel);

        stdTable.setEnabled(true);
        stdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stdTable.setRowSelectionAllowed(true);
        stdTable.setShowGrid(true);
        stdTable.setGridColor(Color.BLACK);

        tchTable.setEnabled(true);
        tchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tchTable.setRowSelectionAllowed(true);
        tchTable.setShowGrid(true);
        tchTable.setGridColor(Color.BLACK);

        stdScroll.setFont(uiManager.fontMain(15, Font.PLAIN));
        stdScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tchScroll.setFont(uiManager.fontMain(15, Font.PLAIN));
        tchScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        btnRefresh.setBounds(750, 100, 150, 30);
        btnRefresh.setFont(uiManager.fontMain(15, Font.BOLD));
        btnRefresh.setToolTipText("Refresh");

        btnPrev.setBounds(750, 30, 150, 30); // Same bounds as btnNext in MainPanel
        btnPrev.setFont(uiManager.fontMain(15, Font.BOLD));

        btnAddStd.setBounds(750, 150, 150, 30);
        btnAddStd.setFont(uiManager.fontMain(15, Font.BOLD));

        btnAddTch.setBounds(750, 200, 150, 30);
        btnAddTch.setFont(uiManager.fontMain(15, Font.BOLD));

        btnSchTch.setBounds(750, 250, 150, 30);
        btnSchTch.setFont(uiManager.fontMain(15, Font.BOLD));

        btnSchStd.setBounds(750, 300, 150, 30);
        btnSchStd.setFont(uiManager.fontMain(15, Font.BOLD));

        btnDelTch.setBounds(750, 350, 150, 30);
        btnDelTch.setFont(uiManager.fontMain(15, Font.BOLD));

        btnDelStd.setBounds(750, 400, 150, 30);
        btnDelStd.setFont(uiManager.fontMain(15, Font.BOLD));

        btnTchInfo.setBounds(750, 450, 150, 30);
        btnTchInfo.setFont(uiManager.fontMain(15, Font.PLAIN));

        btnStdInfo.setBounds(750, 500, 150, 30);
        btnStdInfo.setFont(uiManager.fontMain(15, Font.PLAIN));
    }

    /** 
     * Adds components to the panel.
     */
    private void addComponents() {
        add(searchLabel);
        add(searchField);
        add(tabPane);
        add(btnPrev);
        add(btnRefresh);
        add(btnAddStd);
        add(btnAddTch);
        add(btnSchStd);
        add(btnSchTch);
        add(btnDelStd);
        add(btnDelTch);
        add(btnTchInfo);
        add(btnStdInfo);
    }

    /** 
     * Adds listeners to various components for interactivity.
     */
    private void addListeners() {
        tabPane.addChangeListener(e -> {
            stdTable.clearSelection();
            tchTable.clearSelection();
            app.nullifyName(); // Call nullifyName on the App instance

            // Control visibility of the search filter based on the selected tab
            int selectedIndex = tabPane.getSelectedIndex();
            boolean isFilterableTab = (selectedIndex == 1 || selectedIndex == 2); // Students or Teachers
            searchLabel.setVisible(isFilterableTab);
            searchField.setVisible(isFilterableTab);
            if (!isFilterableTab) {
                searchField.setText(""); // Clear search text when switching away
            }

            // Refresh dashboard or other panels when their tab is selected
            if (tabPane.getSelectedComponent() instanceof DashboardPanel) {
                ((DashboardPanel) tabPane.getSelectedComponent()).refreshCharts();
            }
        });

        stdTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = stdTable.getSelectedRow();
            if (!e.getValueIsAdjusting() && selectedRow != -1) {
                student.setId(Integer.parseInt((String.valueOf(stdTable.getValueAt(selectedRow, 1)))));
                student.setFirstName(String.valueOf(stdTable.getValueAt(selectedRow, 2)));
                student.setMiddleName(String.valueOf(stdTable.getValueAt(selectedRow, 3)));
                student.setLastName(String.valueOf(stdTable.getValueAt(selectedRow, 4)));
                student.setGender(String.valueOf(stdTable.getValueAt(selectedRow, 5)));
                student.setDateOfBirth(String.valueOf(stdTable.getValueAt(selectedRow, 6)));

                // The 'Class' column now shows the name, but we need the ID.
                // We'll retrieve the ID from the student ID in the table.
                int studentId = (Integer) stdTable.getValueAt(selectedRow, 1);
                try {
                    // A more efficient way would be to have the class ID in a hidden column,
                    // but for now, we can re-fetch the student's data to get the raw class ID.
                    // This is a simple approach to avoid major refactoring of the table model.
                    List<Student> result = dataService.searchAndReturn(String.valueOf(studentId), 0);
                    if (!result.isEmpty()) {
                        student.setLevel(result.get(0).getLevel());
                    }
                } catch (Exception ex) {
                    System.err.println("Could not re-fetch student to get class ID: " + ex.getMessage());
                }

                // Get the profile picture data from the hidden column
                student.setProfilePic((byte[]) stdTable.getValueAt(selectedRow, 8));
            }
        });
        stdTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    app.showViewPanelForStudent(student);
                }
            }
        });

        tchTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tchTable.getSelectedRow();
            if (!e.getValueIsAdjusting() && selectedRow != -1) {
                teacher.setId(Integer.parseInt(String.valueOf(tchTable.getValueAt(selectedRow, 1))));
                teacher.setName(String.valueOf(tchTable.getValueAt(selectedRow, 2)));
                teacher.setContact((String) tchTable.getValueAt(selectedRow, 3));
                teacher.setGender(String.valueOf(tchTable.getValueAt(selectedRow, 4)));
                teacher.setAddress(String.valueOf(tchTable.getValueAt(selectedRow, 5)));
                teacher.setEmail(String.valueOf(tchTable.getValueAt(selectedRow, 6)));
                // Get the profile picture data from the hidden column
                teacher.setProfilePic((byte[]) tchTable.getValueAt(selectedRow, 7));
            }
        });
        tchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    app.showViewPanelForTeacher(teacher);
                }
            }
        });

        stdScroll.registerKeyboardAction(this::handleDeleteStudent, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        tchScroll.registerKeyboardAction(this::handleDeleteTeacher, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        registerKeyboardAction(e -> dataService.refreshTableData(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        btnPrev.addActionListener(app::showMainPanel);
        btnRefresh.addActionListener(e -> dataService.refreshTableData());
        btnAddStd.addActionListener(e -> SwingUtilities
                .invokeLater(() -> new AddStd(parentFrame, uiManager, dataService, new Student(), false))); // Add
        // mode
        btnAddTch.addActionListener(e -> SwingUtilities
                .invokeLater(() -> new AddTch(parentFrame, uiManager, dataService, new Teacher(), false))); // Add
        // mode
        btnSchTch.addActionListener(dataService::searchTeacher);
        btnSchStd.addActionListener(dataService::searchStudent);
        btnDelTch.addActionListener(this::handleDeleteTeacher);
        btnDelStd.addActionListener(this::handleDeleteStudent);
        btnTchInfo.addActionListener(e -> app.showViewPanelForTeacher(teacher));
        btnStdInfo.addActionListener(e -> app.showViewPanelForStudent(student));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTables();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTables();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTables();
            }
        });
    }

    /** 
     * Filters the student and teacher tables based on the search field input.
     */
    private void filterTables() {
        String text = searchField.getText();
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + text);
        // Tab indices: 0=Dashboard, 1=Students, 2=Teachers
        int selectedIndex = tabPane.getSelectedIndex();

        if (selectedIndex == 1) { // Students tab
            studentSorter.setRowFilter(rf);
        } else if (selectedIndex == 2) { // Teachers tab
            teacherSorter.setRowFilter(rf);
        } // For other tabs, the sorter is not changed, and the field is hidden.
    }

    /** 
     * Handles the deletion of a selected student.
     * @param e the action event triggering the deletion
     */
    private void handleDeleteStudent(ActionEvent e) {
        if (stdTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a student to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // The student object is already populated by the ListSelectionListener
        String studentName = student.getFirstName() + " " + student.getLastName();
        dataService.deleteItem("student", studentName, () -> {
            try {
                dataService.deleteStudent(student);
            } catch (Exception ex) {
                throw new RuntimeException(ex); // Wrap in RuntimeException to be caught by SwingWorker
            }
        });
    }

    /** 
     * Handles the deletion of a selected teacher.
     * @param e the action event triggering the deletion
     */
    private void handleDeleteTeacher(ActionEvent e) {
        if (tchTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Please select a teacher to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // The teacher object is already populated by the ListSelectionListener
        dataService.deleteItem("teacher", teacher.getName(), () -> {
            try {
                dataService.deleteTeacher(teacher);
            } catch (Exception ex) {
                throw new RuntimeException(ex); // Wrap in RuntimeException to be caught by SwingWorker
            }
        });
    }
}