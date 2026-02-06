package me.axeno.hommr.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
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
        }
    }

    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (Exception e) {
                Hommr.getInstance().getLogger().log(java.util.logging.Level.WARNING, "Error closing database connection", e);
            }
        }
    }

    public List<Home> getAllHomes() throws SQLException {
        return homeDao.queryForAll();
    }

    public void saveAllHomes(List<Home> homes) throws SQLException {
        TableUtils.clearTable(connectionSource, Home.class);
        try {
            homeDao.callBatchTasks((Callable<Void>) () -> {
                for (Home home : homes) {
                    homeDao.create(home);
                }
                return null;
            });
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Error saving all homes", e);
        }
    }
}
