package fr.libnaus.hommr.api.impl;

import fr.libnaus.hommr.api.HommrApi;
import fr.libnaus.hommr.managers.HomeManager;
import fr.libnaus.hommr.models.Home;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the Hommr API
 */
@RequiredArgsConstructor
public class HommrApiImpl implements HommrApi {

    @Override
    public boolean setHome(UUID playerUniqueId, String homeName, Location location) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (player == null) return false;
        return HomeManager.setHome(player, homeName, location);
    }

    @Override
    public Optional<Home> getHome(UUID playerUniqueId, String homeName) {
        return HomeManager.getHome(playerUniqueId, homeName);
    }

    @Override
    public boolean deleteHome(UUID playerUniqueId, String homeName) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (player == null) {
            return false;
        }
        return HomeManager.deleteHome(player, homeName);
    }

    @Override
    public Set<String> getHomeNames(UUID playerUniqueId) {
        return HomeManager.getHomeNames(playerUniqueId);
    }

    @Override
    public boolean teleportToHome(Player player, String homeName) {
        return HomeManager.teleportToHome(player, homeName);
    }

    @Override
    public int getHomeCount(UUID playerUniqueId) {
        return HomeManager.getHomeCount(playerUniqueId);
    }

    @Override
    public boolean hasHome(UUID playerUniqueId, String homeName) {
        return HomeManager.hasHome(playerUniqueId, homeName);
    }
}
