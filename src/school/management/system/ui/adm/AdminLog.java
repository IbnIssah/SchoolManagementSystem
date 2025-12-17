package school.management.system.ui.adm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import school.management.system.data.DB;
import school.management.system.model.adm.Admin;

/**
 * The AdminLog class is responsible for displaying the admin log dialog.
 * @since 1.0
 * @author Ibn Issah
 */
public class AdminLog {
    // GUI Components
    /** The admin log dialog. */
    protected JDialog admLog;
    /** The scroll pane for the admin table. */
    protected JScrollPane admScroll;
    /** The table displaying admin data. */
    protected JTable admTable;
    /** The table model for the admin table. */
    protected DefaultTableModel admMod;
    /** The database handler. */
    private final DB db;
    /** The currently logged-in admin. */
    private final Admin loggedInAdmin;

    /**
     * Constructs the AdminLog dialog.
     * 
     * @param parent        the parent JFrame
     * @param db            the database handler
     * @param loggedInAdmin the currently logged-in admin
     */
    public AdminLog(JFrame parent, DB db, Admin loggedInAdmin) {
        this.db = db;
        this.loggedInAdmin = loggedInAdmin;
        admLog = new JDialog(parent);
        admLog.setTitle("Admins");
        admLog.setSize(600, 300);
        admLog.setLocationRelativeTo(parent); // Center relative to parent
        admLog.setLayout(new BorderLayout()); // Use a layout manager
        admLog.setResizable(false);
        admLog.setVisible(true);
        // Define columns for the table
        String[] columnNames = { "ID", "Name", "Username" };

        admMod = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        admTable = new JTable(admMod);
        admScroll = new JScrollPane(admTable);

        admLog.add(admScroll, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        admLog.add(buttonPanel, BorderLayout.SOUTH);

        editButton.addActionListener(this::editAdmin);
        deleteButton.addActionListener(this::deleteAdmin);
        closeButton.addActionListener(e -> admLog.dispose());

        // Load data into the table
        loadAdminData();

        admLog.setVisible(true);
    }

    /**
     * Fetches admin data from the database and populates the table.
     */
    private void loadAdminData() {
        try {
            admMod.setRowCount(0); // Clear existing data
            List<Admin> admins = db.fetchAllAdmins();
            for (Admin admin : admins) {
                admMod.addRow(new Object[] { admin.getId(), admin.getName(), admin.getUserName() });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(admLog, "Error loading admin data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the editing of a selected admin.
     * @param e the ActionEvent triggering the edit action
     */
    private void editAdmin(ActionEvent e) {
        int selectedRow = admTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(admLog, "Please select an admin to edit.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int adminId = (int) admMod.getValueAt(selectedRow, 0);
        String adminName = (String) admMod.getValueAt(selectedRow, 1);
        String adminUsername = (String) admMod.getValueAt(selectedRow, 2);

        Admin adminToEdit = new Admin(adminId, adminName, adminUsername, null);

        // Open the AddAdmin dialog in edit mode
        new AddAdmin((JFrame) admLog.getParent(), db, adminToEdit, true, this::loadAdminData);
    }

    /**
     * Handles the deletion of a selected admin.
     * @param e the ActionEvent triggering the delete action
     */
    private void deleteAdmin(ActionEvent e) {
        int selectedRow = admTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(admLog, "Please select an admin to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int adminId = (int) admMod.getValueAt(selectedRow, 0);
        String adminName = (String) admMod.getValueAt(selectedRow, 1);

        // Prevent the user from deleting their own account
        if (loggedInAdmin != null && loggedInAdmin.getId() == adminId) {
            JOptionPane.showMessageDialog(admLog, "You cannot delete your own account.", "Action Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(admLog, "Are you sure you want to delete admin '" + adminName + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                db.deleteAdmin(adminId);
                loadAdminData(); // Refresh the table
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(admLog, "Error deleting admin: " + ex.getMessage(), "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
