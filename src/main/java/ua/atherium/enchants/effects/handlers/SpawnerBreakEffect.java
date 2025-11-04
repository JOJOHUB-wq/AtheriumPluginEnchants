package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class SpawnerBreakEffect implements EnchantmentEffect {
    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if(!(event instanceof BlockBreakEvent)) {
            return;
        }

        BlockBreakEvent e = (BlockBreakEvent) event;

        if (e.getBlock().getType() == Material.SPAWNER) {
            e.setDropItems(true);
            e.setExpToDrop(100);

            ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
            if(levelConfig == null) {
                return;
            }

            int durabilityCost = levelConfig.getInt("durability-cost", 100);
            ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
            if (tool.getItemMeta() instanceof Damageable) {
                Damageable meta = (Damageable) tool.getItemMeta();
                meta.setDamage(meta.getDamage() + durabilityCost);
                tool.setItemMeta(meta);
                EffectPlayer.play(e.getBlock().getLocation(), config.getConfigurationSection("effects"));
            }
        }
    }
}
