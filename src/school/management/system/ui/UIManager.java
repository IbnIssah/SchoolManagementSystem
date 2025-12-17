package school.management.system.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Manages common UI tasks like showing dialogs, progress bars, and creating
 * fonts.
 */
public class UIManager {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(UIManager.class.getName());
    /** The main application frame. */
    private final JFrame parentFrame;

    /**
     * Constructor for UIManager.
     * @param parentFrame the main application frame
     */
    public UIManager(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * Returns a particular font with user defined size and style.
     * @param s the size of the font
     * @param style the style of the font (e.g., Font.PLAIN, Font.BOLD
     */
    public Font fontMain(int s, int style) {
        return new Font("Arial", style, s);
    }

    /**
     * Displays a standardized error dialog and logs the exception.
     * @param title the title of the error dialog
     * @param userMessage the message to display to the user
     * @param e the exception to log
     */
    public void showErrorDialog(String title, String userMessage, Exception e) {
        LOGGER.log(Level.SEVERE, title + " - " + (e != null ? e.getMessage() : "N/A"), e);
        JOptionPane.showMessageDialog(parentFrame, userMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Starts a progress dialog synchronized with a SwingWorker process.
     * @param worker the SwingWorker task to monitor
     * @param str the string to display in the progress bar
     * @param lbl the label to display above the progress bar
     */
    public void startProgress(SwingWorker<?, ?> worker, String str, String lbl) {
        JDialog progresslog = new JDialog(this.parentFrame, "Processing...", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel progresspanel = new JPanel(new BorderLayout(3000, 20));
        JLabel progresslbl = new JLabel(lbl);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setFont(fontMain(15, Font.PLAIN));
        bar.setValue(0);
        bar.setIndeterminate(true); // Default to indeterminate
        bar.setStringPainted(true);

        progresslog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progresslog.setResizable(false);
        progresspanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progresspanel.add(progresslbl, BorderLayout.NORTH);
        progresspanel.add(bar, BorderLayout.CENTER);
        progresslog.setContentPane(progresspanel);
        progresslog.pack();
        progresslog.setLocationRelativeTo(parentFrame);

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                if (bar.isIndeterminate()) {
                    bar.setIndeterminate(false);
                }
                int progress = (Integer) evt.getNewValue();
                bar.setValue(progress);
                bar.setString(String.format("%s... %d%%", str, progress));
            }
            if (worker.isDone()) {
                progresslog.dispose();
            }
        });

        worker.execute();
        progresslog.setVisible(true);
        SwingUtilities.updateComponentTreeUI(progresslog);
    }
}