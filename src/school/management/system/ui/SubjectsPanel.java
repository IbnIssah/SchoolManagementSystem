package school.management.system.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import school.management.system.data.DataService;
import school.management.system.model.subject.Subject;

/**
 * Panel for managing subjects in the school management system. Provides
 * functionalities to add, edit, delete, and view subjects.
 * 
 * @author Ibn Issah
 */
public class SubjectsPanel extends JPanel {

    /** The table and model for displaying subjects. */
    private final JTable subjectsTable;
    /** The model for the subjects table. */
    private final DefaultTableModel subjectsModel;
    /** The DataService for data operations. */
    private final DataService dataService;

    /**
     * Constructs the SubjectsPanel with the given DataService.
     * 
     * @param uiManager   the UIManager for UI utilities
     * @param dataService the DataService for data operations
     */
    public SubjectsPanel(UIManager uiManager, DataService dataService) {
        this.dataService = dataService;

        setLayout(null);
        setBorder(new TitledBorder("Subject Management"));

        // --- Subjects Table ---
        String[] columnNames = { "ID", "Subject Name" };
        subjectsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        subjectsTable = new JTable(subjectsModel);
        subjectsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(subjectsTable);
        scrollPane.setBounds(20, 30, 450, 380);
        add(scrollPane);

        // --- Buttons ---
        JButton addButton = new JButton("Add Subject");
        addButton.setFont(uiManager.fontMain(15, Font.BOLD));
        addButton.setBounds(500, 30, 160, 40);
        add(addButton);

        JButton editButton = new JButton("Edit Subject");
        editButton.setFont(uiManager.fontMain(15, Font.BOLD));
        editButton.setBounds(500, 90, 160, 40);
        add(editButton);

        JButton deleteButton = new JButton("Delete Subject");
        deleteButton.setFont(uiManager.fontMain(15, Font.BOLD));
        deleteButton.setBounds(500, 150, 160, 40);
        add(deleteButton);

        // --- Action Listeners ---
        addButton.addActionListener(this::addSubject);
        editButton.addActionListener(this::editSubject);
        deleteButton.addActionListener(this::deleteSubject);

        refreshSubjects();
    }

    /**
     * Refreshes the subjects displayed in the table by fetching from the data
     * source.
     */
    public void refreshSubjects() {
        subjectsModel.setRowCount(0);
        try {
            List<Subject> subjects = dataService.fetchAllSubjects();
            for (Subject subject : subjects) {
                subjectsModel.addRow(new Object[] { subject.getId(), subject.getName() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a dialog to add a new subject.
     * @param e The action event triggering the addition
     */
    private void addSubject(ActionEvent e) {
        String subjectName = JOptionPane.showInputDialog(this, "Enter new subject name:", "Add Subject",
                JOptionPane.PLAIN_MESSAGE);
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            try {
                dataService.addSubject(subjectName.trim());
                refreshSubjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding subject: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Edits the selected subject.
     * @param e The action event triggering the edit
    */
    private void editSubject(ActionEvent e) {
        int selectedRow = subjectsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to edit.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectId = (Integer) subjectsModel.getValueAt(selectedRow, 0);
        String currentName = (String) subjectsModel.getValueAt(selectedRow, 1);

        String newName = (String) JOptionPane.showInputDialog(this, "Enter new name for the subject:", "Edit Subject",
                JOptionPane.PLAIN_MESSAGE, null, null, currentName);

        if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentName)) {
            try {
                dataService.updateSubject(new Subject(subjectId, newName.trim()));
                refreshSubjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating subject: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes the selected subject.
     * @param e The action event triggering the deletion
    */
    private void deleteSubject(ActionEvent e) {
        int selectedRow = subjectsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int subjectId = (Integer) subjectsModel.getValueAt(selectedRow, 0);
        String subjectName = (String) subjectsModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + subjectName + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                dataService.deleteSubject(subjectId);
                refreshSubjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting subject: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}