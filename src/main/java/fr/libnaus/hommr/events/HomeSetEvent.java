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
public class HomeSetEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String homeName;
    private final Home home;
    private final boolean isUpdate;

    @Setter
    private boolean cancelled;

    public HomeSetEvent(Player player, String homeName, Home home, boolean isUpdate) {
        this.player = player;
        this.homeName = homeName;
        this.home = home;
        this.isUpdate = isUpdate;
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
