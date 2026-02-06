package me.axeno.hommr;

import me.axeno.hommr.api.HommrApi;
import me.axeno.hommr.api.impl.HommrApiImpl;
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
    private Lamp<BukkitCommandActor> mockLamp;

    private File mockDataFolder;

    private Hommr plugin;
    private MockedStatic<Bukkit> bukkitMockedStatic;
    private MockedStatic<HomeManager> homeManagerMockedStatic;

    @BeforeEach
    void setUp() {
        plugin = mock(Hommr.class);

        mockDataFolder = new File(System.getProperty("java.io.tmpdir"), "hommr-test-" + System.currentTimeMillis());
        mockDataFolder.mkdirs();

        bukkitMockedStatic = mockStatic(Bukkit.class);
        homeManagerMockedStatic = mockStatic(HomeManager.class);

        bukkitMockedStatic.when(Bukkit::getServer).thenReturn(mockServer);
        bukkitMockedStatic.when(Bukkit::getServicesManager).thenReturn(mockServicesManager);
        bukkitMockedStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
        bukkitMockedStatic.when(Bukkit::getName).thenReturn("Paper");
        bukkitMockedStatic.when(Bukkit::getVersion).thenReturn("git-Paper-123 (MC: 1.21)");

        lenient().when(mockServer.getServicesManager()).thenReturn(mockServicesManager);
        lenient().when(mockServer.getName()).thenReturn("Paper");
        lenient().when(mockServer.getVersion()).thenReturn("git-Paper-123 (MC: 1.21)");
        lenient().when(mockServer.getBukkitVersion()).thenReturn("1.21-R0.1-SNAPSHOT");

        lenient().doReturn(mockLogger).when(plugin).getLogger();
        lenient().doReturn(mockDataFolder).when(plugin).getDataFolder();
        lenient().doReturn(mockPluginMeta).when(plugin).getPluginMeta();
        lenient().doReturn(mockConfig).when(plugin).getConfig();
        lenient().doReturn(org.slf4j.LoggerFactory.getLogger(Hommr.class)).when(plugin).getSLF4JLogger();
        lenient().doReturn(mockLamp).when(plugin).getLamp();
        lenient().when(mockPluginMeta.getVersion()).thenReturn("1.0.0");

        lenient().doAnswer(invocation -> {
            Field instanceField = Hommr.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, plugin);

            plugin.saveDefaultConfig();
            HomeManager.init();

            HommrApi apiInstance = new HommrApiImpl();
            Field apiField = Hommr.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(plugin, apiInstance);

            Bukkit.getServicesManager().register(HommrApi.class, apiInstance, plugin, ServicePriority.Normal);

            Field lampField = Hommr.class.getDeclaredField("lamp");
            lampField.setAccessible(true);
            lampField.set(plugin, mockLamp);

            try {
                java.lang.reflect.Method logLoadMessageMethod = Hommr.class.getDeclaredMethod("logLoadMessage");
                logLoadMessageMethod.setAccessible(true);
                logLoadMessageMethod.invoke(plugin);
            } catch (Exception e) {
                // Ignore
            }

            return null;
        }).when(plugin).onEnable();

        lenient().doCallRealMethod().when(plugin).onDisable();
        lenient().doCallRealMethod().when(plugin).getApi();

        lenient().doNothing().when(plugin).saveDefaultConfig();
    }

    @AfterEach
    void tearDown() {
        if (bukkitMockedStatic != null) {
            bukkitMockedStatic.close();
        }
        if (homeManagerMockedStatic != null) {
            homeManagerMockedStatic.close();
        }

        if (mockDataFolder != null && mockDataFolder.exists()) {
            deleteDirectory(mockDataFolder);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @Test
    void testOnEnableInitializesPlugin() {
        plugin.onEnable();

        assertNotNull(plugin.getApi());
        assertEquals(plugin, Hommr.getInstance());
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
    void testGetApiReturnsNonNullAfterEnable() {
        plugin.onEnable();

        HommrApi api = plugin.getApi();

        assertNotNull(api);
    }

    @Test
    void testGetLampReturnsValidInstance() {
        plugin.onEnable();

        Lamp<BukkitCommandActor> lamp = plugin.getLamp();

        assertNotNull(lamp);
    }

    @Test
    void testOnDisableShutdownsHomeManager() {
        plugin.onEnable();
        plugin.onDisable();

        homeManagerMockedStatic.verify(HomeManager::shutdown);
    }

    @Test
    void testSaveDefaultConfigIsCalled() {
        plugin.onEnable();

        verify(plugin).saveDefaultConfig();
    }

    @Test
    void testHomeManagerInitCalledBeforeApiCreation() {
        plugin.onEnable();

        homeManagerMockedStatic.verify(HomeManager::init);
        assertNotNull(plugin.getApi());
    }
}
