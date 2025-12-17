package school.management.system.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import school.management.system.data.DataService;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.AttendanceRecord;
import school.management.system.model.student.Student;

/**
 * A panel for managing student attendance. It allows users to select a class
 * and date, load the list of students, and mark their attendance status.
 * 
 * @since 1.0
 * @version 1.0
 * @author Ibn Issah
 */
public class AttendancePanel extends JPanel {

    /** Table displaying the attendance records */
    private final JTable attendanceTable;
    /** Model for the attendance table */
    private final DefaultTableModel attendanceModel;
    /** Date chooser for selecting the date */
    private final JDateChooser dateChooser;
    /** Combo box for selecting the class */
    private final JComboBox<SchoolClass> classComboBox;
    /** Button to load students for the selected class and date */
    private final JButton loadButton;
    /** Button to save the attendance records */
    private final JButton saveButton;
    /** Data service for database operations */
    private final DataService dataService;
    /** UI Manager for styling */
    private final UIManager uiManager;

    /**
     * Constructs the AttendancePanel.
     *
     * @param uiManager   An instance of UIManager for UI utilities.
     * @param dataService An instance of DataService for database operations.
     */
    public AttendancePanel(UIManager uiManager, DataService dataService) {
        this.dataService = dataService;
        this.uiManager = uiManager;

        setLayout(null);
        setBorder(new TitledBorder("Student Attendance"));

        // --- Controls ---
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        dateLabel.setBounds(20, 30, 50, 30);
        add(dateLabel);

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date()); // Default to today
        dateChooser.setBounds(70, 30, 120, 30);
        add(dateChooser);

        JLabel classLabel = new JLabel("Class:");
        classLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        classLabel.setBounds(210, 30, 50, 30);
        add(classLabel);

        classComboBox = new JComboBox<>();
        classComboBox.setBounds(260, 30, 100, 30);
        add(classComboBox);
        populateClassComboBox();

        loadButton = new JButton("Load Students");
        loadButton.setBounds(380, 30, 150, 30);
        add(loadButton);

        // --- Attendance Table ---
        String[] columnNames = { "Student ID", "Name", "Status" };
        attendanceModel = new DefaultTableModel(columnNames, 0);
        attendanceTable = new JTable(attendanceModel);

        // Set up the status column with a JComboBox
        String[] statuses = { "Present", "Absent", "Late" };
        JComboBox<String> statusComboBox = new JComboBox<>(statuses);
        attendanceTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusComboBox));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBounds(20, 80, 640, 300);
        add(scrollPane);

        // --- Save Button ---
        saveButton = new JButton("Save Attendance");
        saveButton.setFont(uiManager.fontMain(15, Font.BOLD));
        saveButton.setBounds(510, 390, 150, 30);
        add(saveButton);

        addListeners();
    }

    /** 
     * Populates the class combo box with available class levels from the database.
     */
    private void populateClassComboBox() {
        try {
            List<SchoolClass> classes = dataService.fetchAllClasses();
            classComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new Vector<>(classes)));
            classComboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof SchoolClass) {
                        setText(((SchoolClass) value).getName());
                    }
                    return this;
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading class levels: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        loadButton.addActionListener(this::loadStudentsForAttendance);
        saveButton.addActionListener(this::saveAttendance);
    }

    /** 
     * Loads students for the selected class and date into the attendance table.
     *
     * @param e the action event triggering this method
     */
    private void loadStudentsForAttendance(ActionEvent e) {
        attendanceModel.setRowCount(0); // Clear existing rows
        SchoolClass selectedClass = (SchoolClass) classComboBox.getSelectedItem();
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a class.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int classLevel = selectedClass.getId();

        try {
            List<Student> students = dataService.getStudentsByClass(classLevel);
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students found for Class " + classLevel, "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (Student student : students) {
                String fullName = student.getFirstName() + " " + student.getLastName();
                // Add student to table with default "Present" status
                attendanceModel.addRow(new Object[] { student.getId(), fullName, "Present" });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 
     * Saves the attendance records from the table to the database.
     *
     * @param e the action event triggering this method
     */
    private void saveAttendance(ActionEvent e) {
        if (attendanceModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No attendance data to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<AttendanceRecord> records = new ArrayList<>();
        Date selectedDate = dateChooser.getDate();

        for (int i = 0; i < attendanceModel.getRowCount(); i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId((Integer) attendanceModel.getValueAt(i, 0));
            record.setStatus((String) attendanceModel.getValueAt(i, 2));
            record.setAttendanceDate(selectedDate);
            records.add(record);
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                dataService.saveAttendance(records);
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(AttendancePanel.this, "Attendance saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
        uiManager.startProgress(worker, "Saving Attendance", "Please wait");
    }
}