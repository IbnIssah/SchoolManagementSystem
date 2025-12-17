package school.management.system.model.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a teacher in the school management system.
 * 
 * @author Ibn Issah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
  /** The unique identifier of the teacher. */
  private int id;
  /** The name of the teacher. */
  private String name;
  /** The contact information of the teacher. */
  private String contact;
  /** The address of the teacher. */
  private String address;
  /** The gender of the teacher. */
  private String gender;
  /** The email address of the teacher. */
  private String email;
  /** The profile picture of the teacher. */
  private byte[] profilePic;
}
