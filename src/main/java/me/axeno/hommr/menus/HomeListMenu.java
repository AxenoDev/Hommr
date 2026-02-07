package me.axeno.hommr.menus;

import dev.xernas.menulib.PaginatedMenu;
import dev.xernas.menulib.utils.InventorySize;
import dev.xernas.menulib.utils.ItemBuilder;
import me.axeno.hommr.managers.HomeManager;
import me.axeno.hommr.models.Home;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HomeListMenu extends PaginatedMenu {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final List<Home> homes;

    public HomeListMenu(Player owner) {
        super(owner);
        // Récupérer tous les homes du joueur
        Set<String> homeNames = HomeManager.getHomeNames(owner.getUniqueId());
        this.homes = homeNames.stream()
                .map(name -> HomeManager.getHome(owner.getUniqueId(), name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(Home::getName))
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable Material getBorderMaterial() {
        return Material.GRAY_STAINED_GLASS_PANE;
    }

    @Override
    public @NotNull List<Integer> getStaticSlots() {
        return List.of(49); // Slot pour le bouton de fermeture
    }

    @Override
    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Home home : homes) {
            List<String> lore = new ArrayList<>();
            lore.add("§7Monde: §f" + home.getWorld());
            lore.add("§7X: §f" + String.format("%.2f", home.getX()));
            lore.add("§7Y: §f" + String.format("%.2f", home.getY()));
            lore.add("§7Z: §f" + String.format("%.2f", home.getZ()));
            lore.add("§7Créé le: §f" + DATE_FORMAT.format(new Date(home.getCreatedAt())));
            lore.add("");
            lore.add("§aCliquez pour vous téléporter");
            lore.add("§cShift + Clic pour supprimer");

            ItemStack item = new ItemBuilder(this, Material.RED_BED, it -> {
                it.setDisplayName("§e" + home.getName());
                it.setLore(lore);
            });

            items.add(item);
        }
        return items;
    }

    @Override
    public Map<Integer, ItemBuilder> getButtons() {
        return Map.of(
                49, new ItemBuilder(this, Material.BARRIER, it -> {
                    it.setDisplayName("§cFermer");
                })
        );
    }

    @Override
    public @NotNull String getName() {
        return "§8Mes Homes §7(" + homes.size() + ")";
    }

    @Override
    public @Nullable String getTexture() {
        return null;
    }

    @Override
    public @NotNull InventorySize getInventorySize() {
        return InventorySize.LARGEST;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Bouton de fermeture
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Récupérer le nom du home depuis l'item
        String displayName = clickedItem.getItemMeta().getDisplayName();
        if (displayName == null || displayName.isEmpty()) return;
        String homeName = displayName.replace("§e", "");

        Optional<Home> homeOpt = HomeManager.getHome(player.getUniqueId(), homeName);
        if (homeOpt.isEmpty()) {
            player.sendMessage("§cCe home n'existe plus !");
            player.closeInventory();
            return;
        }

        Home home = homeOpt.get();

        if (event.isShiftClick()) {
            // Suppression du home
            boolean success = HomeManager.deleteHome(player, homeName);
            if (success) {
                player.sendMessage("§cHome §e" + homeName + " §csupprimé !");
            } else {
                player.sendMessage("§cImpossible de supprimer le home §e" + homeName + " §c!");
            }
            player.closeInventory();
        } else {
            // Téléportation au home
            Location location = home.toLocation();
            if (location == null) {
                player.sendMessage("§cLe monde §e" + home.getWorld() + " §cn'existe plus !");
                player.closeInventory();
                return;
            }

            player.teleport(location);
            player.sendMessage("§aTéléportation au home §e" + homeName + " §a!");
            player.closeInventory();
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        // Rien à faire lors de la fermeture
    }

    @Override
    public List<Integer> getTakableSlot() {
        return List.of();
    }

    @Override
    public int getSizeOfItems() {
        return homes.size();
    }
}
