package school.management.system.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterJob;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import school.management.system.App;
import school.management.system.data.DB;
import school.management.system.data.DataService;
import school.management.system.model.student.Payment;
import school.management.system.model.student.Student;
import school.management.system.model.teacher.Teacher;
import school.management.system.model.teacher.TeacherAssignment;
import school.management.system.ui.student.AddStd;
import school.management.system.ui.teacher.AddTch;
import school.management.system.ui.UIManager;
import school.management.system.util.IconUtils;

/** 
 * Panel to view detailed profiles of students and teachers.
 */
@SuppressWarnings("unused")
public class ViewPanel extends JPanel {
    /** The parent JFrame. */
    private final JFrame parentFrame;
    /** The main application instance. */
    private final App app; // Reference to the main App for panel switching
    /** The DataService for data operations. */
    private final DataService dataService;
    /** The database instance for direct queries. */
    private final DB db;
    /** The UIManager for UI utilities. */
    private final UIManager uiManager;

    // --- UI Components ---
    /** Label for displaying the profile picture. */
    private final JLabel profilePicLabel;
    /** Label for displaying the full name. */
    private final JLabel nameLabel;
    /** Scroll pane for displaying assignments. */
    private final JScrollPane assignmentsScrollPane;
    /** List for displaying assignments. */
    private final JList<String> assignmentsList;
    /** Model for the assignments list. */
    private final DefaultListModel<String> assignmentsModel;
    /** Table for displaying payment history. */
    private final JTable paymentHistoryTable;
    /** Model for the payment history table. */
    private final DefaultTableModel paymentHistoryModel;
    /** Scroll pane for the payment history table. */
    private final JScrollPane paymentHistoryScrollPane;
    /** Button to go back to the list view. */
    private final JButton backButton;
    /** Button to edit the current profile. */
    private final JButton editButton;
    /** Button to print the current profile. */
    private final JButton printProfileButton;
    /** Button to record a payment. */
    private final JButton recordPaymentButton;

    /** List of detail components for easy removal when updating the view. */
    private final List<JLabel> detailComponents = new ArrayList<>();
    /** Current row index for placing detail components in the grid. */
    private int detailGridY; // To keep track of the current row for details
    /** The current entity being viewed (Student or Teacher). */
    private Object currentEntity; // Can be Student or Teacher

