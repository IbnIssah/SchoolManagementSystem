package school.management.system.model;

import lombok.Data;

/**
 * A data class representing key statistics for the school management dashboard,
 * including total number of students, total number of teachers, and total fees collected.
 * @author Ibn Issah
 */
@Data
public class DashboardStats {

    /** Total number of students in the school */
    private int totalStudents;

    /** Total number of teachers in the school */
    private int totalTeachers;

    /** Total fees collected from students */
    private double totalFeesCollected;
}