package school.management.system.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for hashing and verifying passwords using BCrypt.
 * <p>
 * This class requires the jBCrypt library. Make sure to add it to your
 * project's classpath.
 * </p>
 */
public class PasswordUtil {

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param plainPassword The password to hash.
     * @return The BCrypt-hashed password.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plain text password matches a hashed password.
     *
     * @param plainPassword  The plain text password to check.
     * @param hashedPassword The hashed password from the database.
     * @return {@code true} if the passwords match, {@code false} otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}