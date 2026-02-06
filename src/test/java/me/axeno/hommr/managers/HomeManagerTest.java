package me.axeno.hommr.managers;

import me.axeno.hommr.Hommr;
import me.axeno.hommr.events.HomeDeleteEvent;
import me.axeno.hommr.events.HomeSetEvent;
import me.axeno.hommr.events.HomeTeleportEvent;
import me.axeno.hommr.models.Home;
import me.axeno.hommr.models.PlayerHomes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeManagerTest {

    @Mock
    private Hommr mockPlugin;

    @Mock
    private Player mockPlayer;

    @Mock
    private World mockWorld;

    @Mock
    private Server mockServer;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Logger mockLogger;

    private UUID testPlayerId;
    private MockedStatic<Bukkit> bukkitMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        testPlayerId = UUID.randomUUID();

        // Setup Bukkit mocks
        bukkitMockedStatic = mockStatic(Bukkit.class);
        bukkitMockedStatic.when(Bukkit::getServer).thenReturn(mockServer);
        bukkitMockedStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        bukkitMockedStatic.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

        when(mockWorld.getName()).thenReturn("world");
        when(mockPlayer.getUniqueId()).thenReturn(testPlayerId);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // Reset the HomeManager static state
        resetHomeManager();
    }

    @AfterEach
    void tearDown() {
        if (bukkitMockedStatic != null) {
            bukkitMockedStatic.close();
        }
    }

    @Test
    void testInitCreatesEmptyCache() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);

            HomeManager.init();

            Map<UUID, PlayerHomes> cache = HomeManager.getPlayerHomesCache();
            assertNotNull(cache);
            assertTrue(cache.isEmpty());
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
    void testSetHomeEventCancelPreventsCreation() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            doAnswer(invocation -> {
                HomeSetEvent event = invocation.getArgument(0);
                event.setCancelled(true);
                return null;
            }).when(mockPluginManager).callEvent(any(HomeSetEvent.class));

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);

            boolean result = HomeManager.setHome(mockPlayer, "home1", location);

            assertFalse(result);
            assertFalse(HomeManager.hasHome(testPlayerId, "home1"));
        }
    }

    @Test
    void testSetHomeUpdatesExistingHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location1 = new Location(mockWorld, 100.0, 64.0, 200.0);
            Location location2 = new Location(mockWorld, 150.0, 80.0, 250.0);

            HomeManager.setHome(mockPlayer, "home1", location1);
            HomeManager.setHome(mockPlayer, "home1", location2);

            assertEquals(1, HomeManager.getHomeCount(testPlayerId));
            Optional<Home> home = HomeManager.getHome(testPlayerId, "home1");
            assertTrue(home.isPresent());
            assertEquals(150.0, home.get().getX());
        }
    }

    @Test
    void testSetHomeUpdateFiresUpdateEvent() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location1 = new Location(mockWorld, 100.0, 64.0, 200.0);
            Location location2 = new Location(mockWorld, 150.0, 80.0, 250.0);

            HomeManager.setHome(mockPlayer, "home1", location1);

            ArgumentCaptor<HomeSetEvent> eventCaptor = ArgumentCaptor.forClass(HomeSetEvent.class);

            HomeManager.setHome(mockPlayer, "home1", location2);

            verify(mockPluginManager, times(2)).callEvent(eventCaptor.capture());
            HomeSetEvent updateEvent = eventCaptor.getAllValues().get(1);
            assertTrue(updateEvent.isUpdate());
        }
    }

    @Test
    void testSetHomeRespectsMaxHomesLimit() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            // Since getMaxHomes returns -1 (unlimited), all sets should succeed
            for (int i = 0; i < 100; i++) {
                Location location = new Location(mockWorld, i, 64.0, i);
                boolean result = HomeManager.setHome(mockPlayer, "home" + i, location);
                assertTrue(result);
            }

            assertEquals(100, HomeManager.getHomeCount(testPlayerId));
        }
    }

    @Test
    void testGetHomeReturnsCorrectHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0, 45.0f, -30.0f);
            HomeManager.setHome(mockPlayer, "home1", location);

            Optional<Home> home = HomeManager.getHome(testPlayerId, "home1");

            assertTrue(home.isPresent());
            assertEquals("home1", home.get().getName());
            assertEquals(testPlayerId, home.get().getOwner());
            assertEquals(100.0, home.get().getX());
        }
    }

    @Test
    void testGetHomeReturnsEmptyForNonexistentHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Optional<Home> home = HomeManager.getHome(testPlayerId, "nonexistent");

            assertTrue(home.isEmpty());
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

            verify(mockPluginManager, atLeast(1)).callEvent(eventCaptor.capture());
            boolean foundDeleteEvent = eventCaptor.getAllValues().stream()
                .anyMatch(e -> e instanceof HomeDeleteEvent && e.getHomeName().equals("home1"));
            assertTrue(foundDeleteEvent);
        }
    }

    @Test
    void testDeleteHomeEventCancelPreventsDeletion() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            doAnswer(invocation -> {
                Object event = invocation.getArgument(0);
                if (event instanceof HomeDeleteEvent) {
                    ((HomeDeleteEvent) event).setCancelled(true);
                }
                return null;
            }).when(mockPluginManager).callEvent(any());

            boolean result = HomeManager.deleteHome(mockPlayer, "home1");

            assertFalse(result);
            assertTrue(HomeManager.hasHome(testPlayerId, "home1"));
        }
    }

    @Test
    void testDeleteNonexistentHomeReturnsFalse() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            boolean result = HomeManager.deleteHome(mockPlayer, "nonexistent");

            assertFalse(result);
        }
    }

    @Test
    void testTeleportToHomeSucceeds() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0, 45.0f, -30.0f);
            HomeManager.setHome(mockPlayer, "home1", location);

            boolean result = HomeManager.teleportToHome(mockPlayer, "home1");

            assertTrue(result);
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(mockPlayer).teleport(locationCaptor.capture());
            Location teleportLocation = locationCaptor.getValue();
            assertEquals(100.0, teleportLocation.getX());
            assertEquals(64.0, teleportLocation.getY());
            assertEquals(200.0, teleportLocation.getZ());
        }
    }

    @Test
    void testTeleportToHomeFiresEvent() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            ArgumentCaptor<HomeTeleportEvent> eventCaptor = ArgumentCaptor.forClass(HomeTeleportEvent.class);

            HomeManager.teleportToHome(mockPlayer, "home1");

            verify(mockPluginManager, atLeast(1)).callEvent(eventCaptor.capture());
            boolean foundTeleportEvent = eventCaptor.getAllValues().stream()
                .anyMatch(e -> e instanceof HomeTeleportEvent && e.getHomeName().equals("home1"));
            assertTrue(foundTeleportEvent);
        }
    }

    @Test
    void testTeleportToHomeEventCancelPreventsTeleport() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            doAnswer(invocation -> {
                Object event = invocation.getArgument(0);
                if (event instanceof HomeTeleportEvent) {
                    ((HomeTeleportEvent) event).setCancelled(true);
                }
                return null;
            }).when(mockPluginManager).callEvent(any());

            boolean result = HomeManager.teleportToHome(mockPlayer, "home1");

            assertFalse(result);
            verify(mockPlayer, never()).teleport(any(Location.class));
        }
    }

    @Test
    void testTeleportToNonexistentHomeReturnsFalse() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            boolean result = HomeManager.teleportToHome(mockPlayer, "nonexistent");

            assertFalse(result);
            verify(mockPlayer, never()).teleport(any(Location.class));
        }
    }

    @Test
    void testTeleportToHomeWithUnloadedWorldReturnsFalse() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Location location = new Location(mockWorld, 100.0, 64.0, 200.0);
            HomeManager.setHome(mockPlayer, "home1", location);

            // Make the world return null when getting the location
            bukkitMockedStatic.when(() -> Bukkit.getWorld("world")).thenReturn(null);

            boolean result = HomeManager.teleportToHome(mockPlayer, "home1");

            assertFalse(result);
            verify(mockPlayer, never()).teleport(any(Location.class));
        }
    }

    @Test
    void testGetHomeNames() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            HomeManager.setHome(mockPlayer, "home1", new Location(mockWorld, 0, 0, 0));
            HomeManager.setHome(mockPlayer, "home2", new Location(mockWorld, 0, 0, 0));
            HomeManager.setHome(mockPlayer, "home3", new Location(mockWorld, 0, 0, 0));

            Set<String> names = HomeManager.getHomeNames(testPlayerId);

            assertEquals(3, names.size());
            assertTrue(names.contains("home1"));
            assertTrue(names.contains("home2"));
            assertTrue(names.contains("home3"));
        }
    }

    @Test
    void testGetHomeNamesReturnsEmptyForNewPlayer() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            Set<String> names = HomeManager.getHomeNames(testPlayerId);

            assertNotNull(names);
            assertTrue(names.isEmpty());
        }
    }

    @Test
    void testGetHomeCount() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            assertEquals(0, HomeManager.getHomeCount(testPlayerId));

            HomeManager.setHome(mockPlayer, "home1", new Location(mockWorld, 0, 0, 0));
            assertEquals(1, HomeManager.getHomeCount(testPlayerId));

            HomeManager.setHome(mockPlayer, "home2", new Location(mockWorld, 0, 0, 0));
            assertEquals(2, HomeManager.getHomeCount(testPlayerId));

            HomeManager.deleteHome(mockPlayer, "home1");
            assertEquals(1, HomeManager.getHomeCount(testPlayerId));
        }
    }

    @Test
    void testHasHome() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            assertFalse(HomeManager.hasHome(testPlayerId, "home1"));

            HomeManager.setHome(mockPlayer, "home1", new Location(mockWorld, 0, 0, 0));

            assertTrue(HomeManager.hasHome(testPlayerId, "home1"));
        }
    }

    @Test
    void testGetMaxHomesReturnsUnlimited() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            int maxHomes = HomeManager.getMaxHomes(mockPlayer);

            assertEquals(-1, maxHomes);
        }
    }

    @Test
    void testShutdownSavesHomes() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            DatabaseManager mockDbManager = mock(DatabaseManager.class);
            setupHommrMock(hommrMock);

            // Use reflection to set the database manager
            Field dbField = HomeManager.class.getDeclaredField("databaseManager");
            dbField.setAccessible(true);
            dbField.set(null, mockDbManager);

            Map<UUID, PlayerHomes> cache = new ConcurrentHashMap<>();
            Field cacheField = HomeManager.class.getDeclaredField("playerHomesCache");
            cacheField.setAccessible(true);
            cacheField.set(null, cache);

            PlayerHomes playerHomes = new PlayerHomes(testPlayerId);
            playerHomes.setHome("home1", createTestHome(testPlayerId, "home1"));
            cache.put(testPlayerId, playerHomes);

            HomeManager.shutdown();

            verify(mockDbManager).saveAllHomes(anyList());
            verify(mockDbManager).close();
        }
    }

    @Test
    void testMultiplePlayersIndependence() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            UUID player2Id = UUID.randomUUID();
            Player mockPlayer2 = mock(Player.class);
            when(mockPlayer2.getUniqueId()).thenReturn(player2Id);

            HomeManager.setHome(mockPlayer, "home1", new Location(mockWorld, 0, 0, 0));
            HomeManager.setHome(mockPlayer2, "home1", new Location(mockWorld, 100, 0, 100));

            assertEquals(1, HomeManager.getHomeCount(testPlayerId));
            assertEquals(1, HomeManager.getHomeCount(player2Id));

            Optional<Home> player1Home = HomeManager.getHome(testPlayerId, "home1");
            Optional<Home> player2Home = HomeManager.getHome(player2Id, "home1");

            assertTrue(player1Home.isPresent());
            assertTrue(player2Home.isPresent());
            assertNotEquals(player1Home.get().getX(), player2Home.get().getX());
        }
    }

    @Test
    void testHomeNamesCaseInsensitive() throws Exception {
        try (MockedStatic<Hommr> hommrMock = mockStatic(Hommr.class)) {
            setupHommrMock(hommrMock);
            HomeManager.init();

            HomeManager.setHome(mockPlayer, "MyHome", new Location(mockWorld, 100, 64, 200));

            assertTrue(HomeManager.hasHome(testPlayerId, "myhome"));
            assertTrue(HomeManager.hasHome(testPlayerId, "MYHOME"));
            assertTrue(HomeManager.hasHome(testPlayerId, "MyHoMe"));

            Optional<Home> home1 = HomeManager.getHome(testPlayerId, "myhome");
            Optional<Home> home2 = HomeManager.getHome(testPlayerId, "MYHOME");

            assertTrue(home1.isPresent());
            assertTrue(home2.isPresent());
            assertEquals(home1.get().getX(), home2.get().getX());
        }
    }

    // Helper methods
    private void setupHommrMock(MockedStatic<Hommr> hommrMock) throws SQLException {
        DatabaseManager mockDbManager = mock(DatabaseManager.class);
        when(mockDbManager.getAllHomes()).thenReturn(new ArrayList<>());

        hommrMock.when(Hommr::getInstance).thenReturn(mockPlugin);
        when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // Use reflection to inject mock database manager
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
        cacheField.set(null, null);

        Field dbField = HomeManager.class.getDeclaredField("databaseManager");
        dbField.setAccessible(true);
        dbField.set(null, null);
    }

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