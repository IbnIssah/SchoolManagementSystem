package school.management.system.service;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

import school.management.system.App;
import school.management.system.data.DB;
import school.management.system.data.DataSource;
import school.management.system.model.student.Student;
import school.management.system.model.teacher.Teacher;
import school.management.system.ui.UIManager;
import school.management.system.util.StringUtils;

/**
 * Handles file-based operations like import/export and backup/restore.
 * <p>
 * This service provides functionalities to export table data to CSV files,
 * import data from CSV files, and perform backups and restores of the database.
 * </p>
 * 
 * @author Ibn Issah
 */
public class FileService {
    
    /** The parent JFrame for dialog positioning */
    private final JFrame parentFrame;
    /** The UI manager for dialogs and progress */
    private final UIManager uiManager;
    /** The database access object */
    private final DB db;
    /** The main application instance */
    private final App app;

    /**
     * Constructor for FileService.
     * @param parentFrame the parent JFrame for dialog positioning
     * @param uiManager the UI manager for dialogs and progress
     * @param db the database access object
     * @param app the main application instance
    */
    public FileService(JFrame parentFrame, UIManager uiManager, DB db, App app) {
        this.parentFrame = parentFrame;
        this.uiManager = uiManager;
        this.db = db;
        this.app = app;
    }

