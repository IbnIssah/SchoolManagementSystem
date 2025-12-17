package school.management.system.ui;

import java.awt.Cursor;
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
import javax.swing.border.TitledBorder;

import school.management.system.data.DB;
import school.management.system.model.adm.Admin;
import school.management.system.service.AuthService;

/**
 * The Login class is responsible for handling user authentication, including
 * both login and registration functionalities.
 */
public class Login {
    /** The login dialog instance. */
    private final JDialog log;
    
    /** Main label for the login dialog. */
    JLabel lblMain; 
    /** Username label. */
    JLabel uNameLbl;
    /** Password label. */
    JLabel pswdLbl;
    /** Main panel for the login dialog. */
    JPanel panel;
    /** Text field for entering the username. */
    JTextField uNameBox;
    /** Password field for entering the password. */
    JPasswordField pswdBox;
    /** OK button for login or registration. */
    JButton btnOK;
    /** Cancel button to close the login dialog. */
    JButton btnCancel;
    /** The authentication service for handling login logic. */
    private final AuthService authService;
    /** The admin model to populate with login data. */
    private final Admin admin;

    /**
     * Constructs a Login dialog.
     * 
     * @param title       The title of the dialog.
     * @param parent      The parent JFrame.
     * @param authService The AuthService for handling login logic.
     * @param db          The database instance (not used directly here).
     * @param admin       The Admin model to populate with login data.
     */
    public Login(String title, JFrame parent, AuthService authService, DB db, Admin admin) {
        this.authService = authService;
        this.admin = admin;
        log = new JDialog(parent);
        log.setTitle(title);
        log.setSize(500, 500);
        log.setLocationRelativeTo(null);
        log.setLayout(null);
        log.setResizable(false);

        panel = new JPanel();
        lblMain = new JLabel(log.getTitle());
        uNameLbl = new JLabel("User Name");
        pswdLbl = new JLabel("Password");
        uNameBox = new JTextField();
        pswdBox = new JPasswordField();
        btnOK = new JButton("OK");
        btnCancel = new JButton("Cancel");
        lblMain.setFont(new Font("Arial", Font.PLAIN, 30));
        lblMain.setBounds(140, 10, 200, 50);
        lblMain.setHorizontalAlignment(0);

        uNameLbl.setBounds(50, 100, 150, 30);
        uNameLbl.setFont(new Font("Arial", Font.BOLD, 20));

        uNameBox.setBounds(250, 100, 150, 30);
        uNameBox.setFont(new Font("Arial", Font.PLAIN, 15));

        pswdLbl.setBounds(50, 150, 150, 30);
        pswdLbl.setFont(uNameLbl.getFont());

        pswdBox.setBounds(250, 150, 150, 30);
        pswdBox.setEchoChar('●');
        pswdBox.setFont(new Font("Arial", Font.BOLD, 15));
        char echo = pswdBox.getEchoChar();

        JCheckBox togglePswd = new JCheckBox("Show Password");
        togglePswd.setFont(uNameBox.getFont());
        togglePswd.setBounds(250, 180, 150, 30);
        togglePswd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePswd.addActionListener(e -> {
            if (togglePswd.isSelected()) {
                pswdBox.setFont(uNameBox.getFont());
                pswdBox.setEchoChar((char) 0);
            } else {
                pswdBox.setFont(new Font("Arial", Font.BOLD, 15));
                pswdBox.setEchoChar(echo);
            }
        });

        panel.setBounds(5, 20, 470, 350);
        panel.setBorder(new TitledBorder("Enter Details"));
        panel.setLayout(null);
        panel.registerKeyboardAction(this::onOK, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.registerKeyboardAction(e -> log.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.add(lblMain);
        panel.add(uNameLbl);
        panel.add(uNameBox);
        panel.add(pswdLbl);
        panel.add(pswdBox);
        panel.add(togglePswd);

        btnOK.setBounds(250, 400, 100, 30);
        btnOK.addActionListener(this::onOK);

        btnCancel.setBounds(370, 400, 100, 30);
        btnCancel.addActionListener(e -> log.dispose());

        log.add(panel);
        log.add(btnOK);
        log.add(btnCancel);
        log.setVisible(true);
    }

    /**
     * onOk method to handle login and registration
     * @param e The ActionEvent triggered by the OK button or Enter key.
    */
    private void onOK(ActionEvent e) {
        admin.setUserName(uNameBox.getText());
        admin.setPassword(new String(pswdBox.getPassword()));
        try {
            if (uNameBox.getText().isEmpty() || String.valueOf(pswdBox.getPassword()).isEmpty()) {
                throw new IllegalArgumentException("Fields cannot be empty");
            }
            authService.login();
            log.dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(log, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
