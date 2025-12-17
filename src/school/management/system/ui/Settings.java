package school.management.system.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import school.management.system.App;
import school.management.system.data.DataService;

/**
 * - The Settings class is responsible for displaying the settings dialog.
 * - It allows users to customize the application's appearance, table settings and colors.
 */
public class Settings {
    /** The settings dialog. */
    private final JDialog setLog;
    /** The components of the settings dialog. */
    private final JComboBox<String> themeComboBox;
    /** Table settings components. */
    private final JCheckBox showGridCheckBox;
    /** Chart settings components. */
    private final JButton gridColorButton, barColorButton, maleColorButton, femaleColorButton, lineColorButton;
    /** Selected colors for various chart elements. */
    private Color selectedGridColor, barColor, maleColor, femaleColor, lineColor;
    /** The UIManager instance for managing UI themes. */
    private final school.management.system.ui.UIManager uiManager;
    /** The DataService instance for data operations. */
    private final DataService dataService;

    /**
     * Constructor to initialize the Settings dialog and its components
     * 
     * @param parent      the parent JFrame
     * @param uiManager   the UIManager instance for managing UI themes
     * @param dataService the DataService instance for data operations
     */
    public Settings(JFrame parent, school.management.system.ui.UIManager uiManager, DataService dataService) {
        this.uiManager = uiManager;
        this.dataService = dataService;
        setLog = new JDialog(parent);
        setLog.setTitle("Settings");
        setLog.setSize(500, 600);
        setLog.setLayout(null);
        setLog.setLocationRelativeTo(parent);

        // --- Appearance Panel ---
        JPanel appearancePanel = new JPanel();
        appearancePanel.setBorder(new TitledBorder("Appearance"));
        appearancePanel.setBounds(20, 20, 440, 150);
        appearancePanel.setLayout(null);

        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(this.uiManager.fontMain(15, Font.PLAIN));
        themeLabel.setBounds(20, 30, 100, 30);
        appearancePanel.add(themeLabel);

        themeComboBox = new JComboBox<>();
        themeComboBox.setBounds(130, 30, 280, 30);
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        String currentLafClass = UIManager.getLookAndFeel().getClass().getName();
        for (UIManager.LookAndFeelInfo laf : lafs) {
            themeComboBox.addItem(laf.getName());
            if (laf.getClassName().equals(currentLafClass)) {
                themeComboBox.setSelectedItem(laf.getName());
            }
        }
        appearancePanel.add(themeComboBox);

        // --- Table Settings Panel ---
        JPanel tablePanel = new JPanel();
        tablePanel.setBorder(new TitledBorder("Table Settings"));
        tablePanel.setBounds(20, 180, 440, 150);
        tablePanel.setLayout(null);

        showGridCheckBox = new JCheckBox("Show table grid lines");
        showGridCheckBox.setFont(this.uiManager.fontMain(15, Font.PLAIN));
        showGridCheckBox.setBounds(20, 30, 200, 30);
        showGridCheckBox.setSelected(App.prefs.getBoolean("tableShowGrid", true));
        tablePanel.add(showGridCheckBox);

        JLabel gridColorLabel = new JLabel("Grid Color:");
        gridColorLabel.setFont(this.uiManager.fontMain(15, Font.PLAIN));
        gridColorLabel.setBounds(20, 80, 100, 30);
        tablePanel.add(gridColorLabel);

        gridColorButton = new JButton("Choose Color");
        gridColorButton.setBounds(130, 80, 150, 30);
        int rgb = App.prefs.getInt("tableGridColor", Color.BLACK.getRGB());
        selectedGridColor = new Color(rgb);
        gridColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(setLog, "Choose Grid Color", selectedGridColor);
            if (newColor != null) {
                selectedGridColor = newColor;
            }
        });
        tablePanel.add(gridColorButton);

        // --- Chart Settings Panel ---
        JPanel chartPanel = new JPanel();
        chartPanel.setBorder(new TitledBorder("Chart Settings"));
        chartPanel.setBounds(20, 340, 440, 150);
        chartPanel.setLayout(null);

        // Bar Chart Color
        barColorButton = createColorChooserButton(chartPanel, "Bar Chart Color:", 30, new Color(0, 150, 136));
        barColor = new Color(App.prefs.getInt("chartBarColor", barColorButton.getBackground().getRGB()));
        barColorButton.addActionListener(e -> {
            barColor = chooseColor(barColor, "Choose Bar Chart Color");
            barColorButton.setBackground(barColor);
        });

        // Pie Chart Colors
        maleColorButton = createColorChooserButton(chartPanel, "Pie Chart (Male):", 70, new Color(33, 150, 243));
        maleColor = new Color(App.prefs.getInt("chartMaleColor", maleColorButton.getBackground().getRGB()));
        maleColorButton.addActionListener(e -> {
            maleColor = chooseColor(maleColor, "Choose Male Slice Color");
            maleColorButton.setBackground(maleColor);
        });

        // Line Chart Color (Positioned correctly on the right)
        lineColorButton = createColorChooserButton(chartPanel, "Line Chart Color:", 30, new Color(255, 152, 0), 220);
        lineColor = new Color(App.prefs.getInt("chartLineColor", lineColorButton.getBackground().getRGB()));
        lineColorButton.addActionListener(e -> {
            lineColor = chooseColor(lineColor, "Choose Line Chart Color");
            lineColorButton.setBackground(lineColor);
        });

        // Pie Chart (Female) (Positioned correctly on the right)
        femaleColorButton = createColorChooserButton(chartPanel, "Pie Chart (Female):", 70, new Color(233, 30, 99),
                220);
        femaleColor = new Color(App.prefs.getInt("chartFemaleColor", femaleColorButton.getBackground().getRGB()));
        femaleColorButton.addActionListener(e -> {
            femaleColor = chooseColor(femaleColor, "Choose Female Slice Color");
            femaleColorButton.setBackground(femaleColor);
        });

        // --- Action Buttons ---
        JButton btnApply = new JButton("Apply");
        btnApply.setBounds(250, 510, 100, 30);
        btnApply.addActionListener(e -> {
            applySettings(parent);
            this.dataService.refreshTableData(); // Repaint tables with new settings
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(370, 510, 100, 30);
        btnCancel.addActionListener(e -> setLog.dispose());

        setLog.add(appearancePanel);
        setLog.add(tablePanel);
        setLog.add(chartPanel);
        setLog.add(btnApply);
        setLog.add(btnCancel);
        setLog.setVisible(true);
    }

    /**
     * Creates a color chooser button with a label.
     * 
     * @param panel        the panel to which the button and label are added
     * @param labelText    the text for the label
     * @param y            the y-coordinate for positioning
     * @param defaultColor the default color for the button
     * @return the created JButton
     */
    private JButton createColorChooserButton(JPanel panel, String labelText, int y, Color defaultColor) {
        return createColorChooserButton(panel, labelText, y, defaultColor, 10);
    }

    /**
     * Creates a color chooser button with a label.
     * 
     * @param panel        the panel to which the button and label are added
     * @param labelText    the text for the label
     * @param y            the y-coordinate for positioning
     * @param defaultColor the default color for the button
     * @param xOffset      the x-coordinate offset for positioning
     * @return the created JButton
     */
    private JButton createColorChooserButton(JPanel panel, String labelText, int y, Color defaultColor, int xOffset) {
        JLabel label = new JLabel(labelText);
        label.setBounds(xOffset, y, 150, 30);
        panel.add(label);

        JButton button = new JButton();
        button.setBounds(xOffset + 130, y, 50, 30);
        button.setBackground(defaultColor);
        panel.add(button);
        return button;
    }

    /**
     * Opens a color chooser dialog and returns the selected color.
     * 
     * @param initialColor the initial color to display in the chooser dialog
     * @param title the title of the color chooser dialog
     * @return the selected color, or the initial color if no selection is made
     */
    private Color chooseColor(Color initialColor, String title) {
        Color newColor = JColorChooser.showDialog(setLog, title, initialColor);
        return newColor != null ? newColor : initialColor;
    }

    /**
     * Applies the selected settings to the application.
     * 
     * @param parent the parent frame for UI updates
     */
    private void applySettings(JFrame parent) {
        // Apply Look and Feel
        String selectedLafName = (String) themeComboBox.getSelectedItem();
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            if (laf.getName().equals(selectedLafName)) {
                try {
                    UIManager.setLookAndFeel(laf.getClassName());
                    SwingUtilities.updateComponentTreeUI(parent);
                    SwingUtilities.updateComponentTreeUI(setLog);
                    App.prefs.put("lookAndFeel", laf.getClassName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Failed to apply theme: " + laf.getName(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }

        // Apply Table Settings
        boolean showGrid = showGridCheckBox.isSelected();
        UIManager.put("Table.showGrid", showGrid);
        App.prefs.putBoolean("tableShowGrid", showGrid);

        UIManager.put("Table.gridColor", selectedGridColor);
        App.prefs.putInt("tableGridColor", selectedGridColor.getRGB());

        // Apply Chart Settings
        App.prefs.putInt("chartBarColor", barColor.getRGB());
        App.prefs.putInt("chartMaleColor", maleColor.getRGB());
        App.prefs.putInt("chartFemaleColor", femaleColor.getRGB());
        App.prefs.putInt("chartLineColor", lineColor.getRGB());
    }
}