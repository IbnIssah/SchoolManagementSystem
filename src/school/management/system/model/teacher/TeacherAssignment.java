package school.management.system.model.teacher;

import lombok.Data;

/**
 * Represents an assignment of a teacher to a subject and class level. This
 * class is used to manage the assignments of teachers to specific subjects and
 * class levels within the school management system.
 * 
 * @author Ibn Issah
 */
@Data
public class TeacherAssignment {
    /** The unique identifier of the teacher assignment. */
    private int assignmentId;
    /** The unique identifier of the teacher. */
    private int teacherId;
    /** The name of the teacher. */
    private String teacherName;
    /** The unique identifier of the subject. */
    private int subjectId;
    /** The name of the subject. */
    private String subjectName;
    /** The class level for the assignment. */
    private int classLevel;
}