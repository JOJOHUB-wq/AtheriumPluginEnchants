package ua.atherium.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;

public class EnchantmentListener implements Listener {

    private final AtheriumEnchants plugin;

    public EnchantmentListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                for (CustomEnchant enchant : plugin.getEnchantManager().getEnchantments().values()) {
                    if (meta.getPersistentDataContainer().has(enchant.getKey(), PersistentDataType.INTEGER)) {
                        enchant.handleEvent(event);
                    }
                }
            }
        }
    }
}
