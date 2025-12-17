package school.management.system.data;

import school.management.system.model.adm.Admin;
import school.management.system.model.DashboardStats;
import school.management.system.model.SchoolClass;
import school.management.system.model.student.AttendanceRecord;
import school.management.system.model.student.Payment;
import school.management.system.model.student.Student;
import school.management.system.model.subject.Subject;
import school.management.system.model.teacher.Teacher;
import school.management.system.model.teacher.TeacherAssignment;
import school.management.system.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles database operations such as connecting, querying, inserting, and
 * updating records.
 * 
 * @author Ibn Issah
 */
public class DB {

    /**
     * Establishes a connection to the SQLite database. If the database file does
     * not exist, it will be created.<br>
     * 
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     * @see DataSource#getConnection()
     */
    public static Connection connect() throws SQLException {
        // This method is now only for direct SQLite connection during migration
        return DriverManager.getConnection("jdbc:sqlite:./libs/db/main.db");
    }

    /**
     * Creates the database main.db if it does not exist
     * 
     * @throws SQLException if a database access error occurs.
     */
    public static void setup() throws SQLException {
        try (Connection conn = DataSource.getConnection()) {
            if (conn != null) {
                // First, check if we need to add the profile_pic column
                migrateProfilePicColumns(conn);

                Statement stmt = conn.createStatement();
                String studentSql = """
                        CREATE TABLE IF NOT EXISTS students (
                          std_id integer PRIMARY KEY,
                          std_fname text NOT NULL,
                          std_mname text,
                          std_lname text NOT NULL,
                          std_gender text NOT NULL,
                          std_dob text,
                          std_class integer,
                          profile_pic blob
                        );
                        """;
                String adminSql = """
                        CREATE TABLE IF NOT EXISTS admin (
                          adm_id integer PRIMARY KEY,
                          adm_name text NOT NULL,
                          adm_username text NOT NULL,
                          password varchar(255) NOT NULL
                        );
                        """;
                String teacherSql = """
                        CREATE TABLE IF NOT EXISTS teachers (
                          tch_id integer PRIMARY KEY,
                          tch_name text NOT NULL,
                          tch_contact text,
                          tch_gender text NOT NULL,
                          tch_email text,
                          tch_address text,
                          profile_pic blob
                        );
                        """;
                String subjectsSql = """
                        CREATE TABLE IF NOT EXISTS subjects (
                          subject_id integer PRIMARY KEY,
                          subject_name text NOT NULL UNIQUE
                        );
                        """;
                String teacherAssignmentsSql = """
                        CREATE TABLE IF NOT EXISTS teacher_assignments (
                          assignment_id integer PRIMARY KEY,
                          teacher_id integer,
                          subject_id integer,
                          class_level integer,
                          FOREIGN KEY(teacher_id) REFERENCES teachers(tch_id),
                          FOREIGN KEY(subject_id) REFERENCES subjects(subject_id)
                        );
                        """;
                String classLevelsSql = """
                        CREATE TABLE IF NOT EXISTS class_levels (
                          class_id integer PRIMARY KEY,
                          class_name text NOT NULL UNIQUE
                        );
                        """;
                String studentAttendanceSql = """
                        CREATE TABLE IF NOT EXISTS student_attendance (
                          attendance_id integer PRIMARY KEY,
                          student_id integer,
                          attendance_date date NOT NULL,
                          status text NOT NULL, -- Present, Absent, Late
                          FOREIGN KEY(student_id) REFERENCES students(std_id)
                        );
                        """;
                String studentPaymentsSql = """
                        CREATE TABLE IF NOT EXISTS student_payments (
                          payment_id integer PRIMARY KEY,
                          student_id integer,
                          amount_paid real NOT NULL,
                          payment_date date NOT NULL,
                          term text,
                          academic_year integer,
                          FOREIGN KEY(student_id) REFERENCES students(std_id)
                        );
                        """;
                stmt.execute(studentSql);
                stmt.execute(adminSql);
                stmt.execute(teacherSql);
                stmt.execute(subjectsSql);
                stmt.execute(teacherAssignmentsSql);
                stmt.execute(classLevelsSql);
                stmt.execute(studentAttendanceSql);
                stmt.execute(studentPaymentsSql);
            }
            // After tables are ensured to exist, run migrations if needed.
            // Pass the connection to avoid creating a new one.
            migratePasswordsToHashes(conn);
        }
    }

