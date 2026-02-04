package fr.libnaus.hommr.managers;

import fr.libnaus.hommr.events.HomeDeleteEvent;
import fr.libnaus.hommr.events.HomeSetEvent;
import fr.libnaus.hommr.events.HomeTeleportEvent;
import fr.libnaus.hommr.models.Home;
import fr.libnaus.hommr.models.PlayerHomes;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {

    @Getter
    private static Map<UUID, PlayerHomes> playerHomesCache;

    public static void init() {
        playerHomesCache = new ConcurrentHashMap<>();
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

        Home home = Home.fromLocation(homeName, location);

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
        PlayerHomes playerHomes = playerHomesCache.get(playerId);
        if (playerHomes == null) {
            return Optional.empty();
        }
        return playerHomes.getHome(homeName);
    }

    public static boolean deleteHome(Player player, String homeName) {
        PlayerHomes playerHomes = playerHomesCache.get(player.getUniqueId());
        if (playerHomes == null) {
            return false;
        }

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
        PlayerHomes playerHomes = playerHomesCache.get(playerId);
        if (playerHomes == null) {
            return Collections.emptySet();
        }
        return playerHomes.getHomeNames();
    }

    public static int getHomeCount(UUID playerId) {
        PlayerHomes playerHomes = playerHomesCache.get(playerId);
        return playerHomes != null ? playerHomes.getHomeCount() : 0;
    }

    public static boolean hasHome(UUID playerId, String homeName) {
        PlayerHomes playerHomes = playerHomesCache.get(playerId);
        return playerHomes != null && playerHomes.hasHome(homeName);
    }

    public static void unloadPlayer(UUID playerId) {
        playerHomesCache.remove(playerId);
    }

    public static int getMaxHomes(Player player) {
        // TODO: Implement a system to determine max homes based on permissions or other criteria
        return -1; // -1 means unlimited
    }
}
