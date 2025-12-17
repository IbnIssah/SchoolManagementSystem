package school.management.system.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import school.management.system.data.DataService;
import school.management.system.model.SchoolClass;
import school.management.system.model.subject.Subject;
import school.management.system.model.teacher.Teacher;
import school.management.system.model.teacher.TeacherAssignment;

/**
 * Panel for managing teacher and subject assignments.
 * 
 * @since 1.0
 * @version 1.0
 * @author Ibn Issah
 */
public class AssignmentsPanel extends JPanel {

    /** Table displaying the list of assignments */
    private final JTable assignmentsTable;
    /** Model for the assignments table */
    private final DefaultTableModel assignmentsModel;
    /** Data service for database operations */
    private final DataService dataService;
    /** Combo boxes for selecting teacher, subject, and class */
    private final JComboBox<Teacher> teacherComboBox;
    /** Combo boxes for selecting subject and class */
    private final JComboBox<Subject> subjectComboBox;
    /** Combo boxes for selecting class */
    private final JComboBox<SchoolClass> classComboBox;
    /** UI Manager for styling */
    private final UIManager uiManager;

    /**
     * Constructs the AssignmentsPanel.
     * 
     * @param uiManager   the UIManager for styling
     * @param dataService the DataService for data operations
     */
    public AssignmentsPanel(UIManager uiManager, DataService dataService) {
        this.dataService = dataService;
        this.uiManager = uiManager;

        setLayout(null);
        setBorder(new TitledBorder("Teacher & Subject Assignments"));

        // --- Assignment Creation Panel ---
        JPanel creationPanel = new JPanel();
        creationPanel.setBorder(new TitledBorder("Assign Subject to Teacher for a Class"));
        creationPanel.setBounds(20, 30, 640, 120);
        creationPanel.setLayout(null);
        add(creationPanel);

        teacherComboBox = new JComboBox<>();
        teacherComboBox.setBounds(20, 30, 200, 30);
        creationPanel.add(teacherComboBox);

        subjectComboBox = new JComboBox<>();
        subjectComboBox.setBounds(240, 30, 200, 30);
        creationPanel.add(subjectComboBox);

        classComboBox = new JComboBox<>();
        classComboBox.setBounds(460, 30, 160, 30);
        creationPanel.add(classComboBox);

        JButton assignButton = new JButton("Assign");
        assignButton.setFont(this.uiManager.fontMain(15, Font.BOLD));
        assignButton.setBounds(460, 70, 160, 30);
        creationPanel.add(assignButton);

        // --- Assignments Table ---
        String[] columnNames = { "ID", "Teacher", "Subject", "Class" };
        assignmentsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        assignmentsTable = new JTable(assignmentsModel);
        assignmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(assignmentsTable);
        scrollPane.setBounds(20, 160, 640, 220);
        add(scrollPane);

        // --- Delete Button ---
        JButton deleteButton = new JButton("Delete Assignment");
        deleteButton.setFont(this.uiManager.fontMain(15, Font.BOLD));
        deleteButton.setBounds(480, 390, 180, 30);
        add(deleteButton);

        // --- Action Listeners ---
        assignButton.addActionListener(this::createAssignment);
        deleteButton.addActionListener(this::deleteAssignment);

        // Set custom renderers for ComboBoxes to display names
        teacherComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Teacher) {
                    setText(((Teacher) value).getName());
                }
                return this;
            }
        });

        subjectComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Subject) {
                    setText(((Subject) value).getName());
                }
                return this;
            }
        });

        classComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SchoolClass) {
                    setText(((SchoolClass) value).getName());
                }
                return this;
            }
        });

        refreshPanel();
    }

    /**
     * Refreshes the panel data.
     */
    public void refreshPanel() {
        try {
            // Refresh ComboBoxes
            teacherComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(dataService.fetchAllTeachers())));
            classComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(dataService.fetchAllClasses())));
            subjectComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(dataService.fetchAllSubjects())));

            // Refresh Table
            assignmentsModel.setRowCount(0);
            List<TeacherAssignment> assignments = dataService.getTeacherAssignments();
            for (TeacherAssignment assignment : assignments) {
                // Get the class name from the ID for display
                String className = dataService.getClassName(assignment.getClassLevel());
                assignmentsModel.addRow(new Object[] { assignment.getAssignmentId(), assignment.getTeacherName(),
                        assignment.getSubjectName(), className });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error refreshing data: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a new assignment based on user input.
     * 
     * @param e the action event triggering this method
     */
    private void createAssignment(ActionEvent e) {
        Teacher selectedTeacher = (Teacher) teacherComboBox.getSelectedItem();
        Subject selectedSubject = (Subject) subjectComboBox.getSelectedItem();
        SchoolClass selectedClass = (SchoolClass) classComboBox.getSelectedItem();

        if (selectedTeacher == null || selectedSubject == null || selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a teacher, a subject, and a class.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            dataService.addTeacherAssignment(selectedTeacher.getId(), selectedSubject.getId(), selectedClass.getId());
            refreshPanel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating assignment: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the selected assignment after confirmation.
     * 
     * @param e the action event triggering this method
     */
    private void deleteAssignment(ActionEvent e) {
        int selectedRow = assignmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assignment to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int assignmentId = (Integer) assignmentsModel.getValueAt(selectedRow, 0);
        String teacherName = (String) assignmentsModel.getValueAt(selectedRow, 1);
        String subjectName = (String) assignmentsModel.getValueAt(selectedRow, 2);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete assignment for " + teacherName + " teaching " + subjectName + "?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                dataService.deleteTeacherAssignment(assignmentId);
                refreshPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting assignment: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}