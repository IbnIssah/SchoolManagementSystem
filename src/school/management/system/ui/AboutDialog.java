package school.management.system.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;

import school.management.system.util.IconUtils;

/**
 * A dialog that displays information about the application.
 * 
 * @since 1.0
 * @author Ibn Issah
 */
public class AboutDialog extends JDialog {

    /**
     * Creates an AboutDialog with the specified parent frame and icon.
     * 
     * @param parent the parent frame
     * @param icon   the icon image to display
     */
    public AboutDialog(JFrame parent, Image icon) {
        super(parent, "About School Management System", true);
        setSize(450, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        setIconImage(parent.getIconImage());

        // Icon and Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel iconLabel = new JLabel(IconUtils.loadScaledImage(icon, 64, 64));
        JLabel titleLabel = new JLabel("School Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Information Panel using JEditorPane for HTML content
        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setOpaque(false); // Make it look like a label
        String imgPath = IconUtils.getImageFileUri("me.jpg");
        // Use HTML to format the content
        infoPane.setText(String.format("""
                <html>\
                <body style='font-family: Arial; font-size: 12px; text-align: center;'>\
                <b>Version:</b> 1.0.0<br><br>\
                A simple application to manage students, teachers, and school-related data.<br><br>\
                <hr>\
                <h3>Author Information</h3>\
                <img src='%s' width='120' height="180">\
                <p><b>Name:</b> Ibn Issah</p>\
                <p><b>Contact:</b> <a href="tel:+233548570375">+233548570375</a></p>\
                <p><b>Email:</b> <a href="mailto:issahsaalim006@gmail.com">issahsaalim006@gmail.com</a></p>\
                </body></html>""", imgPath));
        infoPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    // Some hyperlink events (for non-URL schemes like tel:) may provide no URL object.
                    // Fall back to the event description when getURL() is null.
                    URI uri = null;
                    if (e.getURL() != null) {
                        uri = e.getURL().toURI();
                    } else {
                        String desc = e.getDescription();
                        if (desc != null) {
                            uri = new URI(desc);
                        }
                    }

                    if (uri == null) {
                        return;
                    }

                    String scheme = uri.getScheme();
                    if (scheme != null && "tel".equalsIgnoreCase(scheme)) {
                        // 'tel:' is not supported by Desktop.browse; copy to clipboard instead.
                        String phoneNumber = uri.getSchemeSpecificPart();
                        StringSelection stringSelection = new StringSelection(phoneNumber);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                        JOptionPane.showMessageDialog(this, "Phone number copied to clipboard: " + phoneNumber,
                                "Contact Copied", JOptionPane.INFORMATION_MESSAGE);
                    } else if (scheme != null && "mailto".equalsIgnoreCase(scheme)) {
                        Desktop.getDesktop().mail(uri);
                    } else {
                        // For http, https, file, etc.
                        Desktop.getDesktop().browse(uri);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(infoPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}