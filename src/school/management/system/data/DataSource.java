package school.management.system.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import school.management.system.App;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the database connection pool using HikariCP. This class provides a
 * single, efficient source for database connections throughout the application.
 * It supports both MySQL and SQLite, automatically falling back to SQLite if
 * MySQL is not available. Additionally, it includes functionality to migrate 
 * data from SQLite to MySQL upon first use of MySQL.
 */
@SuppressWarnings("unused")
public class DataSource {

    /** HikariCP connection pool */
    private static final HikariDataSource ds;
    /** Flag indicating whether MySQL is being used */
    private static boolean isMySql = false;

    // Static block to initialize the connection pool configuration once at
    // class loading time 
    static {
        HikariConfig config = new HikariConfig();
        try {
            // Try to configure for MySQL first
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://localhost/school");
            config.setUsername("root");
            config.setPassword("");
            // Test the connection to see if MySQL is available
            try (@SuppressWarnings("resource")
            Connection testConnection = new HikariDataSource(config).getConnection()) {
                isMySql = true;
                System.out.println("Successfully configured connection pool for MySQL.");
            }
        } catch (Exception e) {
            // If MySQL fails, fall back to SQLite
            System.out.println("MySQL connection failed, falling back to SQLite. Reason: " + e.getMessage());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:./libs/db/main.db");
            config.setUsername(null);
            config.setPassword(null);
        }

        // Common pool settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10); // Set pool size

        ds = new HikariDataSource(config);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private DataSource() {
    }

    /**
     * Gets a connection from the connection pool.
     * @return a database connection
     * @throws SQLException if a database access error occurs
    */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Closes the connection pool and releases all resources.
     */
    public static void close() {
        if (ds != null) {
            ds.close();
        }
    }

    /**
     * Checks if the application is configured to use MySQL as the database.
     * @return {@code true} if using MySQL, {@code false} if using SQLite
     */
    public static boolean isUsingMySql() {
        return isMySql;
    }

    /**
     * Migrates data from the local SQLite database to the MySQL database. This
     * method is intended to be called once upon application startup if MySQL is
     * detected and the migration hasn't been performed yet.
     */
    public static void performMigrationIfNeeded() {
        if (!isUsingMySql() || App.prefs.getBoolean("mysql_migrated", false)) {
            return; // Not using MySQL or already migrated
        }

        IO.println("Checking if data migration to MySQL is needed...");

        try (Connection mysqlCon = getConnection(); Connection sqliteCon = DB.connect()) {

            // Check if a key table in MySQL is empty
            try (Statement stmt = mysqlCon.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("MySQL database is not empty. Skipping migration.");
                    App.prefs.putBoolean("mysql_migrated", true); // Mark as done to avoid future checks
                    return;
                }
            }

            System.out.println("MySQL database is empty. Starting data migration from SQLite...");

            // Migrate data table by table
            migrateTable(sqliteCon, mysqlCon, "admin", 4);
            migrateTable(sqliteCon, mysqlCon, "students", 7);
            migrateTable(sqliteCon, mysqlCon, "teachers", 6);
            migrateTable(sqliteCon, mysqlCon, "student_attendance", 4);
            migrateTable(sqliteCon, mysqlCon, "student_payments", 6);

            App.prefs.putBoolean("mysql_migrated", true);
            System.out.println("Data migration to MySQL completed successfully.");

        } catch (Exception e) {
            System.err.println("Data migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migrates a single table from the source SQLite connection to the
     * destination MySQL connection.
     * @param sourceCon the source SQLite connection
     * @param destCon the destination MySQL connection
     * @param tableName the name of the table to migrate
     * @param columnCount the number of columns in the table
     * @throws SQLException if a database access error occurs
     */
    private static void migrateTable(Connection sourceCon, Connection destCon, String tableName, int columnCount)
            throws SQLException {
        System.out.println("Migrating table: " + tableName);
        String selectSql = "SELECT * FROM " + tableName;

        // Build the INSERT statement with placeholders, e.g., INSERT INTO students
        // VALUES (?, ?, ?, ?, ?, ?, ?)
        StringBuilder insertSqlBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
        for (int i = 0; i < columnCount; i++) {
            insertSqlBuilder.append("?");
            if (i < columnCount - 1) {
                insertSqlBuilder.append(",");
            }
        }
        insertSqlBuilder.append(")");
        String insertSql = insertSqlBuilder.toString();

        try (Statement sourceStmt = sourceCon.createStatement();
                ResultSet rs = sourceStmt.executeQuery(selectSql);
                PreparedStatement destPstmt = destCon.prepareStatement(insertSql)) {

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    destPstmt.setObject(i, rs.getObject(i));
                }
                destPstmt.addBatch();
            }
            destPstmt.executeBatch();
        }
    }
}