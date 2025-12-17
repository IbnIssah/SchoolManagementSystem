package school.management.system.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import school.management.system.data.DataService;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.Student;
import school.management.system.model.teacher.TeacherAssignment;
import school.management.system.util.IconUtils;

public class ReportsPanel extends JPanel {

    /** ComboBox to select a school class. */
    private final JComboBox<SchoolClass> classComboBox;
    /** Editor pane to display the generated report. */
    private final JEditorPane reportPane;
    /** Data service for database operations. */
    private final DataService dataService;
    /** Buttons for printing and saving as PDF. */
    private final JButton printButton, pdfButton;

    /***
     * Constructs the ReportsPanel.
     * @param uiManager the UIManager
     * @param dataService the DataService
    */
    public ReportsPanel(UIManager uiManager, DataService dataService) {
        this.dataService = dataService;
        setLayout(null);
        setBorder(new TitledBorder("Class Reports"));

        // --- Controls ---
        JLabel classLabel = new JLabel("Select Class:");
        classLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        classLabel.setBounds(20, 30, 100, 30);
        add(classLabel);

        classComboBox = new JComboBox<>();
        classComboBox.setBounds(120, 30, 100, 30);
        add(classComboBox);
        SwingUtilities.invokeLater(this::populateClassComboBox);

        JButton generateButton = new JButton("Generate Report");
        generateButton.setFont(new Font("Arial", Font.BOLD, 15));
        generateButton.setBounds(240, 30, 180, 30);
        add(generateButton);

        printButton = new JButton("Print Report");
        printButton.setFont(new Font("Arial", Font.BOLD, 15));
        printButton.setBounds(440, 30, 150, 30);
        printButton.setEnabled(false); // Initially disabled
        add(printButton);

        pdfButton = new JButton("Save as PDF");
        pdfButton.setFont(new Font("Arial", Font.BOLD, 15));
        pdfButton.setBounds(500, 420, 160, 30);
        pdfButton.setEnabled(false); // Initially disabled
        add(pdfButton);

        // --- Report Display Area ---
        reportPane = new JEditorPane();
        reportPane.setContentType("text/html"); // Allow HTML formatting
        reportPane.setEditable(false);
        reportPane.setFont(new Font("Arial", Font.PLAIN, 12));
        reportPane.setText(
                "<html><body><p style='padding:10px;'>Select a class and click 'Generate Report'.</p></body></html>");

        JScrollPane scrollPane = new JScrollPane(reportPane);
        scrollPane.setBounds(20, 80, 640, 330);
        add(scrollPane);

        // --- Action Listener ---
        generateButton.addActionListener(this::generateReport);
        printButton.addActionListener(this::printReport);
        pdfButton.addActionListener(this::saveAsPdf);
    }

    /**
     * populates the class combobox.
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
            JOptionPane.showMessageDialog(this, "Error loading class levels: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 
     * Generates a report of the selected class.
     * @param e the action event triggering report generation
     */
    private void generateReport(ActionEvent e) {
        SchoolClass selectedClass = (SchoolClass) classComboBox.getSelectedItem();
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a class.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final int classId = selectedClass.getId();
        final String className = selectedClass.getName();

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Student> students = dataService.getStudentsByClass(classId);
                List<TeacherAssignment> assignments = dataService.getAssignmentsForClass(classId);

                return buildHtmlReport(className, students, assignments);
            }

