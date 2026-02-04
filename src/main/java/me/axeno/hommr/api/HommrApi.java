package me.axeno.hommr.api;

import me.axeno.hommr.models.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Public API of Hommr
 * <p>
 * * Example of usage:
 * <pre>
 *     {@code
 *     HommrApi hommrApi = ...; // Get the API instance
 *     UUID playerId = ...; // UUID of the player
 *     String homeName = "myHome";
 *     Location location = ...; // Location to set the home
 *     boolean success = hommrApi.setHome(playerId, homeName, location);
 *     }
 *     </pre>
 * </p>
 */
public interface HommrApi {

    /**
     * Set a home for a player
     *
     * @param playerUniqueId UUID of the player
     * @param homeName       Name of the home
     * @param location       Location of the home
     * @return true if the home was set successfully, false otherwise
     */
    boolean setHome(UUID playerUniqueId, String homeName, Location location);

    /**
     * Get a specific home of a player
     *
     * @param playerUniqueId UUID of the player
     * @param homeName       Name of the home
     * @return {@link Optional<Home>} of Home if found, empty otherwise
     */
    Optional<Home> getHome(UUID playerUniqueId, String homeName);

    /**
     * Delete a home of a player
     *
     * @param playerUniqueId UUID of the player
     * @param homeName       Name of the home
     * @return true if the home was deleted successfully, false otherwise
     */
    boolean deleteHome(UUID playerUniqueId, String homeName);

    /**
     * Get the names of all homes of a player
     *
     * @param playerUniqueId UUID of the player
     * @return {@link Set<String>} des noms de homes
     */
    Set<String> getHomeNames(UUID playerUniqueId);

    /**
     * Teleport a player to one of their homes
     *
     * @param player   Player to teleport
     * @param homeName Name of the home
     * @return true if the teleportation was successful, false otherwise
     */
    boolean teleportToHome(Player player, String homeName);

    /**
     * Get the number of homes a player has
     *
     * @param playerUniqueId UUID of the player
     * @return Number of homes
     */
    int getHomeCount(UUID playerUniqueId);

    /**
     * Check if a player has a home with the given name
     *
     * @param playerUniqueId UUID of the player
     * @param homeName       Name of the home
     * @return true if the home exists, false otherwise
     */
    boolean hasHome(UUID playerUniqueId, String homeName);
}
