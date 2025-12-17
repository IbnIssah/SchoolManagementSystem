package school.management.system.model.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a student in the school management system.
 * 
 * @author Ibn Issah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
  /** The unique identifier of the student. */
  private int id;
  /** The first name of the student. */
  private String firstName;
  /** The middle name of the student. */
  private String middleName;
  /** The last name of the student. */
  private String lastName;
  /** The gender of the student. */
  private String gender;
  /** The date of birth of the student. */
  private String dateOfBirth;
  /** The level or grade of the student. */
  private int level;
  /** The profile picture of the student. */
  private byte[] profilePic;
}