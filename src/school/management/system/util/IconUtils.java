package school.management.system.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Utility class for handling icons and images throughout the application.
 * Provides centralized image loading, scaling, and conversion functionality.
 */
public final class IconUtils {
    // Common image paths
    /** Directory containing image resources */
    private static final String IMG_DIR = "libs/img/";

    /** Path to the search icon */
    private static final String SEARCH_ICON_PATH = IMG_DIR + "search.png";

    /** Path to the application icon */
    private static final String APP_ICON_PATH = IMG_DIR + "icon.png";

    // Placeholder image paths
    /** Path to the student placeholder image */
    private static final String STUDENT_PLACEHOLDER_PATH = IMG_DIR + "student_placeholder.png";

    /** Path to the teacher placeholder image */
    private static final String TEACHER_PLACEHOLDER_PATH = IMG_DIR + "teacher_placeholder.png";

    /** Path to the profile image */
    private static final String PROFILE_PATH = IMG_DIR + "me.jpg";

    // Cached common icons
    /** Cached search icon */
    private static final Icon SEARCH_ICON = new ImageIcon(SEARCH_ICON_PATH);
    /** Cached application icon */
    private static ImageIcon APP_ICON = null;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private IconUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the application's search icon.
     * 
     * @return the search icon
     */
    public static Icon getSearchIcon() {
        return SEARCH_ICON;
    }

    /**
     * Gets the profile icon.
     * 
     * @return the profile icon
     */
    public static Icon getProfileIcon() {
        return new ImageIcon(PROFILE_PATH);
    }

    /**
     * Gets the main application icon, loading it if necessary.
     * 
     * @return the application icon as an ImageIcon
     */
    public static ImageIcon getAppIcon() {
        if (APP_ICON == null) {
            APP_ICON = new ImageIcon(APP_ICON_PATH);
        }
        return APP_ICON;
    }

    /**
     * Loads and scales a profile picture from byte array data. Falls back to
     * placeholder if data is null or empty.
     * 
     * @param imageData the raw image data
     * @param width     desired width
     * @param height    desired height
    * @param isStudent {@code true} if this is a student profile picture (for placeholder selection), {@code false} otherwise
     *                  for teacher
     * @return a scaled ImageIcon
     */
    public static ImageIcon getProfilePicture(byte[] imageData, int width, int height, boolean isStudent) {
        ImageIcon icon;
        if (imageData != null && imageData.length > 0) {
            icon = new ImageIcon(imageData);
        } else {
            String placeholder = isStudent ? STUDENT_PLACEHOLDER_PATH : TEACHER_PLACEHOLDER_PATH;
            icon = new ImageIcon(placeholder);
        }
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Loads and scales an image from a file path.
     * 
     * @param path   the path to the image file
     * @param width  desired width
     * @param height desired height
     * @return a scaled ImageIcon
     */
    public static ImageIcon loadScaledImage(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Scales an existing Image.
     * 
     * @param image  the source Image
     * @param width  desired width
     * @param height desired height
     * @return a scaled ImageIcon
     */
    public static ImageIcon loadScaledImage(Image image, int width, int height) {
        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Converts an image to a data URI for embedding in HTML.
     * 
     * @param imageData the raw image data
     * @return a data URI string, or empty string if conversion fails
     */
    public static String imageToDataUri(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
        }
        return "";
    }

    /**
     * Gets a file URI for an image in the libs/img directory.
     * 
     * @param filename the name of the image file
     * @return the file URI as a string, or empty string if file doesn't exist
     */
    public static String getImageFileUri(String filename) {
        try {
            File img = new File(IMG_DIR + filename);
            if (!img.exists()) {
                return "";
            }
            // Use toPath().toUri() for proper path handling
            return img.getAbsoluteFile().toPath().toUri().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Converts byte array image data to a BufferedImage.
     * 
     * @param imageData the raw image data
     * @return the BufferedImage, or {@code null} if conversion fails
     */
    public static BufferedImage byteArrayToBufferedImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}