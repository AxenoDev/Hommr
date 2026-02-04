package fr.libnaus.hommr.listeners;

import fr.libnaus.hommr.managers.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        HomeManager.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
