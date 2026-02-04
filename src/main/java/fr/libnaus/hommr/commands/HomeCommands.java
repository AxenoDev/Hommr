package fr.libnaus.hommr.commands;

import fr.libnaus.hommr.managers.HomeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Set;

public class HomeCommands {

    public static final Component PREFIX = MiniMessage.miniMessage()
            .deserialize("<gradient:#7695FF:#FFFFFF>ʜᴏᴍᴍʀ</gradient> <dark_gray>»</dark_gray> ");

    public static void msg(Player player, Component message) {
        player.sendMessage(PREFIX.append(message));
    }

    @Command("home")
    @CommandPermission("hommr.home.list")
    @Description("List all your homes")
    public void noArgs(Player player) {
        Set<String> homes = HomeManager.getHomeNames(player.getUniqueId());

        if (homes.isEmpty()) {
            msg(player, Component.text("You don't have any homes set.", NamedTextColor.RED));
            return;
        }

        msg(player, Component.text("Your Homes", NamedTextColor.GOLD, TextDecoration.BOLD));

        for (String homeName : homes) {
            HomeManager.getHome(player.getUniqueId(), homeName).ifPresent(home -> {
                String worldInfo = home.getWorld();
                player.sendMessage(Component.text()
                        .append(Component.text(" • ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(homeName, NamedTextColor.YELLOW))
                        .append(Component.text(" (" + worldInfo + ")", NamedTextColor.GRAY))
                        .build());
            });
        }

        player.sendMessage(Component.text()
                .append(Component.text(" Total: ", NamedTextColor.GOLD))
                .append(Component.text(homes.size() + " home(s)", NamedTextColor.YELLOW))
                .build());
    }

    @Command("home <home>")
    @Description("Teleport to your home")
    @CommandPlaceholder
    public void teleportHome(Player player, @Named("home") String homeName) {
        homeName = homeName.toLowerCase();

        if (!HomeManager.hasHome(player.getUniqueId(), homeName)) {
            msg(player, Component.text("This home doesn't exist!", NamedTextColor.RED));
            return;
        }

        if (HomeManager.teleportToHome(player, homeName)) {
            msg(player, Component.text()
                    .append(Component.text("Teleporting to home '", NamedTextColor.GREEN))
                    .append(Component.text(homeName, NamedTextColor.YELLOW))
                    .append(Component.text("'...", NamedTextColor.GREEN))
                    .build());
        } else {
            msg(player, Component.text("The world of this home no longer exists!", NamedTextColor.RED));
        }
    }

    @Command("home list")
    @Description("List all your homes")
    @CommandPermission("hommr.home.list")
    public void listHomes(Player player) {
        Set<String> homes = HomeManager.getHomeNames(player.getUniqueId());

        if (homes.isEmpty()) {
            msg(player, Component.text("You don't have any homes set.", NamedTextColor.RED));
            return;
        }

        msg(player, Component.text("Your Homes", NamedTextColor.GOLD, TextDecoration.BOLD));

        for (String homeName : homes) {
            HomeManager.getHome(player.getUniqueId(), homeName).ifPresent(home -> {
                String worldInfo = home.getWorld();
                player.sendMessage(Component.text()
                        .append(Component.text(" • ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(homeName, NamedTextColor.YELLOW))
                        .append(Component.text(" (" + worldInfo + ")", NamedTextColor.GRAY))
                        .build());
            });
        }

        player.sendMessage(Component.text()
                .append(Component.text(" Total: ", NamedTextColor.GOLD))
                .append(Component.text(homes.size() + " home(s)", NamedTextColor.YELLOW))
                .build());
    }

    @Command("home help")
    @Description("Show help for home commands")
    @CommandPlaceholder
    public void showHelp(Player player) {
        msg(player, Component.text("Home Commands", NamedTextColor.GOLD, TextDecoration.BOLD));

        player.sendMessage(Component.text()
                .append(Component.text(" /home <name> ", NamedTextColor.YELLOW))
                .append(Component.text("- Teleport to a home", NamedTextColor.GRAY))
                .build());

        player.sendMessage(Component.text()
                .append(Component.text(" /home set <name> ", NamedTextColor.YELLOW))
                .append(Component.text("- Set a home", NamedTextColor.GRAY))
                .build());

        player.sendMessage(Component.text()
                .append(Component.text(" /home delete <name> ", NamedTextColor.YELLOW))
                .append(Component.text("- Delete a home", NamedTextColor.GRAY))
                .build());

        player.sendMessage(Component.text()
                .append(Component.text(" /home list ", NamedTextColor.YELLOW))
                .append(Component.text("- List your homes", NamedTextColor.GRAY))
                .build());
    }

    @Command("sethome")
    @Description("Set a new home")
    @CommandPermission("hommr.home.set")
    public void setHome(Player player, @Named("name") String homeName) {
        homeName = homeName.toLowerCase();

        // Validate name
        if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
            msg(player, Component.text("Invalid home name! Use only letters, numbers, and underscores.", NamedTextColor.RED));
            return;
        }

        if (HomeManager.hasHome(player.getUniqueId(), homeName)) {
            msg(player, Component.text("A home with this name already exists! It will be updated with the new location.", NamedTextColor.YELLOW));
        }

        boolean isUpdate = HomeManager.hasHome(player.getUniqueId(), homeName);

        if (HomeManager.setHome(player, homeName, player.getLocation())) {
            Component message = Component.text()
                    .append(Component.text("Home '", NamedTextColor.GREEN))
                    .append(Component.text(homeName, NamedTextColor.YELLOW))
                    .append(Component.text(isUpdate ? "' updated successfully!" : "' set successfully!", NamedTextColor.GREEN))
                    .build();
            msg(player, message);
        } else {
            int current = HomeManager.getHomeCount(player.getUniqueId());
            int max = HomeManager.getMaxHomes(player);
            msg(player, Component.text()
                    .append(Component.text("You have reached the maximum number of homes! ", NamedTextColor.RED))
                    .append(Component.text("(" + current + "/" + max + ")", NamedTextColor.GRAY))
                    .build());
        }
    }

    @Command("delhome")
    @Description("Delete a home")
    @CommandPermission("hommr.home.delete")
    public void deleteHome(Player player, @Named("home") String homeName) {
        homeName = homeName.toLowerCase();

        if (HomeManager.deleteHome(player, homeName)) {
            msg(player, Component.text()
                    .append(Component.text("Home '", NamedTextColor.GREEN))
                    .append(Component.text(homeName, NamedTextColor.YELLOW))
                    .append(Component.text("' deleted successfully!", NamedTextColor.GREEN))
                    .build());
        } else {
            msg(player, Component.text("This home doesn't exist!", NamedTextColor.RED));
        }
    }
}
