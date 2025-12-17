package school.management.system.ui.teacher;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import school.management.system.data.DB;
import school.management.system.data.DataService;
import school.management.system.model.teacher.Teacher;
import school.management.system.ui.UIManager;
import school.management.system.util.IconUtils;

/**
 * The AddTch class is responsible for displaying the dialog to add or edit a
 * teacher.
 */
@SuppressWarnings("unused")
public class AddTch {
    /** The dialog for adding or editing a teacher. */
    protected JDialog tch;    
    /** The main panel containing all components. */
    protected final JPanel panel;
    /** Radio buttons for selecting gender. */
    protected JRadioButton maleRadio, femaleRadio;
    /** Button group to manage radio buttons. */
    protected ButtonGroup radioGroup;
    /** Buttons for dialog actions. */
    protected JButton btnOk, btnCancel, btnChoosePic;
    /** Labels for various fields and profile picture. */
    protected JLabel lblMain, nameLbl, contactLbl, addressLbl, mailLbl, profilePicLabel;
    /** Text fields for inputting teacher details. */
    protected JTextField nameBox, contactBox, addressBox, mailBox;

    // Dependencies
    /** The database instance for data operations. */
    private final DB db;
    /** The data service for accessing teacher data. */
    private final DataService dataService;
    /** The teacher object being added or edited. */
    private final Teacher teacher;
    /** Flag indicating if the dialog is in edit mode. */
    private final boolean isEditMode;
    /** The selected profile picture as a byte array. */
    private byte[] selectedProfilePic;
    /** The UI manager for handling UI-related tasks. */
    private final UIManager uiManager;

