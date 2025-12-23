package school.management.system.model.adm;

import lombok.*;

/**
 * Represents an admin in the school management system.
 * 
 * @author Ibn Issah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
  /**
   * Unique identifier for the admin.
   */
  private int id;

  /**
   * Name of the admin.
   */
  private String name;

  /**
   * User name of the admin.
   */
  private String userName;

  /**
   * Password of the admin (not included in the toString method for security reasons).
   */
  @ToString.Exclude
  private String password;
}
