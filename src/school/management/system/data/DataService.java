package school.management.system.data;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import school.management.system.util.IconUtils;
import school.management.system.util.StringUtils;
import school.management.system.App;
import school.management.system.ui.DashboardPanel;
import school.management.system.model.DashboardStats;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.AttendanceRecord;
import school.management.system.model.student.Payment;
import school.management.system.model.student.Student;
import school.management.system.model.subject.Subject;
import school.management.system.model.teacher.Teacher;
import school.management.system.model.teacher.TeacherAssignment;
import school.management.system.ui.UIManager;

/**
 * Handles business logic for data operations like searching, deleting, and
 * fetching.
 * @author Ibn Issah
 */
public class DataService {

    /** The main application instance. */
    private final App app;
    /** The database access object. */
    private final DB db;
    /** The UI manager for handling dialogs and progress. */
    private final UIManager uiManager;

    /**
     * Constructor for DataService.
     * 
     * @param app       The main application instance.
     * @param db        The database access object.
     * @param uiManager The UI manager for handling dialogs and progress.
     */
    public DataService(App app, DB db, UIManager uiManager) {
        this.app = app;
        this.db = db;
        this.uiManager = uiManager;
    }

    /**
     * Fetches all data and refreshes the UI tables.
     */
    public void fetchData() {
        app.getSchoolPanel().getTchMod().setNumRows(0);
        app.getSchoolPanel().getStdMod().setNumRows(0);

        try {
            List<Student> students = db.fetchAllStudents();
            List<Teacher> teachers = db.fetchAllTeachers();

            int studentRowNum = 1;
            for (Student s : students) {
                // Fetch the class name using the ID from the student object
                String className = getClassName(s.getLevel());
                app.getSchoolPanel().getStdMod()
                        .addRow(new Object[] { studentRowNum++, s.getId(), s.getFirstName(), s.getMiddleName(),
                                s.getLastName(), s.getGender(), s.getDateOfBirth(), className, s.getProfilePic() });
            }

            if (students.isEmpty()) {
                app.getSchoolPanel().getStdMod().setRowCount(0);
            }

            int teacherRowNum = 1;
            for (Teacher t : teachers) {
                app.getSchoolPanel().getTchMod().addRow(new Object[] { teacherRowNum++, t.getId(), t.getName(),
                        t.getContact(), t.getGender(), t.getAddress(), t.getEmail(), t.getProfilePic() });
            }

            if (teachers.isEmpty()) {
                app.getSchoolPanel().getTchMod().setRowCount(0);
            }

            // Refresh other panels that depend on this data
            app.getSchoolPanel().getSubjectsPanel().refreshSubjects();
            app.getSchoolPanel().getAssignmentsPanel().refreshPanel();
            app.getSchoolPanel().getClassesPanel().refreshClasses();

            if (app.getSchoolPanel().getTabPane().getSelectedComponent() instanceof DashboardPanel) {
                ((DashboardPanel) app.getSchoolPanel().getTabPane().getSelectedComponent()).refreshCharts();
            }
        } catch (SQLException e) {
            uiManager.showErrorDialog("Data Fetch Error", "Could not load data from the database.", e);
        }
    }

    /**
     * Generic method to show a search dialog and display results.
     * 
     * @param itemType        The type of item being searched (e.g., "Student", "Teacher").
     * @param searchOptions   The search options to present to the user.
     * @param dbSearchFunction A function that takes the search term and selected option index,
     *                         and returns a list of results from the database.
     * @param uiUpdater       A consumer that takes the list of results and updates the UI accordingly.
     */
    public <T> void searchAndDisplay(String itemType, String[] searchOptions,
            BiFunction<String, Integer, List<T>> dbSearchFunction, Consumer<List<T>> uiUpdater) {

        JComboBox<String> options = new JComboBox<>(searchOptions);
        String searchTerm = (String) JOptionPane.showInputDialog(app.getWin(), options, "Search " + itemType + " By",
                JOptionPane.PLAIN_MESSAGE, IconUtils.getSearchIcon(), null, "");

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }

