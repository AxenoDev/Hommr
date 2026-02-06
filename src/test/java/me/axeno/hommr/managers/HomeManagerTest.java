package me.axeno.hommr.managers;

import me.axeno.hommr.Hommr;
import me.axeno.hommr.events.HomeDeleteEvent;
import me.axeno.hommr.events.HomeSetEvent;
import me.axeno.hommr.models.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeManagerTest {

    @Mock
    private Player mockPlayer;

    @Mock
    private World mockWorld;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Hommr mockPlugin;

    @Mock
    private Logger mockLogger;

    private UUID testPlayerId;

    private MockedStatic<Bukkit> bukkitMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        testPlayerId = UUID.randomUUID();

        bukkitMockedStatic = mockStatic(Bukkit.class);
        bukkitMockedStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        bukkitMockedStatic.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

        lenient().when(mockWorld.getName()).thenReturn("world");
        lenient().when(mockPlayer.getUniqueId()).thenReturn(testPlayerId);
        lenient().when(mockPlayer.getWorld()).thenReturn(mockWorld);

        resetHomeManager();
    }

    @AfterEach
    void tearDown() {
        if (bukkitMockedStatic != null) {
            bukkitMockedStatic.close();
        }
    }

    @Test
    void testSetHomeCreatesNewHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0, 45.0f, 0.0f);

            boolean result = HomeManager.setHome(mockPlayer, "home1", location);

            assertTrue(result);
            assertTrue(HomeManager.hasHome(testPlayerId, "home1"));
            assertEquals(1, HomeManager.getHomeCount(testPlayerId));
        }
    }

    @Test
    void testSetHomeFiresEvent() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            ArgumentCaptor<HomeSetEvent> eventCaptor = ArgumentCaptor.forClass(HomeSetEvent.class);

            HomeManager.setHome(mockPlayer, "home1", location);

            verify(mockPluginManager).callEvent(eventCaptor.capture());
            HomeSetEvent event = eventCaptor.getValue();
            assertEquals(mockPlayer, event.getPlayer());
            assertEquals("home1", event.getHomeName());
            assertFalse(event.isUpdate());
        }
    }

    @Test
    void testGetHomeReturnsCorrectHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0, 45.0f, 0.0f);
            HomeManager.setHome(mockPlayer, "home1", location);

            Optional<Home> home = HomeManager.getHome(testPlayerId, "home1");

            assertTrue(home.isPresent());
            assertEquals("home1", home.get().getName());
            assertEquals(100.0, home.get().getX());
        }
    }

    @Test
    void testDeleteHomeRemovesHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            boolean result = HomeManager.deleteHome(mockPlayer, "home1");

            assertTrue(result);
            assertFalse(HomeManager.hasHome(testPlayerId, "home1"));
        }
    }

    @Test
    void testDeleteHomeFiresEvent() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            ArgumentCaptor<HomeDeleteEvent> eventCaptor = ArgumentCaptor.forClass(HomeDeleteEvent.class);

            HomeManager.deleteHome(mockPlayer, "home1");

            verify(mockPluginManager, atLeastOnce()).callEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getAllValues().stream()
                .anyMatch(e -> e.getHomeName().equals("home1")));
        }
    }

    @Test
    void testGetHomeNames() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            HomeManager.setHome(mockPlayer, "home1", new Location(mockWorld, 0, 0, 0));
            HomeManager.setHome(mockPlayer, "home2", new Location(mockWorld, 0, 0, 0));

            Set<String> homeNames = HomeManager.getHomeNames(testPlayerId);

            assertEquals(2, homeNames.size());
            assertTrue(homeNames.contains("home1"));
            assertTrue(homeNames.contains("home2"));
        }
    }

    @Test
    void testHomeNamesCaseInsensitive() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "MyHome", location);

            assertTrue(HomeManager.hasHome(testPlayerId, "myhome"));
            assertTrue(HomeManager.hasHome(testPlayerId, "MYHOME"));
            assertTrue(HomeManager.hasHome(testPlayerId, "MyHome"));
        }
    }

    // Helper methods
    private void setupHommrMock(MockedStatic<Hommr> hommrMock) throws SQLException {
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        lenient().when(mockDbManager.getAllHomes()).thenReturn(new ArrayList<>());

        hommrMock.when(Hommr::getInstance).thenReturn(mockPlugin);
        lenient().when(mockPlugin.getLogger()).thenReturn(mockLogger);

        try {
            Field dbField = HomeManager.class.getDeclaredField("databaseManager");
            dbField.setAccessible(true);
            dbField.set(null, mockDbManager);
        } catch (Exception e) {
            // Ignore
        }
    }

    private void resetHomeManager() throws Exception {
        Field cacheField = HomeManager.class.getDeclaredField("playerHomesCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, ?> cache = (Map<UUID, ?>) cacheField.get(null);
        cache.clear();

        Field dbField = HomeManager.class.getDeclaredField("databaseManager");
        dbField.setAccessible(true);
        dbField.set(null, null);
    }
}
