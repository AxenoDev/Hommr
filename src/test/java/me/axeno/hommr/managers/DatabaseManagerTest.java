package me.axeno.hommr.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import me.axeno.hommr.Hommr;
import me.axeno.hommr.models.Home;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {

    @Mock
    private Hommr mockPlugin;

    @Mock
    private FileConfiguration mockConfig;

    @Mock
    private Logger mockLogger;

    @TempDir
    Path tempDir;

    private DatabaseManager databaseManager;
    private MockedStatic<Hommr> hommrMockedStatic;

    @BeforeEach
    void setUp() {
        databaseManager = new DatabaseManager();

        hommrMockedStatic = mockStatic(Hommr.class);
        hommrMockedStatic.when(Hommr::getInstance).thenReturn(mockPlugin);

        when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(mockPlugin.getConfig()).thenReturn(mockConfig);
        when(mockPlugin.getLogger()).thenReturn(mockLogger);
        when(mockConfig.getString("database.type", "sqlite")).thenReturn("sqlite");
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (hommrMockedStatic != null) {
            hommrMockedStatic.close();
        }
    }

    @Test
    void testInitWithSQLiteCreatesDatabase() {
        databaseManager.init();

        assertNotNull(databaseManager.getHomeDao());
        File dbFile = new File(tempDir.toFile(), "homes.db");
        assertTrue(dbFile.exists());
    }

    @Test
    void testInitCreatesDataFolderIfNotExists() {
        File dataFolder = new File(tempDir.toFile(), "subfolder");
        when(mockPlugin.getDataFolder()).thenReturn(dataFolder);
        assertFalse(dataFolder.exists());

        databaseManager.init();

        assertTrue(dataFolder.exists());
    }

    @Test
    void testInitWithMySQLConfiguration() {
        when(mockConfig.getString("database.type", "sqlite")).thenReturn("mysql");
        when(mockConfig.getString("database.connection.url")).thenReturn("jdbc:mysql://localhost:3306/testdb");
        when(mockConfig.getString("database.connection.username")).thenReturn("testuser");
        when(mockConfig.getString("database.connection.password")).thenReturn("testpass");

        // This will fail to connect but should not throw exception
        databaseManager.init();

        // Verify that DAO was attempted to be created
        verify(mockLogger, atLeastOnce()).log(any(), anyString(), any());
    }

    @Test
    void testGetAllHomesReturnsEmptyListInitially() throws SQLException {
        databaseManager.init();

        List<Home> homes = databaseManager.getAllHomes();

        assertNotNull(homes);
        assertTrue(homes.isEmpty());
    }

    @Test
    void testSaveAndGetAllHomes() throws SQLException {
        databaseManager.init();

        List<Home> homesToSave = new ArrayList<>();
        homesToSave.add(createTestHome(UUID.randomUUID(), "home1"));
        homesToSave.add(createTestHome(UUID.randomUUID(), "home2"));
        homesToSave.add(createTestHome(UUID.randomUUID(), "home3"));

        databaseManager.saveAllHomes(homesToSave);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(3, retrievedHomes.size());
    }

    @Test
    void testSaveAllHomesClearsExistingHomes() throws SQLException {
        databaseManager.init();

        List<Home> firstBatch = new ArrayList<>();
        firstBatch.add(createTestHome(UUID.randomUUID(), "home1"));
        firstBatch.add(createTestHome(UUID.randomUUID(), "home2"));
        databaseManager.saveAllHomes(firstBatch);

        List<Home> secondBatch = new ArrayList<>();
        secondBatch.add(createTestHome(UUID.randomUUID(), "home3"));
        databaseManager.saveAllHomes(secondBatch);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals("home3", retrievedHomes.get(0).getName());
    }

    @Test
    void testSaveAllHomesWithEmptyList() throws SQLException {
        databaseManager.init();

        List<Home> homesToSave = new ArrayList<>();
        homesToSave.add(createTestHome(UUID.randomUUID(), "home1"));
        databaseManager.saveAllHomes(homesToSave);

        databaseManager.saveAllHomes(new ArrayList<>());

        List<Home> retrievedHomes = databaseManager.getAllHomes();
        assertTrue(retrievedHomes.isEmpty());
    }

    @Test
    void testSaveAllHomesPreservesHomeData() throws SQLException {
        databaseManager.init();

        UUID ownerId = UUID.randomUUID();
        Home originalHome = new Home(
            0,
            ownerId,
            "testHome",
            "world",
            100.5, 64.0, 200.5,
            45.0f, -30.0f,
            System.currentTimeMillis()
        );

        List<Home> homes = new ArrayList<>();
        homes.add(originalHome);
        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        Home retrievedHome = retrievedHomes.get(0);
        assertEquals(ownerId, retrievedHome.getOwner());
        assertEquals("testHome", retrievedHome.getName());
        assertEquals("world", retrievedHome.getWorld());
        assertEquals(100.5, retrievedHome.getX());
        assertEquals(64.0, retrievedHome.getY());
        assertEquals(200.5, retrievedHome.getZ());
        assertEquals(45.0f, retrievedHome.getYaw());
        assertEquals(-30.0f, retrievedHome.getPitch());
    }

    @Test
    void testSaveAllHomesWithMultipleHomesForSamePlayer() throws SQLException {
        databaseManager.init();

        UUID ownerId = UUID.randomUUID();
        List<Home> homes = new ArrayList<>();
        homes.add(createTestHome(ownerId, "home1"));
        homes.add(createTestHome(ownerId, "home2"));
        homes.add(createTestHome(ownerId, "home3"));

        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(3, retrievedHomes.size());
        assertTrue(retrievedHomes.stream().allMatch(h -> h.getOwner().equals(ownerId)));
    }

    @Test
    void testGetAllHomesReturnsIndependentList() throws SQLException {
        databaseManager.init();

        List<Home> homesToSave = new ArrayList<>();
        homesToSave.add(createTestHome(UUID.randomUUID(), "home1"));
        databaseManager.saveAllHomes(homesToSave);

        List<Home> homes1 = databaseManager.getAllHomes();
        List<Home> homes2 = databaseManager.getAllHomes();

        assertNotSame(homes1, homes2);
    }

    @Test
    void testCloseDoesNotThrowException() {
        databaseManager.init();

        assertDoesNotThrow(() -> databaseManager.close());
    }

    @Test
    void testCloseWithoutInitDoesNotThrowException() {
        assertDoesNotThrow(() -> databaseManager.close());
    }

    @Test
    void testMultipleCloseCalls() {
        databaseManager.init();

        assertDoesNotThrow(() -> {
            databaseManager.close();
            databaseManager.close();
        });
    }

    @Test
    void testSaveAllHomesWithNegativeCoordinates() throws SQLException {
        databaseManager.init();

        Home home = new Home(
            0,
            UUID.randomUUID(),
            "negativeHome",
            "world",
            -100.0, -64.0, -200.0,
            -90.0f, -45.0f,
            System.currentTimeMillis()
        );

        List<Home> homes = new ArrayList<>();
        homes.add(home);
        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals(-100.0, retrievedHomes.get(0).getX());
        assertEquals(-64.0, retrievedHomes.get(0).getY());
        assertEquals(-200.0, retrievedHomes.get(0).getZ());
    }

    @Test
    void testSaveAllHomesWithVeryLargeCoordinates() throws SQLException {
        databaseManager.init();

        Home home = new Home(
            0,
            UUID.randomUUID(),
            "farHome",
            "world",
            30000000.0, 256.0, 30000000.0,
            359.9f, 89.9f,
            System.currentTimeMillis()
        );

        List<Home> homes = new ArrayList<>();
        homes.add(home);
        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals(30000000.0, retrievedHomes.get(0).getX());
    }

    @Test
    void testSaveAllHomesWithDifferentWorlds() throws SQLException {
        databaseManager.init();

        List<Home> homes = new ArrayList<>();
        homes.add(createTestHomeInWorld(UUID.randomUUID(), "home1", "world"));
        homes.add(createTestHomeInWorld(UUID.randomUUID(), "home2", "world_nether"));
        homes.add(createTestHomeInWorld(UUID.randomUUID(), "home3", "world_the_end"));

        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(3, retrievedHomes.size());
        assertTrue(retrievedHomes.stream().anyMatch(h -> h.getWorld().equals("world")));
        assertTrue(retrievedHomes.stream().anyMatch(h -> h.getWorld().equals("world_nether")));
        assertTrue(retrievedHomes.stream().anyMatch(h -> h.getWorld().equals("world_the_end")));
    }

    @Test
    void testSaveAllHomesPreservesTimestamps() throws SQLException {
        databaseManager.init();

        long timestamp = System.currentTimeMillis();
        Home home = new Home(
            0,
            UUID.randomUUID(),
            "timedHome",
            "world",
            0.0, 0.0, 0.0,
            0.0f, 0.0f,
            timestamp
        );

        List<Home> homes = new ArrayList<>();
        homes.add(home);
        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals(timestamp, retrievedHomes.get(0).getCreatedAt());
    }

    @Test
    void testGetHomeDaoReturnsNonNullAfterInit() {
        databaseManager.init();

        Dao<Home, Integer> dao = databaseManager.getHomeDao();

        assertNotNull(dao);
    }

    @Test
    void testSaveAllHomesWithSpecialCharacterNames() throws SQLException {
        databaseManager.init();

        List<Home> homes = new ArrayList<>();
        homes.add(createTestHome(UUID.randomUUID(), "home_with-special.chars!"));
        homes.add(createTestHome(UUID.randomUUID(), "home with spaces"));
        homes.add(createTestHome(UUID.randomUUID(), "家"));

        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(3, retrievedHomes.size());
    }

    @Test
    void testSaveAllHomesHandlesLargeNumberOfHomes() throws SQLException {
        databaseManager.init();

        List<Home> homes = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            homes.add(createTestHome(UUID.randomUUID(), "home" + i));
        }

        databaseManager.saveAllHomes(homes);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1000, retrievedHomes.size());
    }

    @Test
    void testDatabasePersistsAcrossManagerInstances() throws SQLException {
        databaseManager.init();

        List<Home> homes = new ArrayList<>();
        homes.add(createTestHome(UUID.randomUUID(), "persistentHome"));
        databaseManager.saveAllHomes(homes);
        databaseManager.close();

        DatabaseManager newManager = new DatabaseManager();
        newManager.init();

        List<Home> retrievedHomes = newManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals("persistentHome", retrievedHomes.get(0).getName());

        newManager.close();
    }

    // Helper methods
    private Home createTestHome(UUID owner, String name) {
        return new Home(
            0,
            owner,
            name,
            "world",
            100.0, 64.0, 200.0,
            0.0f, 0.0f,
            System.currentTimeMillis()
        );
    }

    private Home createTestHomeInWorld(UUID owner, String name, String world) {
        return new Home(
            0,
            owner,
            name,
            world,
            100.0, 64.0, 200.0,
            0.0f, 0.0f,
            System.currentTimeMillis()
        );
    }
}