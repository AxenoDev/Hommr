package me.axeno.hommr.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

        lenient().when(mockWorld.getName()).thenReturn("world");
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
        assertTrue(home.getCreatedAt() > 0);
    }

    @Test
    void testFromLocationCreatesHomeWithCorrectData() {
        Location location = new Location(mockWorld, 100.5, 64.0, 200.5, 45.0f, -30.0f);

        Home home = Home.fromLocation(testOwner, testHomeName, location);

        assertNotNull(home);
        assertEquals(testOwner, home.getOwner());
        assertEquals(testHomeName, home.getName());
        assertEquals("world", home.getWorld());
        assertEquals(100.5, home.getX());
        assertEquals(64.0, home.getY());
        assertEquals(200.5, home.getZ());
        assertEquals(45.0f, home.getYaw());
        assertEquals(-30.0f, home.getPitch());
        assertTrue(home.getCreatedAt() <= System.currentTimeMillis());
    }

    @Test
    void testToLocationReturnsCorrectLocation() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(() -> Bukkit.getWorld("world")).thenReturn(mockWorld);

            Home home = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 45.0f, -30.0f, System.currentTimeMillis());

            Location location = home.toLocation();

            assertNotNull(location);
            assertEquals(mockWorld, location.getWorld());
            assertEquals(100.0, location.getX());
            assertEquals(64.0, location.getY());
            assertEquals(200.0, location.getZ());
            assertEquals(45.0f, location.getYaw());
            assertEquals(-30.0f, location.getPitch());
        }
    }

    @Test
    void testToLocationReturnsNullWhenWorldNotFound() {
        try (MockedStatic<Bukkit> bukkitMock = mockStatic(Bukkit.class)) {
            bukkitMock.when(() -> Bukkit.getWorld("world")).thenReturn(null);

            Home home = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 45.0f, -30.0f, System.currentTimeMillis());

            Location location = home.toLocation();

            assertNull(location);
        }
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
        long timestamp = System.currentTimeMillis();
        home.setCreatedAt(timestamp);

        assertEquals(5, home.getId());
        assertEquals(testOwner, home.getOwner());
        assertEquals("NewHome", home.getName());
        assertEquals("world_nether", home.getWorld());
        assertEquals(150.0, home.getX());
        assertEquals(80.0, home.getY());
        assertEquals(250.0, home.getZ());
        assertEquals(180.0f, home.getYaw());
        assertEquals(45.0f, home.getPitch());
        assertEquals(timestamp, home.getCreatedAt());
    }

    @Test
    void testHomeEquality() {
        long timestamp = System.currentTimeMillis();
        Home home1 = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);
        Home home2 = new Home(1, testOwner, testHomeName, "world", 100.0, 64.0, 200.0, 90.0f, 45.0f, timestamp);

        assertEquals(home1, home2);
        assertEquals(home1.hashCode(), home2.hashCode());
    }
}
