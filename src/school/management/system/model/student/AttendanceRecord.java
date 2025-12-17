package school.management.system.model.student;

import lombok.Data;
import java.util.Date;

/**
 * Represents an attendance record for a student in the school management
 * system.
 * 
 * @author Ibn Issah
 */
@Data
public class AttendanceRecord {
    /** The unique identifier of the student. */
    private int studentId;
    /** The date of the attendance record. */
    private Date attendanceDate;
    /** The attendance status (e.g., present, absent). */
    private String status;
}