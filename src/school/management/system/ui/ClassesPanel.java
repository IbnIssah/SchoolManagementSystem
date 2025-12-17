package school.management.system.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import school.management.system.App;
import school.management.system.data.DataService;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.Student;

/**
 * Panel for managing class levels in the school management system.
 */
public class ClassesPanel extends JPanel {

    /** Table displaying the class levels */
    private final JTable classesTable;
    /** Model for the classes table */
    private final DefaultTableModel classesModel;
    /** Data service for database operations */
    private final DataService dataService;
    /** Reference to the main application */
    private final App app;

    /**
     * Constructs the ClassesPanel.
     *
     * @param app         the main application instance 
     * @param uiManager   the UIManager for UI utilities
     * @param dataService the DataService for data operations
     */
    public ClassesPanel(App app, UIManager uiManager, DataService dataService) {
        this.app = app;
        this.dataService = dataService;

        setLayout(null);
        setBorder(new TitledBorder("Class Level Management"));

        // --- Classes Table ---
        String[] columnNames = { "ID", "Class Name" };
        classesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        classesTable = new JTable(classesModel);
        classesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(classesTable);
        scrollPane.setBounds(20, 30, 450, 380);
        add(scrollPane);

        // --- Buttons ---
        JButton addButton = new JButton("Add Class");
        addButton.setFont(uiManager.fontMain(15, Font.BOLD));
        addButton.setBounds(500, 30, 160, 40);
        add(addButton);

        JButton editButton = new JButton("Edit Class");
        editButton.setFont(uiManager.fontMain(15, Font.BOLD));
        editButton.setBounds(500, 90, 160, 40);
        add(editButton);

        JButton deleteButton = new JButton("Delete Class");
        deleteButton.setFont(uiManager.fontMain(15, Font.BOLD));
        deleteButton.setBounds(500, 150, 160, 40);
        add(deleteButton);

        // --- Popup Menu for Right-Click Actions ---
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem viewStudentsItem = new JMenuItem("View Students in this Class");
        popupMenu.add(viewStudentsItem);

        // Add mouse listener for right-click events
        classesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = classesTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < classesTable.getRowCount()) {
                        classesTable.setRowSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // --- Action Listeners ---
        addButton.addActionListener(this::addClass);
        editButton.addActionListener(this::editClass);
        deleteButton.addActionListener(this::deleteClass);
        viewStudentsItem.addActionListener(this::viewStudentsInClass);

        refreshClasses();
    }

    /**
     * Refreshes the classes displayed in the table.
     */
    public void refreshClasses() {
        classesModel.setRowCount(0);
        try {
            List<SchoolClass> classes = dataService.fetchAllClasses();
            for (SchoolClass schoolClass : classes) {
                classesModel.addRow(new Object[] { schoolClass.getId(), schoolClass.getName() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading classes: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 
     * Adds a new class based on user input.
     * @param e the action event triggering the addition
     */
    private void addClass(ActionEvent e) {
        String className = JOptionPane.showInputDialog(this, "Enter new class name (e.g., '1', '2', 'JHS 1'):",
                "Add Class", JOptionPane.PLAIN_MESSAGE);
        if (className != null && !className.trim().isEmpty()) {
            try {
                dataService.addClass(className.trim());
                refreshClasses();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding class: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 
     * Edits the selected class's name.
     * @param e the action event triggering the edit
     */
    private void editClass(ActionEvent e) {
        int selectedRow = classesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to edit.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int classId = (Integer) classesModel.getValueAt(selectedRow, 0);
        String currentName = (String) classesModel.getValueAt(selectedRow, 1);

        String newName = (String) JOptionPane.showInputDialog(this, "Enter new name for the class:", "Edit Class",
                JOptionPane.PLAIN_MESSAGE, null, null, currentName);

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentName)) {
            try {
                dataService.updateClass(new SchoolClass(classId, newName.trim()));
                refreshClasses();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating class: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 
     * Deletes the selected class after confirmation.
     * @param e the action event triggering the deletion
     */
    private void deleteClass(ActionEvent e) {
        int selectedRow = classesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int classId = (Integer) classesModel.getValueAt(selectedRow, 0);
        String className = (String) classesModel.getValueAt(selectedRow, 1);

        dataService.deleteItem("class", className, () -> { // The deleteItem helper shows confirmation and handles
                                                           // errors
            try {
                dataService.deleteClass(classId);
            } catch (Exception ex) {
                throw new RuntimeException(ex); // Wrap in RuntimeException to be caught by the SwingWorker in
                                                // deleteItem
            }
        });
    }

    /** 
     * Views students enrolled in the selected class.
     * @param e the action event triggering the view
     */
    private void viewStudentsInClass(ActionEvent e) {
        int selectedRow = classesTable.getSelectedRow();
        if (selectedRow == -1) {
            return; // Should not happen with the right-click listener, but good practice
        }

        int classId = (Integer) classesModel.getValueAt(selectedRow, 0);
        String className = (String) classesModel.getValueAt(selectedRow, 1);

        try {
            List<Student> students = dataService.getStudentsByClass(classId);

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students are currently enrolled in " + className + ".",
                        "Students in Class", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create a JTable to display student details in a dialog
            String[] columnNames = { "ID", "First Name", "Last Name", "Gender" };
            DefaultTableModel studentTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Student student : students) {
                studentTableModel.addRow(new Object[] { student.getId(), student.getFirstName(), student.getLastName(),
                        student.getGender() });
            }

            JTable studentTable = new JTable(studentTableModel);
            studentTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = studentTable.getSelectedRow();
                        if (selectedRow != -1) {
                            int studentId = (Integer) studentTable.getValueAt(selectedRow, 0);
                            try {
                                // Fetch the full student object to show the profile
                                List<Student> students = dataService.searchAndReturn(String.valueOf(studentId), 0);
                                if (!students.isEmpty()) {
                                    // Close the current dialog before opening the new panel
                                    SwingUtilities.getWindowAncestor((java.awt.Component) e.getSource()).dispose();
                                    app.showViewPanelForStudent(students.get(0));
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(ClassesPanel.this,
                                        "Error loading student profile: " + ex.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });
            JScrollPane tableScrollPane = new JScrollPane(studentTable);
            tableScrollPane.setPreferredSize(new Dimension(450, 250));

            JOptionPane.showMessageDialog(this, tableScrollPane, "Students in " + className, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}