        SwingWorker<List<T>, Integer> worker = new SwingWorker<>() {
            @Override
            protected List<T> doInBackground() throws Exception {
                return dbSearchFunction.apply(searchTerm, options.getSelectedIndex());
            }

            @Override
            protected void done() {
                try {
                    List<T> results = get();
                    if (results.isEmpty()) {
                        JOptionPane.showMessageDialog(app.getWin(), itemType.toUpperCase() + " NOT FOUND",
                                "Search " + itemType, JOptionPane.ERROR_MESSAGE);
                    } else {
                        uiUpdater.accept(results);
                    }
                } catch (Exception ex) {
                    uiManager.showErrorDialog("Search Error",
                            "An error occurred while searching for the " + itemType + ".", ex);
                }
            }
        };
        uiManager.startProgress(worker, "Searching", "Searching through " + itemType.toLowerCase() + "s");
    }

    /**
     * Initiates a search for students and updates the UI with the results.
     * @param e the ActionEvent triggering the search
     */
    public void searchStudent(ActionEvent e) {
        String[] searchOptions = { "Id", "First Name", "Last Name" };

        BiFunction<String, Integer, List<Student>> dbSearchFunction = (term, option) -> {
            try {
                return db.searchStudent(term, option);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };

        Consumer<List<Student>> uiUpdater = students -> {
            app.getSchoolPanel().getStdMod().setNumRows(0);
            int rowNum = 1;

            for (Student s : students) {
                String className = getClassName(s.getLevel());
                app.getSchoolPanel().getStdMod()
                        .addRow(new Object[] { rowNum++, s.getId(), s.getFirstName(), s.getMiddleName(),
                                s.getLastName(), s.getGender(), s.getDateOfBirth(), className, s.getProfilePic() });
            }
        };

        searchAndDisplay("Student", searchOptions, dbSearchFunction, uiUpdater);
    }

    /**
     * Initiates a search for teachers and updates the UI with the results.
     * @param e the ActionEvent triggering the search
     */
    public void searchTeacher(ActionEvent e) {
        String[] searchOptions = { "Id", "Name", "Contact", "Email", "Address" };

        BiFunction<String, Integer, List<Teacher>> dbSearchFunction = (term, option) -> {
            try {
                return db.searchTeacher(term, option);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };

        Consumer<List<Teacher>> uiUpdater = teachers -> {
            app.getSchoolPanel().getTchMod().setNumRows(0);
            int rowNum = 1;
            for (Teacher t : teachers) {
                app.getSchoolPanel().getTchMod().addRow(new Object[] { rowNum++, t.getId(), t.getName(), t.getContact(),
                        t.getGender(), t.getAddress(), t.getEmail(), t.getProfilePic() });
            }
        };

        searchAndDisplay("Teacher", searchOptions, dbSearchFunction, uiUpdater);
    }

    /**
     * Refreshes the data in the main tables with a progress dialog.
     */
    public void refreshTableData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // The main work is fetching data, which is done on the EDT in `done()`.
                // This part is just for showing the progress dialog.
                return null;
            }

            @Override
            protected void done() {
                fetchData();
            }
        };
        uiManager.startProgress(worker, "Refreshing...", "Please wait");
    }

    /**
     * Deletes a record from the database after user confirmation.
     * @param itemType    The type of item being deleted (e.g., "student", "teacher").
     * @param itemName    The name or identifier of the item to display in the confirmation dialog.
     * @param deleteAction A Runnable encapsulating the deletion logic to execute upon confirmation.
     */
    public void deleteItem(String itemType, String itemName, Runnable deleteAction) {
        String capitalizedType = StringUtils.capitalize(itemType);
        int confirmation = JOptionPane.showConfirmDialog(app.getWin(),
                "Are you sure you want to delete this " + itemType + ": " + itemName + "?", "Delete " + capitalizedType,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    deleteAction.run(); // Execute the provided delete logic
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // This will throw an exception if doInBackground failed
                        JOptionPane.showMessageDialog(app.getWin(), capitalizedType + " deleted successfully.",
                                "Delete " + capitalizedType, JOptionPane.INFORMATION_MESSAGE);
                        fetchData();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        uiManager.showErrorDialog("Delete Error", "Could not delete the " + itemType + ".",
                                (Exception) cause);
                    }
                }
            };
            uiManager.startProgress(worker, "Deleting", "Deleting " + itemName);
        }
    }

    /**
     * A specific search method that returns results directly instead of updating a
     * UI component. Useful for cases like finding a student for fee payment.
     * @return List<Student>
     * @throws Exception if an error occurs
     */
    public List<Student> searchAndReturn(String searchTerm, int option) throws Exception {
        return db.searchStudent(searchTerm, option);
    }

    /** 
     * adds or updates a student in the database
     * @param student the student to add or update
     * @param isEdit true if updating an existing student, false if adding a new one
     * @throws Exception if an error occurs
     */
    public void addOrUpdateStudent(Student student, boolean isEdit) throws Exception {
        if (isEdit) {
            db.updateStudent(student);
        } else {
            db.addStudent(student);
        }
    }

    /** 
     * adds or updates a teacher in the database
     * @param teacher the teacher to add or update
     * @param isEdit true if updating an existing teacher, false if adding a new one
     * @throws Exception if an error occurs
     */
    public void addOrUpdateTeacher(Teacher teacher, boolean isEdit) throws Exception {
        if (isEdit) {
            db.updateTeacher(teacher);
        } else {
            db.addTeacher(teacher);
        }
    }

    /** 
     * deletes the specified student from the database
     * @param student the student to delete
     * @throws Exception if an error occurs
     */
    public void deleteStudent(Student student) throws Exception {
        db.deleteStd(student);
    }

    /** 
     * deletes the specified teacher from the database
     * @param teacher the teacher to delete
     * @throws Exception if an error occurs
     */
    public void deleteTeacher(Teacher teacher) throws Exception {
        db.deleteTch(teacher);
    }

    /** 
     * fetches all subjects from the database
     * @return List<Subject> 
     * @throws Exception if an error occurs
     */
    public List<Subject> fetchAllSubjects() throws Exception {
        return db.fetchAllSubjects();
    }

    /** 
     * fetches all teachers from the database
     * @return List<Teacher>
     * @throws Exception if an error occurs
     */
    public List<Teacher> fetchAllTeachers() throws Exception {
        return db.fetchAllTeachers();
    }

    /** 
     * adds a new subject to the database
     * @param subjectName the name of the subject to add
     * @throws Exception if an error occurs
     */
    public void addSubject(String subjectName) throws Exception {
        db.addSubject(subjectName);
    }

    /** 
     * updates an existing subject in the database
     * @param subject the subject to update
     * @throws Exception if an error occurs
     */
    public void updateSubject(Subject subject) throws Exception {
        db.updateSubject(subject);
    }

    /** 
     * deletes a subject from the database
     * @param subjectId the ID of the subject to delete
     * @throws Exception if an error occurs
     */
    public void deleteSubject(int subjectId) throws Exception {
        db.deleteSubject(subjectId);
    }

    /** 
     * returns a list of students in the specified class level
     * @param classLevel the class level to filter students by
     * @return List<Student>
     * @throws Exception if an error occurs
     */
    public List<Student> getStudentsByClass(int classLevel) throws Exception {
        return db.getStudentsByClass(classLevel);
    }

    /** 
     * saves attendance records to the database
     * @param records the list of attendance records to save
     * @throws Exception if an error occurs
     */
    public void saveAttendance(List<AttendanceRecord> records) throws Exception {
        db.saveAttendance(records);
    }

    /** 
     * returns a list of teacher assignments for the specified class level
     * @param classLevel the class level to filter assignments by
     * @return List<TeacherAssignment>
     * @throws Exception if an error occurs
     */
    public List<TeacherAssignment> getAssignmentsForClass(int classLevel) throws Exception {
        return db.getAssignmentsForClass(classLevel);
    }

    /** 
     * returns a list of teacher assignments for the specified teacher
     * @param teacherId the ID of the teacher to filter assignments by
     * @return List<TeacherAssignment>
     * @throws Exception if an error occurs
     */
    public List<TeacherAssignment> getAssignmentsForTeacher(int teacherId) throws Exception {
        return db.getAssignmentsForTeacher(teacherId);
    }

    /** 
     * returns a list of payments made by the specified student
     * @param studentId the ID of the student to filter payments by
     * @return List<Payment>
     * @throws Exception if an error occurs
     */
    public List<Payment> getStudentPayments(int studentId) throws Exception {
        return db.getStudentPayments(studentId);
    }

    /** 
     * adds a student payment to the database
     * @param payment the payment to add
     * @throws Exception if an error occurs
     */
    public void addStudentPayment(Payment payment) throws Exception {
        db.addStudentPayment(payment);
    }

    /** 
     * returns a list of all teacher assignments
     * @return List<TeacherAssignment>
     * @throws Exception if an error occurs
     */
    public List<TeacherAssignment> getTeacherAssignments() throws Exception {
        return db.getTeacherAssignments();
    }

    /** 
     * adds a teacher assignment to the database
     * @param teacherId the ID of the teacher
     * @param subjectId the ID of the subject
     * @param classLevel the class level
     * @throws Exception if an error occurs
     */
    public void addTeacherAssignment(int teacherId, int subjectId, int classLevel) throws Exception {
        db.addTeacherAssignment(teacherId, subjectId, classLevel);
    }

    /** 
     * deletes a teacher assignment from the database
     * @param assignmentId the ID of the teacher assignment to delete
     * @throws Exception if an error occurs
     */
    public void deleteTeacherAssignment(int assignmentId) throws Exception {
        db.deleteTeacherAssignment(assignmentId);
    }

    /** 
     * fetches all classes from the database
     * @return List<SchoolClass>
     * @throws Exception if an error occurs
     */
    public List<SchoolClass> fetchAllClasses() throws Exception {
        return db.fetchAllClasses();
    }

    /** 
     * adds a class to the database
     * @param className the name of the class to add
     * @throws Exception if an error occurs
     */
    public void addClass(String className) throws Exception {
        db.addClass(className);
    }

    /** 
     * updates a class in the database
     * @param schoolClass the class to update
     * @throws Exception if an error occurs
     */
    public void updateClass(SchoolClass schoolClass) throws Exception {
        db.updateClass(schoolClass);
    }

    /** 
     * deletes a class from the database
     * @param classId the ID of the class to delete
     * @throws Exception if an error occurs
     */
    public void deleteClass(int classId) throws Exception {
        // Check for dependencies before deleting.
        int studentCount = db.countStudentsInClass(classId);
        if (studentCount > 0) {
            throw new IllegalStateException("Cannot delete class. It is assigned to " + studentCount
                    + " student" + (studentCount > 1 ? "s" : "") + ". Please reassign them first.");
        }

        int assignmentCount = db.countAssignmentsForClass(classId);
        if (assignmentCount > 0) {
            throw new IllegalStateException("Cannot delete class. It is used in " + assignmentCount
                    + " teacher assignment" + (assignmentCount > 1 ? "s" : "") + ". Please remove the assignments first.");
        }

        db.deleteClass(classId);
    }

    /**
     * Returns a displayable class name when the stored student level is a label. If
     * the provided level string can be parsed to an integer, this delegates to the
     * existing int-based lookup. Otherwise it assumes the level string is already a
     * human-readable label and returns it (or a safe default).
     * @param level the class level string
     * @return a String of the displayable class name
     */
    public String getClassName(String level) {
        // Handle null or empty level
        if (level == null || level.trim().isEmpty()) {
            return "N/A";
        }

        String trimmed = level.trim();
        // If the level actually contains an integer id, delegate to the int lookup
        try {
            int id = Integer.parseInt(trimmed);
            return getClassName(id);
        } catch (NumberFormatException e) {
            // Not an int - treat the stored value as a label and return it directly
            return trimmed;
        }
    }

    /** 
     * returns the displayable class name for a given class ID
     * @param classId the ID of the class
     * @return a String of the displayable class name
     */
    public String getClassName(int classId) {
        try {
            String name = db.getClassNameById(classId);
            return name != null ? name : "N/A";
        } catch (SQLException e) {
            // Log the error but return a default value to avoid crashing the UI
            System.err.println("Could not fetch class name for ID " + classId + ": " + e.getMessage());
            return "Error";
        }
    }

    /** 
     * returns the class name by its ID
     * @param classId the ID of the class
     * @return a String of the class name
     * @throws Exception if an error occurs
     */
    public String getClassNameById(int classId) throws Exception {
        return db.getClassNameById(classId);
    }

    /** 
     * returns dashboard statistics
     * 
     * @return DashboardStats
     * @throws Exception if an error occurs
     */
    public DashboardStats getDashboardStats() throws Exception {
        return db.getDashboardStats();
    }

    /** 
     * gets the database access object.
     * @return the database access object
     */
    public DB getDB() {
        return db;
    }
}