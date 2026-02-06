package me.axeno.hommr.models;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PlayerHomes {
    private final UUID playerId;
    private final Map<String, Home> homes;

    /**
     * Creates a PlayerHomes instance for the specified player.
     *
     * Initializes the object and prepares an empty, thread-safe map for storing the player's named homes.
     *
     * @param playerId UUID identifying the player whose homes will be managed
     */
    public PlayerHomes(UUID playerId) {
        this.playerId = playerId;
        this.homes = new ConcurrentHashMap<>();
    }

    /**
     * Stores a Home under the given name for this player, using the lowercase form of the name.
     *
     * @param name the home name; its lowercase form is used as the storage key
     * @param home the Home instance to store; replaces any existing home with the same lowercase name
     */
    public void setHome(String name, Home home) {
        homes.put(name.toLowerCase(), home);
    }

    public Optional<Home> getHome(String name) {
        return Optional.ofNullable(homes.get(name.toLowerCase()));
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public Set<String> getHomeNames() {
        return new HashSet<>(homes.keySet());
    }

    public boolean hasHome(String name) {
        return homes.containsKey(name.toLowerCase());
    }

    public int getHomeCount() {
        return homes.size();
    }
}