            @Override
            protected void done() {
                try {
                    String reportHtml = get();
                    reportPane.setText(reportHtml);
                    reportPane.setCaretPosition(0); // Scroll to top
                    printButton.setEnabled(true); // Enable print button on success
                    pdfButton.setEnabled(true); // Enable PDF button on success
                } catch (Exception e) {
                    reportPane.setText("<html><body><p style='color:red; padding:10px;'>Error generating report: "
                            + e.getMessage() + "</p></body></html>");
                    pdfButton.setEnabled(false);
                    printButton.setEnabled(false); // Keep it disabled on error
                }
            }
        }.execute();
    }

    // --- Helper Methods ---
    /** 
     * Prints the html report.
     * @param e the action event triggering the print operation
     */
    private void printReport(ActionEvent e) {
        try {
            // The JEditorPane has a built-in print method which shows a print dialog
            if (reportPane.print()) {
                JOptionPane.showMessageDialog(this, "Printing job has been sent to the printer.", "Printing Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing job has been cancelled.", "Printing Cancelled", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while trying to print the report.", "Print Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** 
     * Saves the report as a PDF file.
     * @param e the action event triggering the save operation
     */
    private void saveAsPdf(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as PDF");
        SchoolClass selectedClass = (SchoolClass) classComboBox.getSelectedItem();
        String defaultFileName = "Class_Report_" + (selectedClass != null ? selectedClass.getName() : "Unknown") + ".pdf";
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure the file has a .pdf extension
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
            }
            final File finalFileToSave = fileToSave;

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (OutputStream os = new FileOutputStream(finalFileToSave)) {
                        PdfRendererBuilder builder = new PdfRendererBuilder();
                        builder.useFastMode();
                        // Providing a base URI is important for resolving resources, even embedded
                        // ones.
                        builder.withHtmlContent(reportPane.getText().replaceAll("<img([^>]*)>", "<img$1 />"),
                                getLogoAsBase64());
                        builder.toStream(os).run();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        JOptionPane.showMessageDialog(ReportsPanel.this, "Report saved successfully as PDF.",
                                "PDF Saved", JOptionPane.INFORMATION_MESSAGE);
                        Desktop.getDesktop().open(finalFileToSave);
                    } catch (Throwable ex) {
                        // Unwrap the real cause from the SwingWorker's ExecutionException
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        String errorMessage = "Error saving PDF: " + cause.getMessage();
                        JOptionPane.showMessageDialog(ReportsPanel.this, errorMessage, "Error",
                                JOptionPane.ERROR_MESSAGE);
                        cause.printStackTrace();
                    }
                }
            }.execute();
        }
    };

    /** 
     * Gets the logo image as a base64 data URI.
     * @return the logo image in base64 format
     */
    private String getLogoAsBase64() {
        return IconUtils.getImageFileUri("icon.png");
    }

    /** 
     * Builds the HTML report content.
     * @param className the class
     * @param students students assigned to selected class 
     * @param assignments the teacher assignment of that class
     * @return the class report in string
     */
    private String buildHtmlReport(String className, List<Student> students, List<TeacherAssignment> assignments) {
        String logoBase64 = getLogoAsBase64();

        // Calculate summary stats
        int studentCount = students.size();
        long teacherCount = assignments.stream().map(TeacherAssignment::getTeacherId).distinct().count();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><style>");
        sb.append(
                """
                            @page {
                                size: A4;
                                margin: 1.5cm;

                                @top-left {
                                    content: element(header-logo);
                                }

                                @top-center {
                                    content: "School Management System - Class Report";
                                    font-family: Arial, sans-serif;
                                    font-size: 9pt;
                                    color: #888;
                                }

                                @bottom-right {
                                    content: "Page " counter(page) " of " counter(pages);
                                    font-family: Arial, sans-serif;
                                    font-size: 9pt;
                                    color: #888;
                                }
                            }
                            body { font-family: Arial, sans-serif; font-size: 10pt; }
                            h1 { font-size: 18pt; }
                            h2 { font-size: 14pt; margin-top: 20px; border-bottom: 1px solid #ccc; padding-bottom: 5px; }
                            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                            th, td { border: 1px solid #000; padding: 2px; text-align: left; }
                            th { background-color: #f2f2f2; }
                            .summary-box { background-color: #f0f8ff; border: 1px solid #b0e0e6; padding: 15px; margin-top: 15px; margin-bottom: 20px; border-radius: 5px; }
                            .summary-box h3 { margin-top: 0; color: #005a70; }
                            .summary-box p { margin: 5px 0; font-size: 11pt; }
                            .footer-date { text-align: center; font-size: 9pt; color: #888; margin-top: 30px; }

                            /* Zebra striping for tables */
                            tr:nth-child(even) { background-color: #f9f9f9; }
                        """);
        sb.append("</style></head><body>");

        // Define the header element that will be placed by the @page rule
        if (!logoBase64.isEmpty()) {
            // logoBase64 now contains a file: URI (e.g. file:/C:/... or file:///C:/...)
            sb.append("<div style='position: running(header-logo);'>")
                    .append(String.format("<img src='%s' width='120' height='120' />", logoBase64)).append("</div>");
        }
        sb.append("<h1>Class Report: ").append(className).append("</h1>");

        // --- Summary Section ---
        sb.append("<div class='summary-box'>");
        sb.append("<h3>Report Summary</h3>");
        sb.append("<p><b>Total Students Enrolled:</b> ").append(studentCount).append("</p>");
        sb.append("<p><b>Total Teachers Assigned:</b> ").append(teacherCount).append("</p>");
        sb.append("</div>");

        // --- Teachers and Subjects Section ---
        sb.append("<h2>Teachers & Subjects</h2>");
        if (assignments.isEmpty()) {
            sb.append("<p>No teachers or subjects assigned to this class.</p>");
        } else { // There are assignments
            sb.append("<table><tr><th>Subject</th><th>Assigned Teacher</th></tr>");
            for (TeacherAssignment assignment : assignments) {
                sb.append("<tr><td>").append(assignment.getSubjectName()).append("</td><td>")
                        .append(assignment.getTeacherName()).append("</td></tr>");
            }
            sb.append("</table>");
        }

        // --- Student List Section ---
        sb.append("<h2>Student Roster</h2>");
        if (students.isEmpty()) {
            sb.append("<p>No students enrolled in this class.</p>");
        } else {
            sb.append("<table><tr><th>ID</th><th>Full Name</th><th>Gender</th></tr>");
            for (Student student : students) {
                sb.append("<tr><td>").append(student.getId()).append("</td><td>").append(student.getFirstName())
                        .append(" ").append(student.getLastName()).append("</td><td>").append(student.getGender())
                        .append("</td></tr>");
            }
            sb.append("</table>");
        }

        // --- Footer with Generation Date ---
        String generationDate = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss").format(new Date());
        sb.append("<p class='footer-date'>Report generated on ").append(generationDate).append("</p>");

        sb.append("</body></html>");
        return sb.toString();
    }
}