package me.axeno.hommr.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import me.axeno.hommr.Hommr;
import me.axeno.hommr.models.Home;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class DatabaseManager {

    private ConnectionSource connectionSource;
    @Getter
    private Dao<Home, Integer> homeDao;

    /**
     * Set up the database connection and DAO for Home entities based on configuration.
     *
     * Initializes the plugin data folder if necessary, creates a JDBC connection source
     * (MySQL when `database.type` is `"mysql"`, otherwise SQLite using a `homes.db` file),
     * and creates a Dao<Home, Integer> for accessing Home records. Ensures the Home table
     * exists in the database; failures during folder creation, table creation, or overall
     * initialization are logged.
     */
    public void init() {
        try {
            File dataFolder = Hommr.getInstance().getDataFolder();
            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdirs();
                if (!created) {
                    Hommr.getInstance().getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
                }
            }

            String type = Hommr.getInstance().getConfig().getString("database.type", "sqlite").toLowerCase();
            String databaseUrl;
            String username = null;
            String password = null;

            if (type.equals("mysql")) {
                databaseUrl = Hommr.getInstance().getConfig().getString("database.connection.url");
                username = Hommr.getInstance().getConfig().getString("database.connection.username");
                password = Hommr.getInstance().getConfig().getString("database.connection.password");
                if (databaseUrl == null || databaseUrl.isEmpty()) {
                    throw new IllegalArgumentException("MySQL database URL is required but not configured. Please check your config.yml.");
                }
                connectionSource = new JdbcConnectionSource(databaseUrl, username, password);
            } else {
                databaseUrl = "jdbc:sqlite:" + new File(dataFolder, "homes.db").getAbsolutePath();
                connectionSource = new JdbcConnectionSource(databaseUrl);
            }

            homeDao = DaoManager.createDao(connectionSource, Home.class);

            try {
                TableUtils.createTableIfNotExists(connectionSource, Home.class);
            } catch (SQLException e) {
                Hommr.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to ensure database table", e);
            }

        } catch (SQLException e) {
            Hommr.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Closes the underlying database connection source and releases related resources.
     *
     * If an error occurs while closing, the exception is caught and a warning is logged. 
     */
    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
                connectionSource = null;
            } catch (Exception e) {
                Hommr.getInstance().getLogger().log(java.util.logging.Level.WARNING, "Error closing database connection", e);
            }
        }
    }

    /**
     * Retrieve all Home records from the database.
     *
     * @return a list of all Home objects
     * @throws SQLException if a database access error occurs while querying for homes
     */
    public List<Home> getAllHomes() throws SQLException {
        return homeDao.queryForAll();
    }

    /**
     * Replaces all stored Home records with the provided list.
     *
     * Clears the Home table and inserts the given homes in a single batch operation.
     *
     * @param homes the list of Home objects to persist (may be empty)
     * @throws SQLException if an error occurs while clearing the table or saving records
     */
    public void saveAllHomes(List<Home> homes) throws SQLException {
        TransactionManager.callInTransaction(connectionSource, () -> {
            TableUtils.clearTable(connectionSource, Home.class);
            for (Home home : homes) {
                homeDao.create(home);
            }
            return null;
        });
    }
}