    /**
     * Validates the user logins
     * 
     * @param admin the user to validate
     * @return {@code true} if a row is returned; {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean validateLogin(Admin admin) throws SQLException {
        String loginSql = "SELECT adm_id, adm_name, password FROM admin WHERE adm_username = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(loginSql)) {
            pstmt.setString(1, admin.getUserName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                // Verify the provided password against the stored hash
                if (PasswordUtil.checkPassword(admin.getPassword(), hashedPassword)) {
                    admin.setId(rs.getInt("adm_id"));
                    admin.setName(rs.getString("adm_name"));
                    System.out.println(admin); //for testing purposes
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Adds a new user, hashing their password before storage.
     * 
     * @param newAdmin the user to add
     * @throws SQLException if a database access error occurs.
     */
    public void signUp(Admin newAdmin) throws SQLException {
        String sql = "INSERT INTO admin(adm_name, adm_username, password) VALUES (?,?,?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, newAdmin.getName());
            stmt.setString(2, newAdmin.getUserName());
            stmt.setString(3, PasswordUtil.hashPassword(newAdmin.getPassword()));
            stmt.executeUpdate();
            System.out.println(newAdmin);
        }
    }

    /**
     * Adds a new teacher
     * 
     * @param newTeacher the teacher to add
     * @throws SQLException if a database access error occurs.
     */
    public void addTeacher(Teacher newTeacher) throws SQLException {
        String sql = "INSERT INTO teachers(tch_name, tch_contact, tch_address, tch_email, tch_gender, profile_pic) VALUES (?,?,?,?,?,?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, newTeacher.getName());
            pstmt.setString(2, newTeacher.getContact());
            pstmt.setString(3, newTeacher.getAddress());
            pstmt.setString(4, newTeacher.getEmail());
            pstmt.setString(5, newTeacher.getGender());
            pstmt.setBytes(6, newTeacher.getProfilePic());
            pstmt.executeUpdate();
            // Log the added teacher for debugging purposes
            System.out.println(newTeacher);
        }
    }

    /**
     * Adds a new student
     * 
     * @param newStudent the student to add
     * @throws SQLException if a database access error occurs.
     */
    public void addStudent(Student newStudent) throws SQLException {
        String sql = "INSERT INTO students(std_fname, std_mname, std_lname, std_gender, std_dob, std_class, profile_pic) values (?,?,?,?,?,?,?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, newStudent.getFirstName());
            pstmt.setString(2, newStudent.getMiddleName());
            pstmt.setString(3, newStudent.getLastName());
            pstmt.setString(4, newStudent.getGender());
            pstmt.setString(5, newStudent.getDateOfBirth());
            pstmt.setInt(6, newStudent.getLevel());
            pstmt.setBytes(7, newStudent.getProfilePic());
            pstmt.executeUpdate();
            // Log the added student for debugging purposes
            System.out.println(newStudent);
        }
    }

    /**
     * Inserts a list of students into the database in a single batch operation.
     * 
     * @param newStudents The list of students to add.
     * @throws SQLException if a database error occurs.
     */
    public void addStudentsBatch(List<Student> newStudents) throws SQLException {
        String sql = "INSERT INTO students(std_id, std_fname, std_mname, std_lname, std_gender, std_dob, std_class, profile_pic) values (?,?,?,?,?,?,?,?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (Student newStudent : newStudents) {
                pstmt.setInt(1, newStudent.getId());
                pstmt.setString(2, newStudent.getFirstName());
                pstmt.setString(3, newStudent.getMiddleName());
                pstmt.setString(4, newStudent.getLastName());
                pstmt.setString(5, newStudent.getGender());
                pstmt.setString(6, newStudent.getDateOfBirth());
                pstmt.setInt(7, newStudent.getLevel());
                pstmt.setBytes(8, newStudent.getProfilePic());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Inserts a list of teachers into the database in a single batch operation.
     * 
     * @param newTeachers The list of teachers to add.
     * @throws SQLException if a database error occurs.
     */
    public void addTeachersBatch(List<Teacher> newTeachers) throws SQLException {
        String sql = "INSERT INTO teachers(tch_id, tch_name, tch_contact, tch_gender, tch_address, tch_email, profile_pic) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (Teacher newTeacher : newTeachers) {
                pstmt.setInt(1, newTeacher.getId());
                pstmt.setString(2, newTeacher.getName());
                pstmt.setString(3, newTeacher.getContact());
                pstmt.setString(4, newTeacher.getGender());
                pstmt.setString(5, newTeacher.getAddress());
                pstmt.setString(6, newTeacher.getEmail());
                pstmt.setBytes(7, newTeacher.getProfilePic());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Updates an existing student's record in the database.
     * 
     * @param student The student object with updated information.
     * @throws SQLException if a database access error occurs.
     */
    public void updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET std_fname = ?, std_mname = ?, std_lname = ?, std_gender = ?, std_dob = ?, std_class = ?, profile_pic = ? WHERE std_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getMiddleName());
            pstmt.setString(3, student.getLastName());
            pstmt.setString(4, student.getGender());
            pstmt.setString(5, student.getDateOfBirth());
            pstmt.setInt(6, student.getLevel());
            pstmt.setBytes(7, student.getProfilePic());
            pstmt.setInt(8, student.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing teacher's record in the database.
     * 
     * @param teacher The teacher object with updated information.
     * @throws SQLException if a database access error occurs.
     */
    public void updateTeacher(Teacher teacher) throws SQLException {
        String sql = "UPDATE teachers SET tch_name = ?, tch_contact = ?, tch_address = ?, tch_email = ?, tch_gender = ?, profile_pic = ? WHERE tch_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setString(2, teacher.getContact());
            pstmt.setString(3, teacher.getAddress());
            pstmt.setString(4, teacher.getEmail());
            pstmt.setString(5, teacher.getGender());
            pstmt.setBytes(6, teacher.getProfilePic());
            pstmt.setInt(7, teacher.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * searches through the database for a particular student
     * 
     * @param searchTerm the term to search for
     * @param option     the search option index
     * @return a List of matching Student objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Student> searchStudent(String searchTerm, int option) throws SQLException {
        List<Student> results = new ArrayList<>();

        // Use LIKE for string searches to allow partial matches
        var sql = "SELECT * FROM students WHERE " + getColumnNameForStudent(option) + " LIKE ?";

        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapToStudent(rs));
            }
        }
        return results;
    }

    /**
     * Maps the search option to the corresponding database column name for
     * students.
     * 
     * @param option The search option index.
     * @return The corresponding column name.
     */
    private String getColumnNameForStudent(int option) {
        return switch (option) {
        case 0 -> "std_id";
        case 1 -> "std_fname";
        case 2 -> "std_lname";
        default -> throw new IllegalArgumentException("Invalid search option for student: " + option);
        };
    }

    /**
     * searches through the database for a particular teacher
     * 
     * @param searchTerm the term to search for
     * @param option     the search option index
     * @return a List of matching Teacher objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Teacher> searchTeacher(String searchTerm, int option) throws SQLException {
        List<Teacher> results = new ArrayList<>();
        String sql = "SELECT * FROM teachers WHERE " + getColumnNameForTeacher(option) + " LIKE ?";

        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapToTeacher(rs));
            }
        }
        return results;
    }

    /**
     * Maps the search option to the corresponding database column name for
     * teachers.
     * 
     * @param option The search option index.
     * @return The corresponding column name.
     */
    private String getColumnNameForTeacher(int option) {
        return switch (option) {
        case 0 -> "tch_id";
        case 1 -> "tch_name";
        case 2 -> "tch_contact";
        case 3 -> "tch_email";
        case 4 -> "tch_address";
        default -> throw new IllegalArgumentException("Invalid search option for teacher: " + option);
        };
    }

    /**
     * Fetches all students from the database.
     * 
     * @return A list of all Student objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Student> fetchAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY std_fname, std_lname";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapToStudent(rs));
            }
        }
        return students;
    }

    /**
     * Fetches all teachers from the database.
     * 
     * @return A list of all Teacher objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Teacher> fetchAllTeachers() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers ORDER BY tch_name";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement(); 
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                teachers.add(mapToTeacher(rs));
            }
        }
        return teachers;
    }

    /**
     * Fetches all subjects from the database.
     * 
     * @return A list of all Subject objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Subject> fetchAllSubjects() throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY subject_name";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("subject_id"), rs.getString("subject_name")));
            }
        }
        return subjects;
    }

    /**
     * Adds a new subject to the database.
     * 
     * @param subjectName The name of the new subject.
     * @throws SQLException if a database error occurs.
     */
    public void addSubject(String subjectName) throws SQLException {
        String sql = "INSERT INTO subjects(subject_name) VALUES (?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, subjectName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing subject's name.
     * 
     * @param subject The subject object with the updated name and correct ID.
     * @throws SQLException if a database error occurs.
     */
    public void updateSubject(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET subject_name = ? WHERE subject_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a subject from the database.
     * 
     * @param subjectId The ID of the subject to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteSubject(int subjectId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Fetches all class levels from the database.
     *
     * @return A list of all SchoolClass objects.
     * @throws SQLException if a database error occurs.
     */
    public List<SchoolClass> fetchAllClasses() throws SQLException {
        List<SchoolClass> classes = new ArrayList<>();
        String sql = "SELECT * FROM class_levels ORDER BY class_name";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                classes.add(new SchoolClass(rs.getInt("class_id"), rs.getString("class_name")));
            }
        }
        return classes;
    }

    /**
     * Adds a new class level to the database.
     *
     * @param className The name of the new class.
     * @throws SQLException if a database error occurs.
     */
    public void addClass(String className) throws SQLException {
        String sql = "INSERT INTO class_levels(class_name) VALUES (?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, className);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing class's name.
     *
     * @param schoolClass The class object with the updated name and correct ID.
     * @throws SQLException if a database error occurs.
     */
    public void updateClass(SchoolClass schoolClass) throws SQLException {
        String sql = "UPDATE class_levels SET class_name = ? WHERE class_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, schoolClass.getName());
            pstmt.setInt(2, schoolClass.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a class from the database.
     *
     * @param classId The ID of the class to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteClass(int classId) throws SQLException {
        String sql = "DELETE FROM class_levels WHERE class_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Fetches the name of a class by its ID.
     *
     * @param classId The ID of the class.
     * @return The name of the class, or null if not found.
     * @throws SQLException if a database error occurs.
     */
    public String getClassNameById(int classId) throws SQLException {
        String sql = "SELECT class_name FROM class_levels WHERE class_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("class_name") : null;
            }
        }
    }

    /**
     * Counts the number of students assigned to a specific class.
     *
     * @param classId The ID of the class.
     * @return The number of students in that class.
     * @throws SQLException if a database error occurs.
     */
    public int countStudentsInClass(int classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE std_class = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Counts the number of teacher assignments for a specific class.
     *
     * @param classId The ID of the class.
     * @return The number of assignments for that class.
     * @throws SQLException if a database error occurs.
     */
    public int countAssignmentsForClass(int classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teacher_assignments WHERE class_level = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Retrieves all teacher assignments, joining with teacher and subject tables to
     * get names.
     * 
     * @return A list of TeacherAssignment objects.
     * @throws SQLException           if a database error occurs.
     * @throws ClassNotFoundException if the JDBC driver is not found.
     */
    public List<TeacherAssignment> getTeacherAssignments() throws SQLException, ClassNotFoundException {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = """
                SELECT
                  ta.assignment_id,
                  ta.teacher_id,
                  t.tch_name,
                  ta.subject_id,
                  s.subject_name,
                  ta.class_level
                FROM
                  teacher_assignments ta
                JOIN
                  teachers t ON ta.teacher_id = t.tch_id
                JOIN
                  subjects s ON ta.subject_id = s.subject_id
                ORDER BY
                  t.tch_name, ta.class_level
                """;
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setAssignmentId(rs.getInt("assignment_id"));
                assignment.setTeacherId(rs.getInt("teacher_id"));
                assignment.setTeacherName(rs.getString("tch_name"));
                assignment.setSubjectId(rs.getInt("subject_id"));
                assignment.setSubjectName(rs.getString("subject_name"));
                assignment.setClassLevel(rs.getInt("class_level"));
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    /**
     * Adds a new teacher assignment to the database.
     * 
     * @param teacherId  The ID of the teacher.
     * @param subjectId  The ID of the subject.
     * @param classLevel The class level for the assignment.
     * @throws SQLException if a database error occurs.
     */
    public void addTeacherAssignment(int teacherId, int subjectId, int classLevel) throws SQLException {
        String sql = "INSERT INTO teacher_assignments(teacher_id, subject_id, class_level) VALUES (?, ?, ?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, subjectId);
            pstmt.setInt(3, classLevel);
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a teacher assignment from the database.
     * 
     * @param assignmentId The ID of the assignment to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteTeacherAssignment(int assignmentId) throws SQLException {
        String sql = "DELETE FROM teacher_assignments WHERE assignment_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, assignmentId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves all assignments for a specific teacher.
     *
     * @param teacherId The ID of the teacher.
     * @return A list of TeacherAssignment objects for the given teacher.
     * @throws SQLException           if a database error occurs.
     * @throws ClassNotFoundException if the JDBC driver is not found.
     */
    public List<TeacherAssignment> getAssignmentsForTeacher(int teacherId) throws SQLException, ClassNotFoundException {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = """
                SELECT
                  ta.assignment_id,
                  ta.teacher_id,
                  t.tch_name,
                  ta.subject_id,
                  s.subject_name,
                  ta.class_level
                FROM
                  teacher_assignments ta
                JOIN
                  teachers t ON ta.teacher_id = t.tch_id
                JOIN
                  subjects s ON ta.subject_id = s.subject_id
                WHERE
                  ta.teacher_id = ?
                ORDER BY
                  ta.class_level, s.subject_name
                """;
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapToTeacherAssignment(rs));
                }
            }
        }
        return assignments;
    }

    /**
     * Retrieves all assignments for a specific class level.
     *
     * @param classLevel The class level to get assignments for.
     * @return A list of TeacherAssignment objects for the given class.
     * @throws SQLException           if a database error occurs.
     * @throws ClassNotFoundException if the JDBC driver is not found.
     */
    public List<TeacherAssignment> getAssignmentsForClass(int classLevel) throws SQLException, ClassNotFoundException {
        List<TeacherAssignment> assignments = new ArrayList<>();
        String sql = """
                SELECT
                  ta.assignment_id,
                  ta.teacher_id,
                  t.tch_name,
                  ta.subject_id,
                  s.subject_name,
                  ta.class_level
                FROM
                  teacher_assignments ta
                JOIN
                  teachers t ON ta.teacher_id = t.tch_id
                JOIN
                  subjects s ON ta.subject_id = s.subject_id
                WHERE
                  ta.class_level = ?
                ORDER BY
                  s.subject_name
                """;
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classLevel);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(mapToTeacherAssignment(rs));
                }
            }
        }
        return assignments;
    }

    /**
     * Maps a row from a ResultSet to a Student object.
     * 
     * @param rs The ResultSet to map from.
     * @return A new Student object.
     * @throws SQLException if a database error occurs.
     */
    private Student mapToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("std_id"));
        student.setFirstName(rs.getString("std_fname"));
        student.setMiddleName(rs.getString("std_mname"));
        student.setLastName(rs.getString("std_lname"));
        student.setGender(rs.getString("std_gender"));
        student.setDateOfBirth(rs.getString("std_dob"));
        student.setLevel(rs.getInt("std_class"));
        student.setProfilePic(rs.getBytes("profile_pic"));
        return student;
    }

    /**
     * Maps a row from a ResultSet to a Teacher object.
     * 
     * @param rs The ResultSet to map from.
     * @return A new Teacher object.
     * @throws SQLException if a database error occurs.
     */
    private Teacher mapToTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setId(rs.getInt("tch_id"));
        teacher.setName(rs.getString("tch_name"));
        teacher.setContact(rs.getString("tch_contact"));
        teacher.setGender(rs.getString("tch_gender"));
        teacher.setAddress(rs.getString("tch_address"));
        teacher.setEmail(rs.getString("tch_email"));
        teacher.setProfilePic(rs.getBytes("profile_pic"));
        return teacher;
    }

    /**
     * Maps a row from a ResultSet to a TeacherAssignment object.
     * 
     * @param rs The ResultSet to map from.
     * @return A new TeacherAssignment object.
     * @throws SQLException if a database error occurs.
     */
    private TeacherAssignment mapToTeacherAssignment(ResultSet rs) throws SQLException {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setAssignmentId(rs.getInt("assignment_id"));
        assignment.setTeacherId(rs.getInt("teacher_id"));
        assignment.setTeacherName(rs.getString("tch_name"));
        assignment.setSubjectId(rs.getInt("subject_id"));
        assignment.setSubjectName(rs.getString("subject_name"));
        assignment.setClassLevel(rs.getInt("class_level"));
        return assignment;
    }

    /**
     * Deletes a teacher
     * 
     * @param teacher the teacher to delete
     * @throws SQLException if a database access error occurs.
     */
    public void deleteTch(Teacher teacher) throws SQLException {
        try (Connection con = DataSource.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM teachers Where tch_id = ?")) {
            stmt.setInt(1, teacher.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a student
     * 
     * @param student the student to delete
     * @throws SQLException if a database access error occurs.
     */
    public void deleteStd(Student student) throws SQLException {
        try (Connection con = DataSource.getConnection();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM students WHERE std_id = ?")) {
            stmt.setInt(1, student.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves a list of students belonging to a specific class level.
     * 
     * @param classLevel The class level to search for.
     * @return A list of Student objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Student> getStudentsByClass(int classLevel) throws SQLException {
        List<Student> results = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE std_class = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, classLevel);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapToStudent(rs));
            }
        }
        return results;
    }

    /**
     * Saves a list of student attendance records to the database. It first deletes
     * any existing records for that day to prevent duplicates.
     * 
     * @param records The list of attendance records to save.
     * @throws SQLException if a database error occurs.
     */
    public void saveAttendance(List<AttendanceRecord> records) throws SQLException {
        if (records.isEmpty()) {
            return;
        }
        String deleteSql = "DELETE FROM student_attendance WHERE student_id = ? AND attendance_date = ?";
        String insertSql = "INSERT INTO student_attendance(student_id, attendance_date, status) VALUES(?, ?, ?)";

        try (Connection con = DataSource.getConnection()) {
            con.setAutoCommit(false); // Start transaction
            try (PreparedStatement deletePstmt = con.prepareStatement(deleteSql);
                    PreparedStatement insertPstmt = con.prepareStatement(insertSql)) {

                for (AttendanceRecord record : records) {
                    // Delete existing record for the student on the same day
                    deletePstmt.setInt(1, record.getStudentId());
                    deletePstmt.setDate(2, new Date(record.getAttendanceDate().getTime()));
                    deletePstmt.addBatch();

                    // Insert new record
                    insertPstmt.setInt(1, record.getStudentId());
                    insertPstmt.setDate(2, new Date(record.getAttendanceDate().getTime()));
                    insertPstmt.setString(3, record.getStatus());
                    insertPstmt.addBatch();
                }
                deletePstmt.executeBatch();
                insertPstmt.executeBatch();
                con.commit(); // Commit transaction
            } catch (SQLException e) {
                con.rollback(); // Rollback on error
                throw e;
            }
        }
    }

    /**
     * Retrieves all payment records for a specific student.
     * 
     * @param studentId The ID of the student.
     * @return A list of Payment objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Payment> getStudentPayments(int studentId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM student_payments WHERE student_id = ? ORDER BY payment_date DESC";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setStudentId(rs.getInt("student_id"));
                payment.setAmountPaid(rs.getDouble("amount_paid"));
                payment.setPaymentDate(rs.getDate("payment_date"));
                payment.setTerm(rs.getString("term"));
                payment.setAcademicYear(rs.getInt("academic_year"));
                payments.add(payment);
            }
        }
        return payments;
    }

    /**
     * Adds a new payment record for a student.
     * 
     * @param payment The Payment object containing payment details.
     * @throws SQLException if a database error occurs.
     */
    public void addStudentPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO student_payments(student_id, amount_paid, payment_date, term, academic_year) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, payment.getStudentId());
            pstmt.setDouble(2, payment.getAmountPaid());
            pstmt.setDate(3, new java.sql.Date(payment.getPaymentDate().getTime()));
            pstmt.setString(4, payment.getTerm());
            pstmt.setInt(5, payment.getAcademicYear());
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves key statistics for the dashboard from the database.
     * 
     * @return A DashboardStats object containing the calculated statistics.
     * @throws SQLException if a database access error occurs.
     */
    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        String studentCountSql = "SELECT COUNT(*) FROM students";
        String teacherCountSql = "SELECT COUNT(*) FROM teachers";
        String feesSumSql = "SELECT SUM(amount_paid) FROM student_payments";

        try (Connection con = DataSource.getConnection(); Statement stmt = con.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(studentCountSql)) {
                if (rs.next()) {
                    stats.setTotalStudents(rs.getInt(1));
                }
            }

            try (ResultSet rs = stmt.executeQuery(teacherCountSql)) {
                if (rs.next()) {
                    stats.setTotalTeachers(rs.getInt(1));
                }
            }

            try (ResultSet rs = stmt.executeQuery(feesSumSql)) {
                if (rs.next()) {
                    stats.setTotalFeesCollected(rs.getDouble(1));
                }
            }
        }
        return stats;
    }

    /**
     * Gets the count of students for each class, using class names.
     * 
     * @return A map where the key is the class name and the value is the student
     *         count.
     * @throws SQLException if a database error occurs.
     */
    public Map<String, Integer> getStudentCountPerClassName() throws SQLException {
        Map<String, Integer> classCounts = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        String sql = """
                SELECT
                    COALESCE(cl.class_name, 'Unassigned') as className,
                    COUNT(s.std_id) as studentCount
                FROM
                    students s
                LEFT JOIN
                    class_levels cl ON s.std_class = cl.class_id
                GROUP BY
                    className
                ORDER BY
                    className
                """;
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                classCounts.put(rs.getString("className"), rs.getInt("studentCount"));
            }
        }
        return classCounts;
    }

    /**
     * Gets the distribution of students by gender.
     * 
     * @return A map where the key is the gender and the value is the student count.
     * @throws SQLException if a database error occurs.
     */
    public Map<String, Integer> getStudentGenderDistribution() throws SQLException {
        Map<String, Integer> genderCounts = new HashMap<>();
        String sql = "SELECT std_gender, COUNT(*) as count FROM students GROUP BY std_gender";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                genderCounts.put(rs.getString("std_gender"), rs.getInt("count"));
            }
        }
        return genderCounts;
    }

    /**
     * Gets the total fees collected for each month.
     * 
     * @return A map where the key is the month (YYYY-MM) and the value is the total
     *         amount.
     * @throws SQLException if a database error occurs.
     */
    public Map<String, Double> getFeesCollectedPerMonth() throws SQLException {
        Map<String, Double> monthlyFees = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        String sql = "SELECT strftime('%Y-%m', payment_date) as month, SUM(amount_paid) as total_fees FROM student_payments GROUP BY month ORDER BY month";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                monthlyFees.put(rs.getString("month"), rs.getDouble("total_fees"));
            }
        }
        return monthlyFees;
    }

    /**
     * Fetches all admins from the database, excluding their passwords.
     *
     * @return A list of all Admin objects.
     * @throws SQLException if a database error occurs.
     */
    public List<Admin> fetchAllAdmins() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT adm_id, adm_name, adm_username FROM admin ORDER BY adm_name";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("adm_id"));
                admin.setName(rs.getString("adm_name"));
                admin.setUserName(rs.getString("adm_username"));
                admins.add(admin);
            }
        }
        return admins;
    }

    /**
     * Updates an existing admin's record in the database. If the password in the
     * admin object is null or empty, it is not updated.
     *
     * @param admin The admin object with updated information.
     * @throws SQLException if a database access error occurs.
     */
    public void updateAdmin(Admin admin) throws SQLException {
        // If password is provided, update it. Otherwise, only update name and username.
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            String sql = "UPDATE admin SET adm_name = ?, adm_username = ?, password = ? WHERE adm_id = ?";
            try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, admin.getName());
                pstmt.setString(2, admin.getUserName());
                pstmt.setString(3, PasswordUtil.hashPassword(admin.getPassword()));
                pstmt.setInt(4, admin.getId());
                pstmt.executeUpdate();
            }
        } else {
            String sql = "UPDATE admin SET adm_name = ?, adm_username = ? WHERE adm_id = ?";
            try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, admin.getName());
                pstmt.setString(2, admin.getUserName());
                pstmt.setInt(3, admin.getId());
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Deletes an admin from the database.
     *
     * @param adminId The ID of the admin to delete.
     * @throws SQLException          if a database error occurs.
     * @throws IllegalStateException if attempting to delete the last admin.
     */
    public void deleteAdmin(int adminId) throws SQLException, IllegalStateException {
        // As a safeguard, don't allow deleting the last admin.
        String countSql = "SELECT COUNT(*) FROM admin";
        try (Connection con = DataSource.getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) <= 1) {
                throw new IllegalStateException("Cannot delete the last administrator.");
            }
        }

        String sql = "DELETE FROM admin WHERE adm_id = ?";
        try (Connection con = DataSource.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, adminId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Adds the profile_pic column to existing tables if it doesn't exist.
     * 
     * @param con The database connection to use.
     */
    private static void migrateProfilePicColumns(Connection con) {
        try (Statement stmt = con.createStatement()) {
            // Check if profile_pic column exists in students table
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM students LIMIT 0")) {
                boolean hasStudentProfilePic = false;
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getMetaData().getColumnName(i).equalsIgnoreCase("profile_pic")) {
                        hasStudentProfilePic = true;
                        break;
                    }
                }
                if (!hasStudentProfilePic) {
                    stmt.execute("ALTER TABLE students ADD COLUMN profile_pic blob");
                }
            }

            // Check if profile_pic column exists in teachers table
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM teachers LIMIT 0")) {
                boolean hasTeacherProfilePic = false;
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (rs.getMetaData().getColumnName(i).equalsIgnoreCase("profile_pic")) {
                        hasTeacherProfilePic = true;
                        break;
                    }
                }
                if (!hasTeacherProfilePic) {
                    stmt.execute("ALTER TABLE teachers ADD COLUMN profile_pic blob");
                }
            }
        } catch (SQLException e) {
            // Log the error but don't re-throw as this is a migration
            System.err.println("Error migrating profile_pic columns: " + e.getMessage());
        }
    }

    /**
     * Migrates any plain-text passwords in the admin table to BCrypt hashes. This
     * is a one-time operation. It identifies plain-text passwords by checking if
     * they do not start with the BCrypt prefix.
     *
     * @param con The database connection to use.
     * @throws SQLException if a database error occurs.
     */
    private static void migratePasswordsToHashes(Connection con) throws SQLException {
        Map<Integer, String> passwordsToUpdate = new HashMap<>();
        String selectSql = "SELECT adm_id, password FROM admin";

        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                String currentPassword = rs.getString("password");
                // BCrypt hashes start with $2a$, $2b$, or $2y$. If it doesn't, it's likely
                // plain text.
                if (currentPassword != null && !currentPassword.startsWith("$2")) {
                    int id = rs.getInt("adm_id");
                    String hashedPassword = PasswordUtil.hashPassword(currentPassword);
                    passwordsToUpdate.put(id, hashedPassword);
                }
            }
        }
        // Update passwords to hashed versions
        if (!passwordsToUpdate.isEmpty()) {
            System.out.println("Migrating " + passwordsToUpdate.size() + " plain-text password(s) to BCrypt hashes...");
            String updateSql = "UPDATE admin SET password = ? WHERE adm_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
                for (Map.Entry<Integer, String> entry : passwordsToUpdate.entrySet()) {
                    pstmt.setString(1, entry.getValue());
                    pstmt.setInt(2, entry.getKey());
                    pstmt.executeUpdate();
                }
            }
            System.out.println("Password migration complete.");
        }
    }
}