    /**
     * Constructs the AddTch dialog.
     * 
     * @param parent      the parent JFrame
     * @param uiManager   the UIManager for UI utilities
     * @param dataService the DataService for data operations
     * @param teacher     the Teacher object to add or edit
     * @param isEdit      {@code true} if editing an existing teacher,
     *                    {@code false} if adding a new teacher
     */
    public AddTch(JFrame parent, UIManager uiManager, DataService dataService, Teacher teacher, boolean isEdit) {
        this.dataService = dataService;
        this.db = dataService.getDB();
        this.teacher = teacher;
        this.isEditMode = isEdit;
        this.uiManager = uiManager;

        tch = new JDialog(parent);
        tch.setTitle(isEditMode ? "Edit Teacher" : "Add Teacher");
        tch.setSize(500, 550);
        tch.setLocationRelativeTo(parent);
        tch.setLayout(null);
        tch.setResizable(false);

        panel = new JPanel();
        lblMain = new JLabel(tch.getTitle());
        nameLbl = new JLabel("Full Name");
        nameBox = new JTextField();
        contactLbl = new JLabel("Contact");
        contactBox = new JTextField();
        addressLbl = new JLabel("Address");
        addressBox = new JTextField();
        mailLbl = new JLabel("Email");
        mailBox = new JTextField();
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        radioGroup = new ButtonGroup();
        btnOk = new JButton(isEditMode ? "Update" : "Add");
        btnCancel = new JButton("Cancel");
        btnChoosePic = new JButton("Choose Picture");

        profilePicLabel = new JLabel();
        profilePicLabel.setBounds(30, 80, 120, 120);
        profilePicLabel.setBorder(new LineBorder(Color.GRAY));
        profilePicLabel.setHorizontalAlignment(JLabel.CENTER);
        setProfilePic(null, "libs/img/teacher_placeholder.png");

        btnChoosePic.setBounds(30, 210, 120, 25);
        btnChoosePic.addActionListener(this::choosePicture);

        lblMain.setFont(new Font("Arial", Font.PLAIN, 30));
        lblMain.setBounds(140, 10, 200, 50);
        lblMain.setHorizontalAlignment(0);

        nameLbl.setFont(new Font("Arial", Font.BOLD, 20));
        nameLbl.setBounds(170, 80, 150, 30);

        nameBox.setFont(new Font("Arial", Font.PLAIN, 15));
        nameBox.setBounds(300, 80, 150, 30);

        contactLbl.setFont(nameLbl.getFont());
        contactLbl.setBounds(170, 130, 150, 30);

        contactBox.setBounds(300, 130, 150, 30);
        contactBox.setFont(nameBox.getFont());

        addressLbl.setBounds(170, 180, 150, 30);
        addressLbl.setFont(nameLbl.getFont());

        addressBox.setBounds(300, 180, 150, 30);
        addressBox.setFont(nameBox.getFont());

        mailLbl.setBounds(170, 230, 150, 30);
        mailLbl.setFont(nameLbl.getFont());

        mailBox.setBounds(300, 230, 150, 30);
        mailBox.setFont(nameBox.getFont());

        maleRadio.setBounds(170, 280, 150, 30);
        maleRadio.setFont(nameLbl.getFont());

        femaleRadio.setBounds(330, 280, 150, 30);
        femaleRadio.setFont(nameLbl.getFont());

        radioGroup.add(maleRadio);
        radioGroup.add(femaleRadio);

        // If in edit mode, populate fields with existing data
        if (isEditMode) {
            populateFields();
        }

        panel.setLayout(null);
        panel.setBounds(5, 20, 470, 400);
        panel.setBorder(new TitledBorder(isEditMode ? "Edit Details" : "Enter Details"));
        panel.registerKeyboardAction(this::onOk, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.registerKeyboardAction(e -> tch.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        panel.add(lblMain);
        panel.add(nameLbl);
        panel.add(nameBox);
        panel.add(contactLbl);
        panel.add(contactBox);
        panel.add(addressLbl);
        panel.add(addressBox);
        panel.add(mailLbl);
        panel.add(mailBox);
        panel.add(maleRadio);
        panel.add(femaleRadio);
        panel.add(profilePicLabel);
        panel.add(btnChoosePic);

        btnOk.setBounds(250, 450, 100, 30);
        btnOk.addActionListener(this::onOk);

        btnCancel.setBounds(370, 450, 100, 30);
        btnCancel.addActionListener(e -> tch.dispose());

        tch.add(panel);
        tch.add(btnOk);
        tch.add(btnCancel);
        tch.setVisible(true);
    }

    /**
     * Populates the input fields with the existing teacher data in edit mode. This
     * method is called when the dialog is opened in edit mode.
     */
    private void populateFields() {
        nameBox.setText(teacher.getName());
        contactBox.setText(teacher.getContact());
        addressBox.setText(teacher.getAddress());
        mailBox.setText(teacher.getEmail());
        this.selectedProfilePic = teacher.getProfilePic();
        setProfilePic(this.selectedProfilePic, "libs/img/teacher_placeholder.png");
        if ("male".equalsIgnoreCase(teacher.getGender())) {
            maleRadio.setSelected(true);
        } else {
            femaleRadio.setSelected(true);
        }
    }

    /**
     * Opens a file chooser dialog to select a profile picture for the teacher. The
     * selected image is read as a byte array and displayed in the dialog.
     * @param e the action event triggering this method
     */
    private void choosePicture(ActionEvent e) {
        var fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "gif"));
        if (fileChooser.showOpenDialog(tch) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                this.selectedProfilePic = Files.readAllBytes(file.toPath());
                setProfilePic(this.selectedProfilePic, null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(tch, "Error reading image file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Sets the profile picture in the dialog.
     * 
     * @param imageData       the byte array of the image data
     * @param placeholderPath the path to the placeholder image
     */
    private void setProfilePic(byte[] imageData, String placeholderPath) {
        profilePicLabel.setIcon(IconUtils.getProfilePicture(imageData, 120, 120, false));
    }

    /**
     * Handles the OK button action to add or update the teacher. Validates input
     * fields and performs the database operation in a background thread.
     * @param e the action event triggering this method
     */
    private void onOk(ActionEvent e) {
        teacher.setName(nameBox.getText());
        teacher.setContact(contactBox.getText());
        teacher.setAddress(addressBox.getText());
        teacher.setGender((maleRadio.isSelected()) ? "male" : "female");
        teacher.setEmail(mailBox.getText());
        teacher.setProfilePic(selectedProfilePic);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                dataService.addOrUpdateTeacher(teacher, isEditMode);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // check for exceptions
                    dataService.fetchData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(tch, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                tch.dispose();
            }
        };
        uiManager.startProgress(worker, isEditMode ? "Updating teacher..." : "Adding teacher...", "Please wait");
    }
}
