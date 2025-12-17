package school.management.system.ui.adm;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import school.management.system.data.DB;
import school.management.system.model.adm.Admin;

/** Th
 *  AddAdmin class is responsible for displaying the dialog to add or edit
 * an admin.
 * @since 1.0
 * @author Ibn Issah
 */
public class AddAdmin {
    /** The dialog window for adding/editing an admin. */
    private final JDialog dialog;
    /** The text field for admin name. */
    private final JTextField nameBox;
    /** The text field for admin user name. */
    private final JTextField uNameBox;
    /** The password fields for admin details. */
    private final JPasswordField pswdBox;
    /** The confirmation password field for admin details. */
    private final JPasswordField pswdBoxCfm;
    /** The database handler. */
    private final DB db;
    /** The admin being added or edited. */
    private final Admin admin;
    /** Flag indicating if the dialog is in edit mode. */
    private final boolean isEditMode;
    /** Callback to run after operation is complete. */
    private final Runnable onFinish;

    /**
     * Constructs the AddAdmin dialog.
     *   
     * @param parent   the parent JFrame
     * @param db       the database handler
     * @param admin    the Admin object to add or edit
     * @param isEdit   true if editing an existing admin, false if adding a new one
     * @param onFinish   a callback to run after the operation is complete
     */
    public AddAdmin(JFrame parent, DB db, Admin admin, boolean isEdit, Runnable onFinish) {
        this.db = db;
        this.admin = admin;
        this.isEditMode = isEdit;
        this.onFinish = onFinish;

        dialog = new JDialog(parent);
        dialog.setTitle(isEditMode ? "Edit Admin" : "Add Admin");
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        JLabel lblMain = new JLabel(dialog.getTitle());
        JLabel nameLbl = new JLabel("Name");
        JLabel uNameLbl = new JLabel("User Name");
        JLabel pswdLbl = new JLabel("Password");
        JLabel cfmPswdLbl = new JLabel("Confirm Password");
        JCheckBox showPswd = new JCheckBox("Show Password");
        
        nameBox = new JTextField();
        uNameBox = new JTextField();
        pswdBox = new JPasswordField();
        pswdBoxCfm = new JPasswordField();
        JButton btnOK = new JButton(isEditMode ? "Update" : "Add");
        JButton btnCancel = new JButton("Cancel");

        Font labelFont = new Font("Arial", Font.BOLD, 20);
        Font fieldFont = new Font("Arial", Font.PLAIN, 15);

        lblMain.setFont(new Font("Arial", Font.PLAIN, 30));
        lblMain.setBounds(140, 10, 200, 50);
        lblMain.setHorizontalAlignment(0);

        nameLbl.setFont(labelFont);
        nameLbl.setBounds(50, 80, 150, 30);
        nameBox.setFont(fieldFont);
        nameBox.setBounds(250, 80, 180, 30);

        uNameLbl.setFont(labelFont);
        uNameLbl.setBounds(50, 130, 150, 30);
        uNameBox.setFont(fieldFont);
        uNameBox.setBounds(250, 130, 180, 30);

        pswdLbl.setFont(labelFont);
        pswdLbl.setBounds(50, 180, 150, 30);
        pswdBox.setFont(fieldFont);
        pswdBox.setEchoChar('●');
        pswdBox.setBounds(250, 180, 180, 30);
        // Store the default echo char to toggle visibility later
        char echo = pswdBox.getEchoChar();

        cfmPswdLbl.setFont(new Font("Arial", Font.BOLD, 15));
        cfmPswdLbl.setBounds(50, 230, 200, 30);
        pswdBoxCfm.setFont(fieldFont);
        pswdBoxCfm.setEchoChar('●');
        pswdBoxCfm.setBounds(250, 230, 180, 30);

        showPswd.setFont(fieldFont);
        showPswd.setBounds(250, 255, 200, 30);
        // Toggle password visibility
        showPswd.addActionListener(e -> {
            if (showPswd.isSelected()) {
                pswdBox.setEchoChar((char) 0);
                pswdBoxCfm.setEchoChar((char) 0);
            } else {
                pswdBox.setEchoChar(echo);
                pswdBoxCfm.setEchoChar(echo);
            }
        });

        if (isEditMode) {
            populateFields();
            // For editing, we don't force a password change.
            // These fields are for setting a *new* password.
            pswdLbl.setText("New Password");
            cfmPswdLbl.setText("Confirm New Password");
        }

        panel.setLayout(null);
        panel.setBounds(5, 20, 470, 300);
        panel.setBorder(new TitledBorder((isEditMode ? "Edit" : "Enter") + " Details"));

        panel.registerKeyboardAction(this::onOK, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.registerKeyboardAction(e -> dialog.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        panel.add(lblMain);
        panel.add(nameLbl);
        panel.add(nameBox);
        panel.add(uNameLbl);
        panel.add(uNameBox);
        panel.add(pswdLbl);
        panel.add(pswdBox);
        panel.add(cfmPswdLbl);
        panel.add(pswdBoxCfm);
        panel.add(showPswd);

        btnOK.setBounds(250, 350, 100, 30);
        btnOK.addActionListener(this::onOK);

        btnCancel.setBounds(370, 350, 100, 30);
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(panel);
        dialog.add(btnOK);
        dialog.add(btnCancel);
        dialog.setVisible(true);
    }

    /**
     * Populates the fields with existing admin data in edit mode.
     */
    private void populateFields() {
        nameBox.setText(admin.getName());
        uNameBox.setText(admin.getUserName());
        // Password fields are left empty for security reasons.
    }

    /**
     * Handles the OK button action to add or update the admin.
     * 
     * @param e the ActionEvent triggered by the button click
     */
    private void onOK(ActionEvent e) {
        String name = nameBox.getText();
        String username = uNameBox.getText();
        String password = new String(pswdBox.getPassword());
        String confirmPassword = new String(pswdBoxCfm.getPassword());
        // Validate required fields
        if (name.trim().isEmpty() || username.trim().isEmpty()) {      
            JOptionPane.showMessageDialog(dialog, "Name and Username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // If not in edit mode, a password is required.
        if (!isEditMode && password.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Password cannot be empty for a new admin.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Update admin object with new values if all validations pass
        admin.setName(name);
        admin.setUserName(username);
        // Only set the password if the user entered a new one.
        if (!password.isEmpty()) {
            admin.setPassword(password);
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isEditMode) {
                    db.updateAdmin(admin);
                } else {
                    db.signUp(admin);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Wait for the operation to complete and check for exceptions
                    onFinish.run(); // Refresh the calling table
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Database operation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}