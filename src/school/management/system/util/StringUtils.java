package school.management.system.util;

/**
 * Contains general-purpose string utility functions. 
 * @since 1.0
 * @version 1.0
 * @author Ibn Issah
 */
public final class StringUtils {

    /**
     * to prevent instantiation of this class.
     */
    private StringUtils() {
        // Private constructor to prevent instantiation
    }

    /** 
     * Capitalizes the first letter of the given string.
     * @param str the input string
     * @return the string with the first letter capitalized
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /** 
     * Escapes special characters in a CSV value. 
     * @param value the CSV value to escape
     * @return the escaped CSV value
     */
    public static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** 
     * Unescapes special characters in a CSV value.
     * @param value the CSV value to unescape
     * @return the unescaped CSV value
     */
    public static String unEscapeCsv(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            return value.replace("\"\"", "\"");
        }
        return value;
    }
}