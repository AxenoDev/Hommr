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
    void testSetHomeReplacesExistingHome() {
        Home home1 = createTestHome("home1");
        Home home2 = createTestHome("home1");
        home2.setX(200.0);

        playerHomes.setHome("home1", home1);
        playerHomes.setHome("home1", home2);

        assertEquals(1, playerHomes.getHomeCount());
        Optional<Home> retrievedHome = playerHomes.getHome("home1");
        assertTrue(retrievedHome.isPresent());
        assertEquals(200.0, retrievedHome.get().getX());
    }

    @Test
    void testGetHomeReturnsCorrectHome() {
        Home home = createTestHome("home1");
        playerHomes.setHome("home1", home);

        Optional<Home> retrievedHome = playerHomes.getHome("home1");

        assertTrue(retrievedHome.isPresent());
        assertEquals("home1", retrievedHome.get().getName());
    }

    @Test
    void testGetHomeReturnsEmptyForNonexistentHome() {
        Optional<Home> home = playerHomes.getHome("nonexistent");

        assertFalse(home.isPresent());
    }

    @Test
    void testRemoveHomeRemovesHome() {
        Home home = createTestHome("home1");
        playerHomes.setHome("home1", home);

        boolean result = playerHomes.removeHome("home1");

        assertTrue(result);
        assertFalse(playerHomes.hasHome("home1"));
        assertEquals(0, playerHomes.getHomeCount());
    }

    @Test
    void testRemoveNonexistentHomeReturnsFalse() {
        boolean result = playerHomes.removeHome("nonexistent");

        assertFalse(result);
    }

    @Test
    void testHasHomeReturnsTrueForExistingHome() {
        Home home = createTestHome("home1");
        playerHomes.setHome("home1", home);

        assertTrue(playerHomes.hasHome("home1"));
    }

    @Test
    void testHasHomeReturnsFalseForNonexistentHome() {
        assertFalse(playerHomes.hasHome("nonexistent"));
    }

    @Test
    void testHasHomeIsCaseInsensitive() {
        Home home = createTestHome("MyHome");
        playerHomes.setHome("MyHome", home);

        assertTrue(playerHomes.hasHome("myhome"));
        assertTrue(playerHomes.hasHome("MYHOME"));
        assertTrue(playerHomes.hasHome("MyHome"));
    }

    @Test
    void testGetHomeNames() {
        playerHomes.setHome("home1", createTestHome("home1"));
        playerHomes.setHome("home2", createTestHome("home2"));
        playerHomes.setHome("home3", createTestHome("home3"));

        Set<String> homeNames = playerHomes.getHomeNames();

        assertEquals(3, homeNames.size());
        assertTrue(homeNames.contains("home1"));
        assertTrue(homeNames.contains("home2"));
        assertTrue(homeNames.contains("home3"));
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
    }

    @Test
    void testGetHomesReturnsMap() {
        playerHomes.setHome("home1", createTestHome("home1"));
        playerHomes.setHome("home2", createTestHome("home2"));

        assertNotNull(playerHomes.getHomes());
        assertEquals(2, playerHomes.getHomes().size());
    }

    // Helper methods
    private Home createTestHome(String name) {
        return new Home(
            0,
            testPlayerId,
            name,
            "world",
            100.0, 64.0, 200.0,
            0.0f, 0.0f,
            System.currentTimeMillis()
        );
    }
}
