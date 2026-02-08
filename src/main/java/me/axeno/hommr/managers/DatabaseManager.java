package me.axeno.hommr.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import me.axeno.hommr.Hommr;
import me.axeno.hommr.models.Home;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

    private ConnectionSource connectionSource;
    @Getter
    private Dao<Home, Integer> homeDao;

    public void init(String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new IllegalStateException("Database URL is not configured. Please set 'database.connection.url' in config.yml");
        }

        if (dbUrl.startsWith("jbdc:mysql:")) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Hommr.getInstance().getSLF4JLogger().error("MySQL database driver not found. Please ensure the MySQL driver is included in the classpath.");
                throw new RuntimeException(e);
            }
        }

        try {
            connectionSource = new JdbcPooledConnectionSource(dbUrl, dbUser, dbPassword);

            homeDao = DaoManager.createDao(connectionSource, Home.class);
            if (!homeDao.isTableExists()) {
                TableUtils.createTableIfNotExists(connectionSource, Home.class);
            }

        } catch (SQLException e) {
            Hommr.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Set up the database connection and DAO for Home entities based on configuration.
     * <p>
     * Initializes the plugin data folder if necessary, creates a JDBC connection source
     * and creates a Dao<Home, Integer> for accessing Home records. Ensures the Home table
     * exists in the database; failures during folder creation, table creation, or overall
     * initialization are logged.
     */
    public void init() {
        FileConfiguration config = Hommr.getInstance().getConfig();
        String dbUrl = config.getString("database.connection.url");
        String dbUser = config.getString("database.connection.username", "");
        String dbPassword = config.getString("database.connection.password", "");

        init(dbUrl, dbUser, dbPassword);
    }

    /**
     * Closes the underlying database connection source and releases related resources.
     * <p>
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
     * <p>
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