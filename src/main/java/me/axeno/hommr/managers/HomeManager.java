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

    /**
     * Initializes the HomeManager: ensures the database manager exists, creates the in-memory player homes cache, and loads all persisted homes into the cache.
     *
     * <p>On failure to read from the database, the method logs a severe error and continues (the cache will be empty).</p>
     */
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

    /**
     * Persists all homes currently held in the in-memory cache to persistent storage and closes the database manager.
     *
     * If the database manager is not initialized, this method does nothing. On failure to save homes a severe log entry is recorded.
     */
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

    /**
     * Get the PlayerHomes object for the specified player, creating and caching a new one if none exists.
     *
     * @param playerId the UUID of the player
     * @return the PlayerHomes for the specified player; created and stored in the cache if absent
     */
    private static PlayerHomes getOrCreatePlayerHomes(UUID playerId) {
        return playerHomesCache.computeIfAbsent(playerId, PlayerHomes::new);
    }

    /**
     * Create or update a player's home with the given name and location.
     *
     * Attempts to store the provided location as a home for the player. If the player has reached
     * the configured maximum number of homes and the operation would create a new home, the method
     * fails and returns `false`. A HomeSetEvent is fired before the change; if that event is
     * cancelled the operation is aborted.
     *
     * @param player   the player who owns the home
     * @param homeName the name of the home to create or update
     * @param location the location to store for the home
     * @return `true` if the home was created or updated, `false` if the operation was prevented (max homes reached or event cancelled)
     */
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

    /**
     * Retrieve the home with the given name for the specified player.
     *
     * If the player has no existing PlayerHomes entry in the in-memory cache, one will be created.
     *
     * @param playerId the UUID of the player
     * @param homeName the name of the home to retrieve
     * @return an Optional containing the Home if found, or an empty Optional if no home with that name exists
     */
    public static Optional<Home> getHome(UUID playerId, String homeName) {
        PlayerHomes playerHomes = getOrCreatePlayerHomes(playerId);
        return playerHomes.getHome(homeName);
    }

    /**
     * Delete the specified home for the given player, emitting a HomeDeleteEvent that can cancel the deletion.
     *
     * @param player   the player who owns the home
     * @param homeName the name of the home to delete
     * @return `true` if the home was removed, `false` if the home did not exist or the deletion was cancelled
     */
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

    /**
     * Teleports the given player to the specified home if available and permitted.
     *
     * Attempts to locate the home by name for the player, reconstruct its location, fire a HomeTeleportEvent
     * and, if the event is not cancelled, perform the teleport.
     *
     * @param player   the player to teleport
     * @param homeName the name of the home to teleport to
     * @return `true` if the player was teleported, `false` if the home was not found, the location could not be reconstructed, or the teleport was cancelled
     */
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

    /**
     * Retrieve the names of all homes for the specified player.
     *
     * @param playerId the UUID of the player whose home names to return
     * @return the set of home names owned by the player, empty if the player has no homes
     */
    public static Set<String> getHomeNames(UUID playerId) {
        return getOrCreatePlayerHomes(playerId).getHomeNames();
    }

    /**
     * Retrieve the number of homes owned by the specified player.
     *
     * @param playerId the UUID of the player
     * @return the number of homes the player currently has
     */
    public static int getHomeCount(UUID playerId) {
        return getOrCreatePlayerHomes(playerId).getHomeCount();
    }

    /**
     * Determines whether the specified player has a home with the given name.
     *
     * @param playerId the UUID of the player
     * @param homeName the name of the home to check
     * @return `true` if the player has a home with the given name, `false` otherwise
     */
    public static boolean hasHome(UUID playerId, String homeName) {
        return getOrCreatePlayerHomes(playerId).hasHome(homeName);
    }


    /**
     * Determine the maximum number of homes allowed for the given player.
     *
     * <p>Currently returns a placeholder value indicating no limit; permission- or
     * configuration-based limits should be implemented later.</p>
     *
     * @param player the player whose home limit is being queried
     * @return `-1` if unlimited, otherwise the maximum number of homes permitted for the player
     */
    public static int getMaxHomes(@SuppressWarnings("unused") Player player) {
        // TODO: Implement a system to determine max homes based on permissions or other criteria
        return -1; // -1 means unlimited
    }
}