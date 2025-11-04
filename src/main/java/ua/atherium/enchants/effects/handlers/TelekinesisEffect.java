package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import ua.atherium.enchants.effects.EnchantmentEffect;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TelekinesisEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        Player player = null;
        if (event instanceof BlockBreakEvent) {
            player = ((BlockBreakEvent) event).getPlayer();
        } else if (event instanceof EntityDeathEvent) {
            player = ((EntityDeathEvent) event).getEntity().getKiller();
        }

        if (player == null) {
            return;
        }

        List<ItemStack> drops = (List<ItemStack>) context.get("drops");
        if (drops == null) {
            return;
        }

        Collection<ItemStack> leftover = player.getInventory().addItem(drops.toArray(new ItemStack[0])).values();

        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        context.put("prevent_default_drops", true);
        if (event instanceof BlockBreakEvent) {
            ((BlockBreakEvent) event).setDropItems(false);
        } else if (event instanceof EntityDeathEvent) {
            ((EntityDeathEvent) event).getDrops().clear();
        }
    }
}
