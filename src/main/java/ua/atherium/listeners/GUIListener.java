package ua.atherium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.atherium.gui.GUI;
import ua.atherium.gui.ShopGUI;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof GUI) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                if (inventory.getHolder() instanceof ShopGUI) {
                    ShopGUI gui = (ShopGUI) inventory.getHolder();
                    String command = gui.getCommand(event.getSlot());
                    if (command != null) {
                        player.performCommand(command);
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
