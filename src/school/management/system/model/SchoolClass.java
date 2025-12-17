package school.management.system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a school class level (e.g., Class 1, Class 2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass {
    /** The unique identifier of the school class. */
    private int id;
    /** The name of the school class. */
    private String name;
}