package school.management.system.ui.student;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.toedter.calendar.JDateChooser;

import school.management.system.data.DataService;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.Student;
import school.management.system.ui.UIManager;
import school.management.system.util.IconUtils;

public class AddStd extends JDialog {

    //--- Fields ---
    /** The text field for the student's first name. */
    private final JTextField fNameFld;
    /** The text field for the student's middle name. */
    private final JTextField mNameFld;
    /** The text field for the student's last name. */
    private final JTextField lNameFld;
    /** The combo box for selecting the student's gender. */
    private final JComboBox<String> genderComboBox;
    /** The combo box for selecting the student's class. */
    private final JComboBox<SchoolClass> classComboBox;
    /** The date chooser for selecting the student's date of birth. */
    private final JDateChooser dobChooser;
    /** The label for displaying the student's profile picture. */
    private final JLabel profilePicLabel;
    /** The data for the student's profile picture. */
    private byte[] profilePicData;

    //--- Dependencies ---
    /** The data service for accessing student and class data. */
    private final DataService dataService;
    /** The UI manager for managing UI components and styles. */
    private final UIManager uiManager;
    /** The student being added or edited. */
    private final Student student;
    /** Flag indicating if the dialog is in edit mode. */
    private final boolean isEdit;

    /** 
     * Constructor for AddStd dialog.
     *
     * @param parent        the parent frame
     * @param uiManager     the UI manager for managing UI components and styles
     * @param dataService   the data service for accessing student and class data
     * @param student       the student being added or edited
     * @param isEdit        flag indicating if the dialog is in edit mode
     */
    public AddStd(JFrame parent, UIManager uiManager, DataService dataService, Student student, boolean isEdit) {
        super(parent, true);
        this.uiManager = uiManager;
        this.dataService = dataService;
        this.student = student;
        this.isEdit = isEdit;

        setTitle((isEdit ? "Edit" : "Add") +" Student");
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(null);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder((isEdit ? "Edit" : "Enter") + " Student Details"));
        panel.setBounds(20, 20, 440, 480);
        panel.setLayout(null);
        add(panel);

        fNameFld = addLabeledField(panel, "First Name:", 30);
        mNameFld = addLabeledField(panel, "Middle Name:", 80);
        lNameFld = addLabeledField(panel, "Last Name:", 130);

        // Gender ComboBox
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        genderLabel.setBounds(20, 180, 150, 30);
        panel.add(genderLabel);
        genderComboBox = new JComboBox<>(new String[] { "Male", "Female" });
        genderComboBox.setBounds(180, 180, 240, 30);
        panel.add(genderComboBox);

        // Date of Birth Chooser
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        dobLabel.setBounds(20, 230, 150, 30);
        panel.add(dobLabel);
        dobChooser = new JDateChooser();
        dobChooser.setBounds(180, 230, 240, 30);
        panel.add(dobChooser);

        // Class ComboBox
        JLabel classLabel = new JLabel("Class:");
        classLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        classLabel.setBounds(20, 280, 150, 30);
        panel.add(classLabel);
        classComboBox = new JComboBox<>();
        classComboBox.setBounds(180, 280, 240, 30);
        panel.add(classComboBox);
        populateClassComboBox();

        // Profile Picture
        profilePicLabel = new JLabel();
        profilePicLabel.setBorder(new TitledBorder(""));
        profilePicLabel.setBounds(20, 330, 120, 120);
        panel.add(profilePicLabel);

        JButton choosePicButton = new JButton("Choose Picture");
        choosePicButton.setBounds(180, 350, 150, 30);
        panel.add(choosePicButton);

        JButton saveButton = new JButton(isEdit ? "Update" : "Save");
        saveButton.setBounds(360, 520, 100, 30);
        add(saveButton);

        // --- Action Listeners ---
        choosePicButton.addActionListener(this::chooseProfilePicture);
        saveButton.addActionListener(this::saveStudent);

        if (isEdit) {
            populateFields();
        } else {
            profilePicLabel.setIcon(IconUtils.getProfilePicture(null, 120, 120, true));
        }

        setVisible(true);
    }

    /** 
     * Adds a labeled text field to the specified panel.
     * @param panel the panel to add the field to
     * @param labelText the text for the label
     * @param y the y-coordinate for the field
     * @return the created JTextField
     */
    private JTextField addLabeledField(JPanel panel, String labelText, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(uiManager.fontMain(15, Font.PLAIN));
        label.setBounds(20, y, 150, 30);
        panel.add(label);

        JTextField textField = new JTextField();
        textField.setBounds(180, y, 240, 30);
        panel.add(textField);
        return textField;
    }

    /** 
     * Populates the class combo box with available classes.
     */
    private void populateClassComboBox() {
        try {
            List<SchoolClass> classes = dataService.fetchAllClasses();
            classComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(classes)));
            classComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof SchoolClass) {
                        setText(((SchoolClass) value).getName());
                    }
                    return this;
                }
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading classes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 
     * Populates the fields with the student's existing data for editing.
     */
    private void populateFields() {
        fNameFld.setText(student.getFirstName());
        mNameFld.setText(student.getMiddleName());
        lNameFld.setText(student.getLastName());
        genderComboBox.setSelectedIndex(student.getGender().equalsIgnoreCase("Male") ? 0 : 1);
        try {
            Date dob = new SimpleDateFormat("dd/MM/yyyy").parse(student.getDateOfBirth());
            dobChooser.setDate(dob);
        } catch (ParseException | NullPointerException e) {
            // Ignore if date is invalid or null
        }

        // Select the correct class in the combo box
        for (int i = 0; i < classComboBox.getItemCount(); i++) {
            if (classComboBox.getItemAt(i).getName().equals(dataService.getClassName(student.getLevel()))) {
                classComboBox.setSelectedIndex(i);
                break;
            }
        }

        profilePicData = student.getProfilePic();
        profilePicLabel.setIcon(IconUtils.getProfilePicture(profilePicData, 120, 120, true));
    }

    /** 
     * Opens a file chooser dialog to select a profile picture.
     * @param e the action event triggering the method
     */
    private void chooseProfilePicture(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                profilePicData = Files.readAllBytes(file.toPath());
                profilePicLabel.setIcon(IconUtils.getProfilePicture(profilePicData, 120, 120, true));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 
     * Saves or updates the student information.
     * @param e the action event triggering the method
     */
    private void saveStudent(ActionEvent e) {
        student.setFirstName(fNameFld.getText());
        student.setMiddleName(mNameFld.getText());
        student.setLastName(lNameFld.getText());
        student.setGender((String) genderComboBox.getSelectedItem());
        student.setProfilePic(profilePicData);

        if (dobChooser.getDate() != null) {
            student.setDateOfBirth(new SimpleDateFormat("yyyy/MM/dd").format(dobChooser.getDate()));
        }

        SchoolClass selectedClass = (SchoolClass) classComboBox.getSelectedItem();
        if (selectedClass != null) {
            student.setLevel(selectedClass.getId());
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                dataService.addOrUpdateStudent(student, isEdit);
                return null;
            }

            @Override
            protected void done() {
                dispose();
                dataService.refreshTableData();
            }
        };
        uiManager.startProgress(worker, isEdit ? "Updating..." : "Saving...", "Please wait");
    }
}