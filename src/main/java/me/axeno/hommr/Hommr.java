package me.axeno.hommr;

import dev.xernas.menulib.MenuLib;
import lombok.Getter;
import me.axeno.hommr.api.HommrApi;
import me.axeno.hommr.api.impl.HommrApiImpl;
import me.axeno.hommr.commands.HomeCommands;
import me.axeno.hommr.managers.HomeManager;
import me.axeno.hommr.models.PlayerHomes;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashMap;
import java.util.Map;

public final class Hommr extends JavaPlugin {

    @Getter
    private static Hommr instance;

    @Getter
    private HommrApi api;

    @Getter
    private Lamp<BukkitCommandActor> lamp;

    /**
     * Initializes the plugin on enable: sets the singleton instance, ensures default configuration,
     * starts the HomeManager, creates and registers the Hommr API service, builds the command lamp,
     * registers plugin commands, and emits the startup log banner.
     */
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        HomeManager.init();

        int pluginId = 29415;
        Metrics metrics = new Metrics(this, pluginId);

        System.out.println("Registering bStats metrics...");

        metrics.addCustomChart(new SingleLineChart("total_homes", () -> {
            System.out.println("Calculating total homes for metrics...");
            return HomeManager.getPlayerHomesCache().values().stream()
                    .mapToInt(PlayerHomes::getHomeCount)
                    .sum();
        }));

        MenuLib.init(this);

        this.api = new HommrApiImpl();

        Bukkit.getServicesManager().register(HommrApi.class, api, this, ServicePriority.Normal);

        this.lamp = BukkitLamp.builder(this).build();

        lamp.register(new HomeCommands());

        this.logLoadMessage();
    }

    /**
     * Perform plugin shutdown tasks when the plugin is disabled.
     *
     * <p>Shuts down the HomeManager and logs a disable message to the SLF4J logger.</p>
     */
    @Override
    public void onDisable() {
        HomeManager.shutdown();
        this.getSLF4JLogger().info("Hommr Disabled");
    }

    /**
     * Logs a stylized startup banner containing plugin, Java, and server information.
     *
     * <p>Emits a multi-line ASCII banner to the plugin logger that includes the plugin
     * version, the running Java version, and the server name/version.</p>
     */
    private void logLoadMessage() {
        Logger logger = this.getSLF4JLogger();

        @SuppressWarnings("UnstableApiUsage")
        String pluginVersion = this.getPluginMeta().getVersion();
        String javaVersion = System.getProperty("java.version");
        String server = String.format("%s %s", Bukkit.getName(), Bukkit.getVersion());

        logger.info("\u001B[1;34m                                                    \u001B[0m");
        logger.info("\u001B[1;34m  _  _  ___  __  __ __  __ ___      \u001B[0;90mHommr {}\u001B[0m", pluginVersion);
        logger.info("\u001B[1;34m | || |/ _ \\|  \\/  |  \\/  | _ \\     \u001B[0;90mJava {}\u001B[0m", javaVersion);
        logger.info("\u001B[1;34m | __ | (_) | |\\/| | |\\/| |   /     \u001B[0;90mServer: {}\u001B[0m", server);
        logger.info("\u001B[1;34m |_||_|\\___/|_|  |_|_|  |_|_|_\\     \u001B[0m");
        logger.info("\u001B[1;34m                                                    \u001B[0m");
    }
}