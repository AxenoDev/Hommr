package me.axeno.hommr.managers;

import me.axeno.hommr.Hommr;
import me.axeno.hommr.models.Home;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void setUp() throws SQLException {
        // Use H2 in-memory database for testing
        databaseManager = new DatabaseManager();
        databaseManager.init("jdbc:sqlite::memory:", "", "");

        hommrMockedStatic = mockStatic(Hommr.class);
        hommrMockedStatic.when(Hommr::getInstance).thenReturn(mockPlugin);

        lenient().when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfig);
        lenient().when(mockPlugin.getLogger()).thenReturn(mockLogger);
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
        assertNotNull(databaseManager.getHomeDao());
    }

    @Test
    void testSaveAndGetAllHomes() throws SQLException {
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
        List<Home> firstBatch = new ArrayList<>();
        firstBatch.add(createTestHome(UUID.randomUUID(), "home1"));
        firstBatch.add(createTestHome(UUID.randomUUID(), "home2"));
        databaseManager.saveAllHomes(firstBatch);

        List<Home> secondBatch = new ArrayList<>();
        secondBatch.add(createTestHome(UUID.randomUUID(), "home3"));
        databaseManager.saveAllHomes(secondBatch);

        List<Home> retrievedHomes = databaseManager.getAllHomes();

        assertEquals(1, retrievedHomes.size());
        assertEquals("home3", retrievedHomes.getFirst().getName());
    }

    @Test
    void testSaveAllHomesPreservesHomeData() throws SQLException {
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
        Home retrievedHome = retrievedHomes.getFirst();
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
    void testCloseDoesNotThrowException() {
        assertDoesNotThrow(() -> databaseManager.close());
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
}

