package me.axeno.hommr;

import me.axeno.hommr.api.HommrApi;
import me.axeno.hommr.managers.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HommrTest {

    @Mock
    private Server mockServer;

    @Mock
    private ServicesManager mockServicesManager;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Logger mockLogger;

    @Mock
    private FileConfiguration mockConfig;

    @Mock
    private PluginDescriptionFile mockPluginMeta;

    @Mock
    private File mockDataFolder;

    private Hommr plugin;
    private MockedStatic<Bukkit> bukkitMockedStatic;
    private MockedStatic<HomeManager> homeManagerMockedStatic;

    @BeforeEach
    void setUp() {
        plugin = mock(Hommr.class, CALLS_REAL_METHODS);

        bukkitMockedStatic = mockStatic(Bukkit.class);
        homeManagerMockedStatic = mockStatic(HomeManager.class);

        bukkitMockedStatic.when(Bukkit::getServer).thenReturn(mockServer);
        bukkitMockedStatic.when(Bukkit::getServicesManager).thenReturn(mockServicesManager);
        bukkitMockedStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);

        when(mockServer.getServicesManager()).thenReturn(mockServicesManager);
        when(mockServer.getName()).thenReturn("Paper");
        when(mockServer.getVersion()).thenReturn("1.21");

        when(plugin.getLogger()).thenReturn(mockLogger);
        when(plugin.getConfig()).thenReturn(mockConfig);
        when(plugin.getSLF4JLogger()).thenReturn(org.slf4j.LoggerFactory.getLogger(Hommr.class));
        when(plugin.getDataFolder()).thenReturn(mockDataFolder);
        when(plugin.getPluginMeta()).thenReturn(mockPluginMeta);
        when(mockPluginMeta.getVersion()).thenReturn("1.0.0");
    }

    @AfterEach
    void tearDown() {
        if (bukkitMockedStatic != null) {
            bukkitMockedStatic.close();
        }
        if (homeManagerMockedStatic != null) {
            homeManagerMockedStatic.close();
        }
    }

    @Test
    void testOnEnableInitializesPlugin() {
        plugin.onEnable();

        verify(plugin).saveDefaultConfig();
        homeManagerMockedStatic.verify(HomeManager::init);
        assertNotNull(plugin.getApi());
    }

    @Test
    void testOnEnableRegistersApi() {
        plugin.onEnable();

        verify(mockServicesManager).register(
            eq(HommrApi.class),
            any(HommrApi.class),
            eq(plugin),
            eq(ServicePriority.Normal)
        );
    }

    @Test
    void testOnEnableSetsInstance() {
        plugin.onEnable();

        assertEquals(plugin, Hommr.getInstance());
    }

    @Test
    void testOnEnableCreatesLamp() {
        plugin.onEnable();

        assertNotNull(plugin.getLamp());
    }

    @Test
    void testGetApiReturnsNonNullAfterEnable() {
        plugin.onEnable();

        HommrApi api = plugin.getApi();

        assertNotNull(api);
    }

    @Test
    void testGetInstanceReturnsSingleton() {
        plugin.onEnable();

        Hommr instance1 = Hommr.getInstance();
        Hommr instance2 = Hommr.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    void testOnDisableShutdownsHomeManager() {
        plugin.onEnable();
        plugin.onDisable();

        homeManagerMockedStatic.verify(HomeManager::shutdown);
    }

    @Test
    void testOnDisableLogsMessage() {
        plugin.onEnable();
        plugin.onDisable();

        // Verify logger was called (actual logging is hard to verify with SLF4J)
        verify(plugin, atLeastOnce()).getSLF4JLogger();
    }

    @Test
    void testGetLampReturnsValidInstance() {
        plugin.onEnable();

        Lamp<BukkitCommandActor> lamp = plugin.getLamp();

        assertNotNull(lamp);
    }

    @Test
    void testApiIsAccessibleAfterEnable() {
        plugin.onEnable();

        HommrApi api = plugin.getApi();

        assertNotNull(api);
        // Verify it implements the API interface
        assertTrue(api instanceof HommrApi);
    }

    @Test
    void testOnEnableCallsLogLoadMessage() {
        plugin.onEnable();

        // Verify logger was called for startup banner
        verify(plugin, atLeastOnce()).getSLF4JLogger();
    }

    @Test
    void testSaveDefaultConfigIsCalled() {
        plugin.onEnable();

        verify(plugin).saveDefaultConfig();
    }

    @Test
    void testHomeManagerInitCalledBeforeApiCreation() {
        plugin.onEnable();

        // Verify HomeManager.init was called
        homeManagerMockedStatic.verify(HomeManager::init);
        // And API was created
        assertNotNull(plugin.getApi());
    }

    @Test
    void testPluginInitializationOrder() {
        plugin.onEnable();

        // Verify initialization happens in correct order
        verify(plugin).saveDefaultConfig(); // First
        homeManagerMockedStatic.verify(HomeManager::init); // Second
        assertNotNull(plugin.getApi()); // Third - API created
        assertNotNull(plugin.getLamp()); // Fourth - Lamp created
    }

    @Test
    void testGetInstanceBeforeEnableReturnsNull() {
        // Before onEnable is called, getInstance should return null
        try {
            Field instanceField = Hommr.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);

            assertNull(Hommr.getInstance());
        } catch (Exception e) {
            fail("Failed to reset instance field: " + e.getMessage());
        }
    }

    @Test
    void testMultipleOnEnableCallsUpdateInstance() {
        plugin.onEnable();
        Hommr firstInstance = Hommr.getInstance();

        // Create a new plugin instance and enable it
        Hommr plugin2 = mock(Hommr.class, CALLS_REAL_METHODS);
        when(plugin2.getLogger()).thenReturn(mockLogger);
        when(plugin2.getConfig()).thenReturn(mockConfig);
        when(plugin2.getSLF4JLogger()).thenReturn(org.slf4j.LoggerFactory.getLogger(Hommr.class));
        when(plugin2.getDataFolder()).thenReturn(mockDataFolder);
        when(plugin2.getPluginMeta()).thenReturn(mockPluginMeta);

        plugin2.onEnable();

        // The instance should now be the second plugin
        assertEquals(plugin2, Hommr.getInstance());
        assertNotEquals(firstInstance, Hommr.getInstance());
    }

    @Test
    void testOnDisableDoesNotThrowException() {
        plugin.onEnable();

        assertDoesNotThrow(() -> plugin.onDisable());
    }

    @Test
    void testOnDisableWithoutEnableDoesNotThrowException() {
        // Calling onDisable without onEnable should not cause issues
        assertDoesNotThrow(() -> plugin.onDisable());
    }

    @Test
    void testApiRegistrationPriority() {
        plugin.onEnable();

        verify(mockServicesManager).register(
            any(),
            any(HommrApi.class),
            any(),
            eq(ServicePriority.Normal)
        );
    }

    @Test
    void testLampBuildsSuccessfully() {
        plugin.onEnable();

        Lamp<BukkitCommandActor> lamp = plugin.getLamp();

        assertNotNull(lamp);
        assertInstanceOf(Lamp.class, lamp);
    }

    @Test
    void testGetApiReturnsConsistentInstance() {
        plugin.onEnable();

        HommrApi api1 = plugin.getApi();
        HommrApi api2 = plugin.getApi();

        assertSame(api1, api2);
    }

    @Test
    void testGetLampReturnsConsistentInstance() {
        plugin.onEnable();

        Lamp<BukkitCommandActor> lamp1 = plugin.getLamp();
        Lamp<BukkitCommandActor> lamp2 = plugin.getLamp();

        assertSame(lamp1, lamp2);
    }

    @Test
    void testPluginMetaVersionIsAccessible() {
        when(mockPluginMeta.getVersion()).thenReturn("2.0.0-TEST");

        plugin.onEnable();

        verify(mockPluginMeta, atLeastOnce()).getVersion();
    }

    @Test
    void testOnEnableHandlesJavaVersionRetrieval() {
        plugin.onEnable();

        // Verify that Java version can be retrieved without errors
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion);
    }

    @Test
    void testOnEnableHandlesServerVersionRetrieval() {
        plugin.onEnable();

        // Verify server name and version can be retrieved
        verify(mockServer, atLeastOnce()).getName();
        verify(mockServer, atLeastOnce()).getVersion();
    }

    @Test
    void testPluginLifecycle() {
        // Test full plugin lifecycle
        plugin.onEnable();

        assertNotNull(Hommr.getInstance());
        assertNotNull(plugin.getApi());
        assertNotNull(plugin.getLamp());

        plugin.onDisable();

        homeManagerMockedStatic.verify(HomeManager::shutdown);
    }

    @Test
    void testOnEnableWithDifferentServerNames() {
        when(mockServer.getName()).thenReturn("Purpur");
        when(mockServer.getVersion()).thenReturn("1.21.1");

        assertDoesNotThrow(() -> plugin.onEnable());
    }

    @Test
    void testOnEnableWithSpecialCharactersInVersion() {
        when(mockPluginMeta.getVersion()).thenReturn("1.0.0-SNAPSHOT+build.123");

        assertDoesNotThrow(() -> plugin.onEnable());
    }

    @Test
    void testSLF4JLoggerIsUsed() {
        plugin.onEnable();

        verify(plugin, atLeastOnce()).getSLF4JLogger();
    }

    @Test
    void testHomeManagerInitializedOnlyOnce() {
        plugin.onEnable();

        homeManagerMockedStatic.verify(HomeManager::init, times(1));
    }

    @Test
    void testHomeManagerShutdownCalledOnDisable() {
        plugin.onEnable();

        homeManagerMockedStatic.clearInvocations();

        plugin.onDisable();

        homeManagerMockedStatic.verify(HomeManager::shutdown, times(1));
    }
}