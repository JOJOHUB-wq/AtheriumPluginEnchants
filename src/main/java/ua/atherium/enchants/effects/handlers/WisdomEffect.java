package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;

import java.util.Map;

public class WisdomEffect implements EnchantmentEffect {
    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        double multiplier = levelConfig.getDouble("multiplier", 1.5);
        int xp = 0;
        if (event instanceof BlockBreakEvent) {
           xp = ((BlockBreakEvent) event).getExpToDrop();
           ((BlockBreakEvent) event).setExpToDrop((int) (xp * multiplier));
        } else if (event instanceof EntityDeathEvent) {
            xp = ((EntityDeathEvent) event).getDroppedExp();
            ((EntityDeathEvent) event).setDroppedExp((int) (xp * multiplier));
        }
    }
}
