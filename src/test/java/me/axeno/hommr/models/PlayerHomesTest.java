package me.axeno.hommr.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerHomesTest {

    private UUID testPlayerId;
    private PlayerHomes playerHomes;

    @BeforeEach
    void setUp() {
        testPlayerId = UUID.randomUUID();
        playerHomes = new PlayerHomes(testPlayerId);
    }

    @Test
    void testConstructorInitializesCorrectly() {
        assertNotNull(playerHomes);
        assertEquals(testPlayerId, playerHomes.getPlayerId());
        assertNotNull(playerHomes.getHomes());
        assertTrue(playerHomes.getHomes().isEmpty());
        assertEquals(0, playerHomes.getHomeCount());
    }

    @Test
    void testSetHomeAddsNewHome() {
        Home home = createTestHome("home1");

        playerHomes.setHome("home1", home);

        assertEquals(1, playerHomes.getHomeCount());
        assertTrue(playerHomes.hasHome("home1"));
    }

    @Test
    void testSetHomeWithUppercaseConvertsToLowercase() {
        Home home = createTestHome("HOME1");

        playerHomes.setHome("HOME1", home);

        assertTrue(playerHomes.hasHome("home1"));
        assertTrue(playerHomes.hasHome("HOME1"));
        assertTrue(playerHomes.hasHome("HoMe1"));
        assertEquals(1, playerHomes.getHomeCount());
    }

    @Test
    void testSetHomeReplacesExistingHome() {
        Home home1 = createTestHome("home1", 100.0, 64.0, 200.0);
        Home home2 = createTestHome("home1", 150.0, 80.0, 250.0);

        playerHomes.setHome("home1", home1);
        assertEquals(1, playerHomes.getHomeCount());

        playerHomes.setHome("home1", home2);
        assertEquals(1, playerHomes.getHomeCount());

        Optional<Home> retrieved = playerHomes.getHome("home1");
        assertTrue(retrieved.isPresent());
        assertEquals(150.0, retrieved.get().getX());
    }

    @Test
    void testSetMultipleHomes() {
        Home home1 = createTestHome("home1");
        Home home2 = createTestHome("home2");
        Home home3 = createTestHome("home3");

        playerHomes.setHome("home1", home1);
        playerHomes.setHome("home2", home2);
        playerHomes.setHome("home3", home3);

        assertEquals(3, playerHomes.getHomeCount());
        assertTrue(playerHomes.hasHome("home1"));
        assertTrue(playerHomes.hasHome("home2"));
        assertTrue(playerHomes.hasHome("home3"));
    }

    @Test
    void testGetHomeReturnsCorrectHome() {
        Home home = createTestHome("myHome");
        playerHomes.setHome("myHome", home);

        Optional<Home> retrieved = playerHomes.getHome("myHome");

        assertTrue(retrieved.isPresent());
        assertEquals(home, retrieved.get());
    }

    @Test
    void testGetHomeIsCaseInsensitive() {
        Home home = createTestHome("MyHome");
        playerHomes.setHome("MyHome", home);

        assertTrue(playerHomes.getHome("myhome").isPresent());
        assertTrue(playerHomes.getHome("MYHOME").isPresent());
        assertTrue(playerHomes.getHome("MyHoMe").isPresent());
    }

    @Test
    void testGetHomeReturnsEmptyForNonexistentHome() {
        Optional<Home> retrieved = playerHomes.getHome("nonexistent");

        assertTrue(retrieved.isEmpty());
    }

    @Test
    void testRemoveHomeDeletesHome() {
        Home home = createTestHome("home1");
        playerHomes.setHome("home1", home);
        assertEquals(1, playerHomes.getHomeCount());

        boolean removed = playerHomes.removeHome("home1");

        assertTrue(removed);
        assertEquals(0, playerHomes.getHomeCount());
        assertFalse(playerHomes.hasHome("home1"));
    }

    @Test
    void testRemoveHomeIsCaseInsensitive() {
        Home home = createTestHome("MyHome");
        playerHomes.setHome("MyHome", home);

        boolean removed = playerHomes.removeHome("myhome");

        assertTrue(removed);
        assertFalse(playerHomes.hasHome("MyHome"));
    }

    @Test
    void testRemoveNonexistentHomeReturnsFalse() {
        boolean removed = playerHomes.removeHome("nonexistent");

        assertFalse(removed);
    }

    @Test
    void testRemoveHomeDoesNotAffectOtherHomes() {
        Home home1 = createTestHome("home1");
        Home home2 = createTestHome("home2");
        Home home3 = createTestHome("home3");

        playerHomes.setHome("home1", home1);
        playerHomes.setHome("home2", home2);
        playerHomes.setHome("home3", home3);

        playerHomes.removeHome("home2");

        assertEquals(2, playerHomes.getHomeCount());
        assertTrue(playerHomes.hasHome("home1"));
        assertFalse(playerHomes.hasHome("home2"));
        assertTrue(playerHomes.hasHome("home3"));
    }

    @Test
    void testGetHomeNames() {
        playerHomes.setHome("home1", createTestHome("home1"));
        playerHomes.setHome("home2", createTestHome("home2"));
        playerHomes.setHome("home3", createTestHome("home3"));

        Set<String> names = playerHomes.getHomeNames();

        assertEquals(3, names.size());
        assertTrue(names.contains("home1"));
        assertTrue(names.contains("home2"));
        assertTrue(names.contains("home3"));
    }

    @Test
    void testGetHomeNamesReturnsNewSet() {
        playerHomes.setHome("home1", createTestHome("home1"));

        Set<String> names1 = playerHomes.getHomeNames();
        Set<String> names2 = playerHomes.getHomeNames();

        assertNotSame(names1, names2);
    }

    @Test
    void testGetHomeNamesReturnsEmptySetWhenNoHomes() {
        Set<String> names = playerHomes.getHomeNames();

        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test
    void testHasHomeReturnsTrueForExistingHome() {
        playerHomes.setHome("home1", createTestHome("home1"));

        assertTrue(playerHomes.hasHome("home1"));
    }

    @Test
    void testHasHomeReturnsFalseForNonexistentHome() {
        assertFalse(playerHomes.hasHome("nonexistent"));
    }

    @Test
    void testHasHomeIsCaseInsensitive() {
        playerHomes.setHome("MyHome", createTestHome("MyHome"));

        assertTrue(playerHomes.hasHome("myhome"));
        assertTrue(playerHomes.hasHome("MYHOME"));
        assertTrue(playerHomes.hasHome("MyHoMe"));
    }

    @Test
    void testGetHomeCount() {
        assertEquals(0, playerHomes.getHomeCount());

        playerHomes.setHome("home1", createTestHome("home1"));
        assertEquals(1, playerHomes.getHomeCount());

        playerHomes.setHome("home2", createTestHome("home2"));
        assertEquals(2, playerHomes.getHomeCount());

        playerHomes.removeHome("home1");
        assertEquals(1, playerHomes.getHomeCount());

        playerHomes.removeHome("home2");
        assertEquals(0, playerHomes.getHomeCount());
    }

    @Test
    void testGetHomeCountAfterUpdate() {
        playerHomes.setHome("home1", createTestHome("home1"));
        assertEquals(1, playerHomes.getHomeCount());

        // Update the same home
        playerHomes.setHome("home1", createTestHome("home1", 200.0, 64.0, 300.0));
        assertEquals(1, playerHomes.getHomeCount());
    }

    @Test
    void testGetPlayerId() {
        assertEquals(testPlayerId, playerHomes.getPlayerId());
    }

    @Test
    void testGetHomesReturnsMap() {
        Home home1 = createTestHome("home1");
        Home home2 = createTestHome("home2");

        playerHomes.setHome("home1", home1);
        playerHomes.setHome("home2", home2);

        assertNotNull(playerHomes.getHomes());
        assertEquals(2, playerHomes.getHomes().size());
        assertTrue(playerHomes.getHomes().containsKey("home1"));
        assertTrue(playerHomes.getHomes().containsKey("home2"));
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                playerHomes.setHome("home" + i, createTestHome("home" + i));
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 100; i < 200; i++) {
                playerHomes.setHome("home" + i, createTestHome("home" + i));
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertEquals(200, playerHomes.getHomeCount());
    }

    @Test
    void testSetHomeWithEmptyString() {
        Home home = createTestHome("");
        playerHomes.setHome("", home);

        assertTrue(playerHomes.hasHome(""));
        assertEquals(1, playerHomes.getHomeCount());
    }

    @Test
    void testSetHomeWithSpecialCharacters() {
        Home home = createTestHome("home_with-special.chars!");
        playerHomes.setHome("home_with-special.chars!", home);

        assertTrue(playerHomes.hasHome("home_with-special.chars!"));
        assertEquals(1, playerHomes.getHomeCount());
    }

    @Test
    void testSetHomeWithUnicodeCharacters() {
        Home home = createTestHome("家");
        playerHomes.setHome("家", home);

        assertTrue(playerHomes.hasHome("家"));
        assertEquals(1, playerHomes.getHomeCount());
    }

    @Test
    void testRemoveAllHomes() {
        for (int i = 0; i < 10; i++) {
            playerHomes.setHome("home" + i, createTestHome("home" + i));
        }
        assertEquals(10, playerHomes.getHomeCount());

        for (int i = 0; i < 10; i++) {
            playerHomes.removeHome("home" + i);
        }

        assertEquals(0, playerHomes.getHomeCount());
        assertTrue(playerHomes.getHomeNames().isEmpty());
    }

    @Test
    void testGetHomeAfterRemove() {
        Home home = createTestHome("home1");
        playerHomes.setHome("home1", home);
        playerHomes.removeHome("home1");

        Optional<Home> retrieved = playerHomes.getHome("home1");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void testMultiplePlayersIndependence() {
        UUID player2Id = UUID.randomUUID();
        PlayerHomes player2Homes = new PlayerHomes(player2Id);

        playerHomes.setHome("home1", createTestHome("home1"));
        player2Homes.setHome("home1", createTestHome("home1"));

        assertEquals(1, playerHomes.getHomeCount());
        assertEquals(1, player2Homes.getHomeCount());
        assertNotEquals(playerHomes.getPlayerId(), player2Homes.getPlayerId());
    }

    // Helper methods
    private Home createTestHome(String name) {
        return createTestHome(name, 100.0, 64.0, 200.0);
    }

    private Home createTestHome(String name, double x, double y, double z) {
        return new Home(
            0,
            testPlayerId,
            name,
            "world",
            x, y, z,
            0.0f, 0.0f,
            System.currentTimeMillis()
        );
    }
}