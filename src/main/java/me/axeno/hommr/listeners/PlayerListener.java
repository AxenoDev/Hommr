package me.axeno.hommr.listeners;

import me.axeno.hommr.managers.HomeManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        HomeManager.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
