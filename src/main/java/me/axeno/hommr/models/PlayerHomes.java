package me.axeno.hommr.models;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PlayerHomes {
    private final UUID playerId;
    private final Map<String, Home> homes;

    public PlayerHomes(UUID playerId) {
        this.playerId = playerId;
        this.homes = new ConcurrentHashMap<>();
    }

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
