package school.management.system.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import school.management.system.data.DataService;
import school.management.system.model.student.Payment;
import school.management.system.model.student.Student;

/**
 * <h2>FeesPanel Class</h2><br>
 * This class represents the panel for managing student fees, including
 * searching for students, viewing payment history, and recording new payments.
 * --- IGNORE ---
 */
public class FeesPanel extends JPanel {

    // --- UI Components ---
    /** Text field for entering student ID */
    private final JTextField studentIdField;
    /** Button to search for a student */
    private final JButton searchButton;
    /** Label to display the selected student's name */
    private final JLabel studentNameLabel;
    /** Table to display payment history */
    private final JTable paymentsTable;
    /** Model for the payments table */
    private final DefaultTableModel paymentsModel;
    /** Text field for entering payment amount */
    private final JTextField amountField;
    /** Button to record a new payment */
    private final JButton payButton;

    /** Data service for database operations */
    private final DataService dataService;
    /** Currently selected student */
    private Student currentStudent; // To hold the currently selected student

    /** UI Manager for utility methods */
    private final UIManager uiManager;

    /**
     * Constructor for the FeesPanel class.
     * 
     * @param uiManager   the UIManager instance for utility methods
     * @param dataService the DataService instance for data operations --- IGNORE
     *                    ---
     */
    public FeesPanel(UIManager uiManager, DataService dataService) {
        this.dataService = dataService;
        this.currentStudent = null;
        this.uiManager = uiManager;

        setLayout(null);
        setBorder(new TitledBorder("Student Fee Management"));

        // --- Student Search ---
        JLabel searchLabel = new JLabel("Student ID:");
        searchLabel.setFont(this.uiManager.fontMain(15, Font.PLAIN));
        searchLabel.setBounds(20, 30, 100, 30);
        add(searchLabel);

        studentIdField = new JTextField();
        studentIdField.setBounds(120, 30, 100, 30);
        add(studentIdField);

        searchButton = new JButton("Search");
        searchButton.setBounds(230, 30, 100, 30);
        add(searchButton);

        studentNameLabel = new JLabel("Student Name: (Not Selected)");
        studentNameLabel.setFont(uiManager.fontMain(15, Font.BOLD));
        studentNameLabel.setBounds(350, 30, 300, 30);
        add(studentNameLabel);

        // --- Payments History Table ---
        JLabel historyLabel = new JLabel("Payment History:");
        historyLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        historyLabel.setBounds(20, 70, 150, 30);
        add(historyLabel);

        String[] columnNames = { "Date", "Amount Paid", "Term", "Academic Year" };
        paymentsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentsTable = new JTable(paymentsModel);
        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBounds(20, 100, 640, 200);
        add(scrollPane);

        // --- New Payment ---
        JPanel paymentPanel = new JPanel();
        paymentPanel.setBorder(new TitledBorder("Record New Payment"));
        paymentPanel.setBounds(20, 310, 640, 100);
        paymentPanel.setLayout(null);
        add(paymentPanel);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(uiManager.fontMain(15, Font.PLAIN));
        amountLabel.setBounds(20, 30, 80, 30);
        paymentPanel.add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(100, 30, 150, 30);
        paymentPanel.add(amountField);

        payButton = new JButton("Record Payment");
        payButton.setFont(uiManager.fontMain(15, Font.BOLD));
        payButton.setBounds(450, 30, 170, 30);
        paymentPanel.add(payButton);

        addListeners();
    }

    /**
     * Adds action listeners to buttons.
     */
    private void addListeners() {
        searchButton.addActionListener(this::searchStudent);
        payButton.addActionListener(this::recordPayment);
    }

    /**
     * Searches for a student by ID and updates the UI accordingly.
     * 
     * @param e the ActionEvent triggered by the search button
     */
    private void searchStudent(ActionEvent e) {
        String studentIdText = studentIdField.getText();
        if (studentIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Student> students = dataService.searchAndReturn(studentIdText, 0); // Search by ID
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
                studentNameLabel.setText("Student Name: (Not Selected)");
                currentStudent = null;
                paymentsModel.setRowCount(0);
            } else {
                currentStudent = students.get(0);
                studentNameLabel.setText("Student Name: " + currentStudent.getFirstName() + " " + currentStudent.getLastName());
                loadPaymentHistory();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error searching for student: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the payment history for the selected student.
     */
    private void loadPaymentHistory() {
        paymentsModel.setRowCount(0);
        if (currentStudent == null)
            return;

        try {
            List<Payment> payments = dataService.getStudentPayments(currentStudent.getId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Payment p : payments) {
                paymentsModel.addRow(new Object[] { sdf.format(p.getPaymentDate()), p.getAmountPaid(), p.getTerm(),
                        p.getAcademicYear() });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading payment history: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Records a new payment for the selected student.
     * 
     * @param e the action event triggered by the pay button
     */
    private void recordPayment(ActionEvent e) {
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, "Please search for and select a student first.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double amount = Double.parseDouble(amountField.getText());
            Payment newPayment = new Payment();
            newPayment.setStudentId(currentStudent.getId());
            newPayment.setAmountPaid(amount);
            newPayment.setPaymentDate(new Date());
            newPayment.setTerm("Term 1"); // Example, this could be a dropdown
            newPayment.setAcademicYear(Calendar.getInstance().get(Calendar.YEAR)); // Example

            dataService.addStudentPayment(newPayment);
            JOptionPane.showMessageDialog(this, "Payment recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            amountField.setText("");
            loadPaymentHistory(); // Refresh the table
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error recording payment: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}