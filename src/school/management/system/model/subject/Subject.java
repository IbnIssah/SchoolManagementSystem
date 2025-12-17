package school.management.system.model.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a subject in the school management system.
 * 
 * @author Ibn Issah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    /** The unique identifier of the subject. */
    private int id;
    /** The name of the subject. */
    private String name;
}