    /**
     * Exports the given TableModel to a CSV file.
     * @param model the TableModel to export
     * @param defaultFileName the default name for the exported file
     */
    public void exportModelToCsv(TableModel model, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as CSV");
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(StringUtils.escapeCsv(model.getColumnName(i)));
                    if (i < model.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();

                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Object obj = model.getValueAt(row, col);
                        writer.write(StringUtils.escapeCsv(obj == null ? "" : obj.toString()));
                        if (col < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(parentFrame, "Data exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                uiManager.showErrorDialog("Export Error", "An error occurred while writing the file.", ex);
            }
        }
    }

    /**
     * Imports data from a CSV file.
     * @param importType the type of data to import (e.g., "student" or "teacher")
     */
    public void importFromCsv(String importType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    int recordsAdded = 0;
                    try (BufferedReader reader = new BufferedReader(new FileReader(fileToImport))) {
                        reader.readLine(); // Skip header

                        if ("student".equals(importType)) {
                            List<Student> students = new ArrayList<>();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                                Student student = new Student();
                                student.setId(Integer.parseInt(StringUtils.unEscapeCsv(data[1])));
                                student.setFirstName(StringUtils.unEscapeCsv(data[2]));
                                student.setMiddleName(StringUtils.unEscapeCsv(data[3]));
                                student.setLastName(StringUtils.unEscapeCsv(data[4]));
                                student.setGender(StringUtils.unEscapeCsv(data[5]));
                                student.setDateOfBirth(StringUtils.unEscapeCsv(data[6]));
                                student.setLevel(Integer.parseInt(StringUtils.unEscapeCsv(data[7])));
                                students.add(student);
                            }
                            if (!students.isEmpty()) {
                                db.addStudentsBatch(students);
                                recordsAdded = students.size();
                            }
                        } else if ("teacher".equals(importType)) {
                            List<Teacher> teachers = new ArrayList<>();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                                Teacher teacher = new Teacher();
                                teacher.setId(Integer.parseInt(StringUtils.unEscapeCsv(data[1])));
                                teacher.setName(StringUtils.unEscapeCsv(data[2]));
                                teacher.setContact(StringUtils.unEscapeCsv(data[3]));
                                teacher.setGender(StringUtils.unEscapeCsv(data[4]));
                                teacher.setAddress(StringUtils.unEscapeCsv(data[5]));
                                teacher.setEmail(StringUtils.unEscapeCsv(data[6]));
                                teachers.add(teacher);
                            }
                            if (!teachers.isEmpty()) {
                                db.addTeachersBatch(teachers);
                                recordsAdded = teachers.size();
                            }
                        }
                    }
                    return recordsAdded;
                }

                @Override
                protected void done() {
                    try {
                        int recordsAdded = get();
                        JOptionPane.showMessageDialog(parentFrame, "Successfully imported " + recordsAdded + " records.", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                        app.getDataService().fetchData();
                    } catch (Exception ex) {
                        if (ex.getCause() instanceof SQLException && ((SQLException) ex.getCause()).getErrorCode() == 19) {
                            uiManager.showErrorDialog("Import Error", "Import failed. One or more records already exist in the database (ID conflict).", (Exception) ex.getCause());
                        } else {
                            uiManager.showErrorDialog("Import Error", "An error occurred during the import process.", (Exception) ex.getCause());
                        }
                    }
                }
            };
            uiManager.startProgress(worker, "Importing data...", "Please wait");
        }
    }

    /**
     * Backs up the current database to a file.
     * @param e the action event triggering the backup
    */
    public void backupDatabase(ActionEvent e) {
        if (DataSource.isUsingMySql()) {
            backupMySqlDatabase();
        } else {
            backupSqliteDatabase();
        }
    }

    /** 
     * Backs up the current SQLite database to a file.
     */
    private void backupSqliteDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save SQLite Backup");
        String defaultFileName = "school-db-backup-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".db";
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database Files", "db"));

        if (Objects.equals(fileChooser.showSaveDialog(parentFrame), JFileChooser.APPROVE_OPTION)) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                Files.copy(Paths.get("./libs/db/main.db"), fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(parentFrame, "SQLite database backup created successfully:\n" + fileToSave.toPath(), "Backup Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                uiManager.showErrorDialog("Backup Error", "Failed to create SQLite backup.", e);
            }
        }
    }

    /** 
     * Backs up the current MySQL database to a file.
     */
    private void backupMySqlDatabase() {
        // Implementation for MySQL backup would go here.
        // This often involves executing 'mysqldump' via ProcessBuilder.
        JOptionPane.showMessageDialog(parentFrame, "MySQL backup is not yet implemented in this refactoring.", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 
     * Restores the database from a backup file.
     * @param e the action event triggering the restore
     */
    public void restoreDatabase(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "Restoring from a backup will overwrite the current database and restart the application.\nAre you sure you want to continue?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        if (DataSource.isUsingMySql()) {
            restoreMySqlDatabase();
        } else {
            restoreSqliteDatabase();
        }
    }

    /** 
     * Restores the SQLite database from a backup file.
     */
    private void restoreSqliteDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select SQLite Backup to Restore");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database Files", "db"));

        if (fileChooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            try {
                DataSource.close(); // IMPORTANT: Close connection pool before overwriting file
                Files.copy(backupFile.toPath(), Paths.get("./libs/db/main.db"), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(parentFrame, "SQLite database restored successfully.\nThe application will now restart.", "Restore Successful", JOptionPane.INFORMATION_MESSAGE);
                restartApplication();
            } catch (IOException e) {
                uiManager.showErrorDialog("Restore Error", "Failed to restore SQLite database.", e);
            }
        }
    }

    /** 
     * Restores the MySQL database from a backup file.
     */
    private void restoreMySqlDatabase() {
        // Implementation for MySQL restore would go here.
        // This often involves executing 'mysql' via ProcessBuilder.
        JOptionPane.showMessageDialog(parentFrame, "MySQL restore is not yet implemented in this refactoring.", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 
     * Restarts the application.
     */
    private void restartApplication() {
        try {
            String java = System.getProperty("java.home") + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String mainClass = "school.management.system.Main";

            ArrayList<String> command = new ArrayList<>();
            command.add(java);
            command.add("-cp");
            command.add(classpath);
            command.add(mainClass);

            new ProcessBuilder(command).start();
            System.exit(0);
        } catch (IOException e) {
            uiManager.showErrorDialog("Restart Error", "Failed to restart the application.", e);
        }
    }
}