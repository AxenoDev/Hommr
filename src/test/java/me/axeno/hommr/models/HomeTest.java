package me.axeno.hommr.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeTest {

    @Mock
    private World mockWorld;

    @Mock
    private Server mockServer;

    private UUID testOwner;
    private String testHomeName;

    @BeforeEach
    void setUp() {
        testOwner = UUID.randomUUID();
        testHomeName = "TestHome";

        // Setup Bukkit server mock
        mockStatic(Bukkit.class);
        when(Bukkit.getServer()).thenReturn(mockServer);
        when(mockWorld.getName()).thenReturn("world");
    }

    @Test
    void testHomeConstructor() {
        Home home = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 0.0f, System.currentTimeMillis());

        assertEquals(1, home.getId());
        assertEquals(testOwner, home.getOwner());
        assertEquals(testHomeName, home.getName());
        assertEquals("world", home.getWorld());
        assertEquals(100.0, home.getX());
        assertEquals(64.0, home.getY());
        assertEquals(200.0, home.getZ());
        assertEquals(90.0f, home.getYaw());
        assertEquals(0.0f, home.getPitch());
        assertNotNull(home.getCreatedAt());
    }

    @Test
    void testFromLocationCreatesHomeWithCorrectData() {
        Location location = new Location(mockWorld, 100.5, 64.0, 200.5, 45.0f, -30.0f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertNotNull(home);
        assertEquals(0, home.getId()); // ID should be 0 for new homes
        assertEquals(testOwner, home.getOwner());
        assertEquals(testHomeName, home.getName());
        assertEquals("world", home.getWorld());
        assertEquals(100.5, home.getX());
        assertEquals(64.0, home.getY());
        assertEquals(200.5, home.getZ());
        assertEquals(45.0f, home.getYaw());
        assertEquals(-30.0f, home.getPitch());
        assertTrue(home.getCreatedAt() > 0);
        assertTrue(home.getCreatedAt() <= System.currentTimeMillis());
    }

    @Test
    void testFromLocationWithNegativeCoordinates() {
        Location location = new Location(mockWorld, -50.0, 10.0, -100.0, 180.0f, 90.0f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertEquals(-50.0, home.getX());
        assertEquals(10.0, home.getY());
        assertEquals(-100.0, home.getZ());
        assertEquals(180.0f, home.getYaw());
        assertEquals(90.0f, home.getPitch());
    }

    @Test
    void testFromLocationWithVeryLargeCoordinates() {
        Location location = new Location(mockWorld, 30000000.0, 256.0, 30000000.0, 359.9f, 89.9f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertEquals(30000000.0, home.getX());
        assertEquals(256.0, home.getY());
        assertEquals(30000000.0, home.getZ());
    }

    @Test
    void testFromLocationWithZeroCoordinates() {
        Location location = new Location(mockWorld, 0.0, 0.0, 0.0, 0.0f, 0.0f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertEquals(0.0, home.getX());
        assertEquals(0.0, home.getY());
        assertEquals(0.0, home.getZ());
        assertEquals(0.0f, home.getYaw());
        assertEquals(0.0f, home.getPitch());
    }

    @Test
    void testToLocationReturnsCorrectLocation() {
        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Home home = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, System.currentTimeMillis());

            Location location = home.toLocation();

            assertNotNull(location);
            assertEquals(mockWorld, location.getWorld());
            assertEquals(100.0, location.getX());
            assertEquals(64.0, location.getY());
            assertEquals(200.0, location.getZ());
            assertEquals(90.0f, location.getYaw());
            assertEquals(45.0f, location.getPitch());
        }
    }

    @Test
    void testToLocationReturnsNullWhenWorldNotFound() {
        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.getWorld("nonexistent_world")).thenReturn(null);

            Home home = new Home(1, testOwner, testHomeName, "nonexistent_world", 100.0, 64.0, 200.0, 90.0f, 45.0f, System.currentTimeMillis());

            Location location = home.toLocation();

            assertNull(location);
        }
    }

    @Test
    void testToLocationWithNegativeCoordinates() {
        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Home home = new Home(1, testOwner, testHomeName, "world", -100.5, 64.0, -200.5, -90.0f, -45.0f, System.currentTimeMillis());

            Location location = home.toLocation();

            assertNotNull(location);
            assertEquals(-100.5, location.getX());
            assertEquals(64.0, location.getY());
            assertEquals(-200.5, location.getZ());
            assertEquals(-90.0f, location.getYaw());
            assertEquals(-45.0f, location.getPitch());
        }
    }

    @Test
    void testHomeEquality() {
        long timestamp = System.currentTimeMillis();
        Home home1 = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);
        Home home2 = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);

        assertEquals(home1, home2);
        assertEquals(home1.hashCode(), home2.hashCode());
    }

    @Test
    void testHomeInequality() {
        long timestamp = System.currentTimeMillis();
        Home home1 = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);
        Home home2 = new Home(2, testOwner, "DifferentHome", "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);

        assertNotEquals(home1, home2);
    }

    @Test
    void testSettersAndGetters() {
        Home home = new Home();

        home.setId(5);
        home.setOwner(testOwner);
        home.setName("NewHome");
        home.setWorld("world_nether");
        home.setX(150.0);
        home.setY(80.0);
        home.setZ(250.0);
        home.setYaw(180.0f);
        home.setPitch(45.0f);
        home.setCreatedAt(123456789L);

        assertEquals(5, home.getId());
        assertEquals(testOwner, home.getOwner());
        assertEquals("NewHome", home.getName());
        assertEquals("world_nether", home.getWorld());
        assertEquals(150.0, home.getX());
        assertEquals(80.0, home.getY());
        assertEquals(250.0, home.getZ());
        assertEquals(180.0f, home.getYaw());
        assertEquals(45.0f, home.getPitch());
        assertEquals(123456789L, home.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Home home = new Home();
        assertNotNull(home);
    }

    @Test
    void testFromLocationWithDifferentWorldNames() {
        when(mockWorld.getName()).thenReturn("world_the_end");
        Location location = new Location(mockWorld, 0.0, 64.0, 0.0, 0.0f, 0.0f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertEquals("world_the_end", home.getWorld());
    }

    @Test
    void testFromLocationPreservesExactDoubleValues() {
        Location location = new Location(mockWorld, 123.456789, 64.123456, 987.654321, 12.34f, 56.78f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertEquals(123.456789, home.getX());
        assertEquals(64.123456, home.getY());
        assertEquals(987.654321, home.getZ());
        assertEquals(12.34f, home.getYaw());
        assertEquals(56.78f, home.getPitch());
    }

    @Test
    void testCreatedAtTimestampIsReasonable() throws InterruptedException {
        long beforeCreate = System.currentTimeMillis();
        Thread.sleep(1); // Small delay to ensure timestamp difference
        Location location = new Location(mockWorld, 0.0, 0.0, 0.0, 0.0f, 0.0f);
        Home home = Home.fromLocation(testOwner, testHomeName, location);
        Thread.sleep(1);
        long afterCreate = System.currentTimeMillis();

        assertTrue(home.getCreatedAt() >= beforeCreate);
        assertTrue(home.getCreatedAt() <= afterCreate);
    }

    @Test
    void testToLocationRoundTrip() {
        try (var mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Location originalLocation = new Location(mockWorld, 100.5, 64.0, 200.5, 45.0f, -30.0f);
            Home home = Home.fromLocation(testOwner, testHomeName, originalLocation);
            Location reconstructedLocation = home.toLocation();

            assertNotNull(reconstructedLocation);
            assertEquals(originalLocation.getX(), reconstructedLocation.getX());
            assertEquals(originalLocation.getY(), reconstructedLocation.getY());
            assertEquals(originalLocation.getZ(), reconstructedLocation.getZ());
            assertEquals(originalLocation.getYaw(), reconstructedLocation.getYaw());
            assertEquals(originalLocation.getPitch(), reconstructedLocation.getPitch());
        }
    }
}