    /** 
     * Constructor to initialize the ViewPanel.
     * @param parentFrame the parent JFrame
     * @param app the main App instance
     * @param uiManager the UIManager instance for managing UI themes
     * @param dataService the DataService instance for data operations
     */
    public ViewPanel(JFrame parentFrame, App app, UIManager uiManager, DataService dataService) {
        this.parentFrame = parentFrame;
        this.app = app;
        this.uiManager = uiManager;
        this.dataService = dataService;
        this.db = dataService.getDB();

        // Use null layout for manual positioning
        setLayout(null);
        setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
        setBorder(new TitledBorder("Profile View"));

        // --- Action Buttons Panel (top right) ---
        backButton = new JButton("Back to List");
        backButton.setFont(uiManager.fontMain(15, Font.BOLD));
        backButton.setBounds(750, 50, 150, 30);
        backButton.addActionListener(app::showSchoolPanel);
        add(backButton);

        editButton = new JButton("Edit");
        editButton.setFont(uiManager.fontMain(15, Font.BOLD));
        editButton.setBounds(750, 90, 150, 30);
        editButton.addActionListener(this::handleEdit);
        add(editButton);

        printProfileButton = new JButton("Print Profile");
        printProfileButton.setFont(uiManager.fontMain(15, Font.BOLD));
        printProfileButton.setBounds(750, 130, 150, 30);
        printProfileButton.addActionListener(this::printProfile);
        add(printProfileButton);

        recordPaymentButton = new JButton("Record Payment");
        recordPaymentButton.setFont(uiManager.fontMain(15, Font.BOLD));
        recordPaymentButton.setBounds(750, 170, 150, 30);
        recordPaymentButton.addActionListener(this::handleRecordPayment);
        recordPaymentButton.setVisible(false); // Initially hidden
        add(recordPaymentButton);

        // --- Header and Profile Picture ---
        profilePicLabel = new JLabel();
        profilePicLabel.setBorder(new TitledBorder(""));
        profilePicLabel.setBounds(50, 50, 150, 150);
        add(profilePicLabel);

        nameLabel = new JLabel("Full Name");
        nameLabel.setFont(uiManager.fontMain(24, Font.BOLD));
        nameLabel.setBounds(220, 50, 500, 40);
        add(nameLabel);

        // --- Assignments List (for teachers) ---
        assignmentsModel = new DefaultListModel<>();
        assignmentsList = new JList<>(assignmentsModel);
        assignmentsList.setFont(uiManager.fontMain(14, Font.ITALIC));
        assignmentsScrollPane = new JScrollPane(assignmentsList);
        assignmentsScrollPane.setBorder(new TitledBorder("Assignments"));
        assignmentsScrollPane.setBounds(50, 380, 850, 150);
        add(assignmentsScrollPane);

        // --- Payment History Table (for students) ---
        String[] paymentColumnNames = { "Date", "Amount Paid", "Term", "Academic Year" };
        paymentHistoryModel = new DefaultTableModel(paymentColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentHistoryTable = new JTable(paymentHistoryModel);
        paymentHistoryScrollPane = new JScrollPane(paymentHistoryTable);
        paymentHistoryScrollPane.setBorder(new TitledBorder("Payment History"));
        paymentHistoryScrollPane.setBounds(50, 380, 850, 150); // Same position as assignments
        paymentHistoryScrollPane.setVisible(false); // Initially hidden
        add(paymentHistoryScrollPane);
    }

    /** 
     * Adds a detail row to the profile view.
     * @param label the label for the detail
     * @param value the value for the detail
     */
    private void addDetail(String label, String value) {
        // Calculate Y position based on the current detail row
        int yPos = 100 + (detailGridY - 1) * 35;

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(uiManager.fontMain(15, Font.BOLD));
        labelComponent.setBounds(220, yPos, 150, 30);
        add(labelComponent);
        detailComponents.add(labelComponent);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(uiManager.fontMain(15, Font.PLAIN));
        valueComponent.setBounds(380, yPos, 350, 30);
        add(valueComponent);
        detailComponents.add(valueComponent);

        detailGridY++; // Increment the row for the next detail
    }

    /** 
     * Displays the detailed profile of a student.
     * @param student the Student object whose details are to be displayed
     */
    public void displayStudent(Student student) {
        this.currentEntity = student;
        clearDetails();

        nameLabel.setText(student.getFirstName() + " " + student.getLastName());
        setProfilePic(student.getProfilePic(), null /* "libs/img/me.jpg" */);

        addDetail("Student ID", String.valueOf(student.getId()));
        addDetail("First Name", student.getFirstName());
        addDetail("Middle Name", student.getMiddleName());
        addDetail("Last Name", student.getLastName());
        addDetail("Gender", student.getGender());
        addDetail("Date of Birth", student.getDateOfBirth());
        addDetail("Class", dataService.getClassName(student.getLevel()));

        // Hide teacher-specific panel, show student-specific one
        assignmentsScrollPane.setVisible(false);
        paymentHistoryScrollPane.setVisible(true);
        recordPaymentButton.setVisible(true);
        loadPaymentHistory(student.getId());

        revalidate();
        repaint();
    }

    /** 
     * Loads the payment history for a student.
     * @param studentId the ID of the student whose payment history is to be loaded
     */
    private void loadPaymentHistory(int studentId) {
        paymentHistoryModel.setRowCount(0);
        try {
            List<Payment> payments = dataService.getStudentPayments(studentId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Payment p : payments) {
                paymentHistoryModel.addRow(new Object[] { sdf.format(p.getPaymentDate()), p.getAmountPaid(),
                        p.getTerm(), p.getAcademicYear() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading payment history: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        revalidate();
        repaint();
    }

    /** 
     * Displays the detailed profile of a teacher.
     * @param teacher the Teacher object whose details are to be displayed
     */
    public void displayTeacher(Teacher teacher) {
        this.currentEntity = teacher;
        clearDetails();
        paymentHistoryScrollPane.setVisible(false); // Hide student-specific panel
        recordPaymentButton.setVisible(false); // Hide payment button for teachers
        assignmentsModel.clear();

        nameLabel.setText(teacher.getName());
        setProfilePic(teacher.getProfilePic(), "libs/img/teacher_placeholder.png");

        addDetail("Teacher ID", String.valueOf(teacher.getId()));
        addDetail("Gender", teacher.getGender());
        addDetail("Contact", teacher.getContact());
        addDetail("Email", teacher.getEmail());
        addDetail("Address", teacher.getAddress());

        // Load assignments
        try {
            List<TeacherAssignment> assignments = db.getAssignmentsForTeacher(teacher.getId());
            if (assignments.isEmpty()) {
                assignmentsModel.addElement("No subjects assigned.");
            } else {
                for (TeacherAssignment assignment : assignments) {
                    assignmentsModel.addElement(
                            "Teaches " + assignment.getSubjectName() + " in Class " + dataService.getClassName(assignment.getClassLevel()));
                }
            }
        } catch (Exception ex) {
            assignmentsModel.addElement("Error loading assignments.");
        }

        assignmentsScrollPane.setVisible(true);
        revalidate();
        repaint();
    }

    /**
     * Clears all detail components from the view.
     */
    private void clearDetails() {
        for (JLabel component : detailComponents) {
            remove(component);
        }
        paymentHistoryModel.setRowCount(0);
        detailComponents.clear();
        detailGridY = 1; // Reset the detail row counter
    }

    /** 
     * Handles the edit action for the current entity.
     * @param e The action event triggering the edit
     */
    private void handleEdit(ActionEvent e) {
        if (currentEntity instanceof Student) {
            new AddStd(app.getWin(), uiManager, dataService, (Student) currentEntity, true);
        } else if (currentEntity instanceof Teacher) {
            new AddTch(app.getWin(), uiManager, dataService, (Teacher) currentEntity, true);
        }
    }

    /** 
     * Handles recording a payment for the current student.
     * @param e The action event triggering the payment recording
     */
    private void handleRecordPayment(ActionEvent e) {
        if (!(currentEntity instanceof Student student)) {
            return; // Should not happen as button is hidden, but good practice
        }

        String amountStr = JOptionPane.showInputDialog(parentFrame, "Enter payment amount:", "Record Payment",
                JOptionPane.PLAIN_MESSAGE);

        if (amountStr == null || amountStr.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new NumberFormatException("Amount must be positive.");
            }

            Payment newPayment = new Payment();
            newPayment.setStudentId(student.getId());
            newPayment.setAmountPaid(amount);
            newPayment.setPaymentDate(new Date());
            newPayment.setTerm("Term 1"); // Example value, could be enhanced with a dialog
            newPayment.setAcademicYear(Calendar.getInstance().get(Calendar.YEAR)); // Example value

            dataService.addStudentPayment(newPayment);
            JOptionPane.showMessageDialog(this, "Payment recorded successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadPaymentHistory(student.getId()); // Refresh the table
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for the amount.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            uiManager.showErrorDialog("Payment Error", "Could not record the payment.", ex);
        }
    }

    /** 
     * Sets the profile picture in the view.
     * @param imageData the image data as a byte array
     * @param placeholderPath the path to the placeholder image
     */
    private void setProfilePic(byte[] imageData, String placeholderPath) {
        // Since this view panel shows both students and teachers, we'll determine type
        // based on the placeholder
        boolean isStudent = placeholderPath != null && placeholderPath.contains("student");
        profilePicLabel.setIcon(IconUtils.getProfilePicture(imageData, 150, 150, isStudent));
    }

    /** 
     * Prints the profile of the current entity.
     * @param e The action event triggering the print
     */
    private void printProfile(ActionEvent e) {
        if (currentEntity == null) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                byte[] pdfBytes = generateProfilePdfBytes();
                try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPageable(new PDFPageable(document));
                    if (job.printDialog()) {
                        job.print();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                } catch (Exception e) {
                    // Don't show an error if the user just cancelled the print dialog
                    if (!(e.getCause() instanceof java.awt.print.PrinterException
                            && "User cancelled".equals(e.getCause().getMessage()))) {
                        e.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(ViewPanel.this,
                                "Failed to print profile: " + e.getMessage(), "Print Error",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        uiManager.startProgress(worker, "Preparing Profile", "Generating document...");
    }

    /** 
     * Generates PDF bytes for the current profile.
     * @return the PDF as a byte array
     * @throws Exception if PDF generation fails
     */
    private byte[] generateProfilePdfBytes() throws Exception {
        String html = buildProfileHtml();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, new File(".").toURI().toString());
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }

    /** 
     * Builds the HTML content for the profile.
     * @return the HTML content for the profile
     */
    private String buildProfileHtml() {
        StringBuilder sb = new StringBuilder();
        String generationDate = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());

        sb.append("<html><head><style>");
        sb.append(
                """
                            @page { size: A4; margin: 2cm; }
                            body { font-family: Arial, sans-serif; font-size: 11pt; }
                            .header { text-align: center; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
                            .profile-pic { float: right; border: 1px solid #ccc; padding: 5px; }
                            .details-table { border-collapse: collapse; width: 100%; }
                            .details-table td { padding: 8px; border-bottom: 1px solid #eee; }
                            .details-table td:first-child { font-weight: bold; width: 150px; }
                            .assignments { margin-top: 20px; }
                            .assignments-title { font-size: 14pt; border-bottom: 1px solid #ccc; padding-bottom: 5px; }
                            .assignments-list { list-style-type: disc; padding-left: 20px; }
                            .footer { text-align: center; font-size: 9pt; color: #888; position: fixed; bottom: 0; width: 100%; }
                        """);
        sb.append("</style></head><body>");

        sb.append("<div class='header'><h1>Profile Summary</h1></div>");

        if (currentEntity instanceof Student s) {
            appendImage(sb, s.getProfilePic());
            sb.append("<h2>").append(s.getFirstName()).append(" ").append(s.getLastName()).append("</h2>");
            sb.append("<table class='details-table'>");
            sb.append("<tr><td>Student ID:</td><td>").append(s.getId()).append("</td></tr>");
            sb.append("<tr><td>Gender:</td><td>").append(s.getGender()).append("</td></tr>");
            sb.append("<tr><td>Date of Birth:</td><td>").append(s.getDateOfBirth()).append("</td></tr>");
            sb.append("<tr><td>Class:</td><td>").append(dataService.getClassName(s.getLevel())).append("</td></tr>");
            sb.append("</table>");

            // Add payment history to PDF
            sb.append("<div class='assignments'>"); // Re-using 'assignments' style
            sb.append("<h3 class='assignments-title'>Payment History</h3>");
            if (paymentHistoryModel.getRowCount() == 0) {
                sb.append("<p>No payment history found.</p>");
            } else {
                sb.append(
                        "<table class='details-table'><tr><th>Date</th><th>Amount</th><th>Term</th><th>Year</th></tr>");
                for (int i = 0; i < paymentHistoryModel.getRowCount(); i++) {
                    sb.append("<tr><td>").append(paymentHistoryModel.getValueAt(i, 0)).append("</td>");
                    sb.append("<td>").append(paymentHistoryModel.getValueAt(i, 1)).append("</td>");
                    sb.append("<td>").append(paymentHistoryModel.getValueAt(i, 2)).append("</td>");
                    sb.append("<td>").append(paymentHistoryModel.getValueAt(i, 3)).append("</td></tr>");
                }
                sb.append("</table>");
            }
            sb.append("</div>");
        } else if (currentEntity instanceof Teacher t) {
            appendImage(sb, t.getProfilePic());
            sb.append("<h2>").append(t.getName()).append("</h2>");
            sb.append("<table class='details-table'>");
            sb.append("<tr><td>Teacher ID:</td><td>").append(t.getId()).append("</td></tr>");
            sb.append("<tr><td>Gender:</td><td>").append(t.getGender()).append("</td></tr>");
            sb.append("<tr><td>Contact:</td><td>").append(t.getContact()).append("</td></tr>");
            sb.append("<tr><td>Email:</td><td>").append(t.getEmail()).append("</td></tr>");
            sb.append("<tr><td>Address:</td><td>").append(t.getAddress()).append("</td></tr>");
            sb.append("</table>");

            sb.append("<div class='assignments'>");
            sb.append("<h3 class='assignments-title'>Assignments</h3>");
            sb.append("<ul class='assignments-list'>");
            for (int i = 0; i < assignmentsModel.getSize(); i++) {
                sb.append("<li>").append(assignmentsModel.getElementAt(i)).append("</li>");
            }
            sb.append("</ul></div>");
        }

        sb.append("<div class='footer'>Generated on ").append(generationDate).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /** 
     * Appends an image to the HTML profile.
     * @param sb the StringBuilder to append to
     * @param imageData the image data as a byte array
     */
    private void appendImage(StringBuilder sb, byte[] imageData) {
        String dataUri = IconUtils.imageToDataUri(imageData);
        if (!dataUri.isEmpty()) {
            sb.append("<img src='").append(dataUri).append("' class='profile-pic' width='120' />");
        }
    }
}