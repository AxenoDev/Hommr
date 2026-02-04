package fr.libnaus.hommr.commands;

import fr.libnaus.hommr.Hommr;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/**
 * Admin commands for Hommr
 */
@Command("hommradmin")
@CommandPermission("hommr.admin")
public class AdminCommands {

    /**
     * /hommradmin info - Show plugin info
     */
    @Subcommand("info")
    @Description("Show plugin information")
    @CommandPermission("hommr.admin.info")
    public void info(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━ Hommr Info ━━━━━━━");
        sender.sendMessage("§eVersion: §f" + Hommr.getInstance().getPluginMeta().getVersion());
        sender.sendMessage("§eAuthor: §fAxenoDev");
        sender.sendMessage("§eWebsite: §fhttps://axeno.me");
    }
}
