package fr.libnaus.hommr.events;

import fr.libnaus.hommr.models.Home;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class HomeDeleteEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String homeName;
    private final Home home;

    @Setter
    private boolean cancelled;

    public HomeDeleteEvent(Player player, String homeName, Home home) {
        this.player = player;
        this.homeName = homeName;
        this.home = home;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
