package ua.atherium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import ua.atherium.AtheriumEnchants;
import ua.atherium.gui.AnimatedGUI;

import java.util.List;

public class GUIListener implements Listener {

    private final AtheriumEnchants plugin;

    public GUIListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AnimatedGUI) {
            event.setCancelled(true);
            AnimatedGUI gui = (AnimatedGUI) holder;
            Player player = (Player) event.getWhoClicked();

            List<String> actions = gui.getClickActions(event.getRawSlot());
            if (actions != null && !actions.isEmpty()) {
                processActions(player, actions);
            }
        }
    }

    private void processActions(Player player, List<String> actions) {
        for (String action : actions) {
            if (action.equalsIgnoreCase("[CLOSE]")) {
                player.closeInventory();
            } else if (action.startsWith("[AE_OPEN]")) {
                String menuId = action.substring(10, action.length() - 1);
                plugin.getGuiManager().openGUI(player, menuId);
            }
            // Other actions can be implemented here
        }
    }
}
