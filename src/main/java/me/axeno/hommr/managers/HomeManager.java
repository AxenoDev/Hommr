package me.axeno.hommr.managers;

import lombok.Getter;
import me.axeno.hommr.Hommr;
import me.axeno.hommr.events.HomeDeleteEvent;
import me.axeno.hommr.events.HomeSetEvent;
import me.axeno.hommr.events.HomeTeleportEvent;
import me.axeno.hommr.models.Home;
import me.axeno.hommr.models.PlayerHomes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {

    @Getter
    private static Map<UUID, PlayerHomes> playerHomesCache;
    private static DatabaseManager databaseManager;

    public static void init() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            databaseManager.init();
        }

        playerHomesCache = new ConcurrentHashMap<>();

        try {
            List<Home> homes = databaseManager.getAllHomes();
            for (Home home : homes) {
                PlayerHomes ph = playerHomesCache.computeIfAbsent(home.getOwner(), PlayerHomes::new);
                ph.setHome(home.getName(), home);
            }
            Hommr.getInstance().getLogger().info("Loaded " + homes.size() + " homes.");
        } catch (SQLException e) {
            Hommr.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to load all homes", e);
        }
    }

    public static void shutdown() {
        if (databaseManager != null) {
            try {
                List<Home> allHomes = new ArrayList<>();
                for (PlayerHomes ph : playerHomesCache.values()) {
                    allHomes.addAll(ph.getHomes().values());
                }
                databaseManager.saveAllHomes(allHomes);
                Hommr.getInstance().getLogger().info("Saved " + allHomes.size() + " homes.");
            } catch (SQLException e) {
                Hommr.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to save all homes", e);
            }
            databaseManager.close();
        }
    }

    private static PlayerHomes getOrCreatePlayerHomes(UUID playerId) {
        return playerHomesCache.computeIfAbsent(playerId, PlayerHomes::new);
    }

    public static boolean setHome(Player player, String homeName, Location location) {
        PlayerHomes playerHomes = getOrCreatePlayerHomes(player.getUniqueId());

        boolean isUpdate = playerHomes.hasHome(homeName);
        if (!isUpdate && getMaxHomes(player) != -1 && playerHomes.getHomeCount() >= getMaxHomes(player)) {
            return false;
        }

        Home home = Home.fromLocation(player.getUniqueId(), homeName, location);

        // Call the event
        HomeSetEvent event = new HomeSetEvent(player, homeName, home, isUpdate);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        playerHomes.setHome(homeName, home);
        return true;
    }

    public static Optional<Home> getHome(UUID playerId, String homeName) {
        PlayerHomes playerHomes = getOrCreatePlayerHomes(playerId);
        return playerHomes.getHome(homeName);
    }

    public static boolean deleteHome(Player player, String homeName) {
        PlayerHomes playerHomes = getOrCreatePlayerHomes(player.getUniqueId());

        Optional<Home> homeOpt = playerHomes.getHome(homeName);
        if (homeOpt.isEmpty()) {
            return false;
        }

        // Call the event
        HomeDeleteEvent event = new HomeDeleteEvent(player, homeName, homeOpt.get());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        return playerHomes.removeHome(homeName);
    }

    public static boolean teleportToHome(Player player, String homeName) {
        Optional<Home> homeOpt = getHome(player.getUniqueId(), homeName);
        if (homeOpt.isEmpty()) {
            return false;
        }

        Home home = homeOpt.get();
        Location location = home.toLocation();
        if (location == null) {
            return false; // The location could not be reconstructed
        }

        // Call the event
        HomeTeleportEvent event = new HomeTeleportEvent(player, homeName, home);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        player.teleport(location);
        return true;
    }

    public static Set<String> getHomeNames(UUID playerId) {
        return getOrCreatePlayerHomes(playerId).getHomeNames();
    }

    public static int getHomeCount(UUID playerId) {
        return getOrCreatePlayerHomes(playerId).getHomeCount();
    }

    public static boolean hasHome(UUID playerId, String homeName) {
        return getOrCreatePlayerHomes(playerId).hasHome(homeName);
    }


    public static int getMaxHomes(@SuppressWarnings("unused") Player player) {
        // TODO: Implement a system to determine max homes based on permissions or other criteria
        return -1; // -1 means unlimited
    }
}
