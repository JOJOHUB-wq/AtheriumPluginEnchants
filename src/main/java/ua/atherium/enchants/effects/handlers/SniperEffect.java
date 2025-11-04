package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import ua.atherium.enchants.effects.EnchantmentEffect;

import java.util.Map;

public class SniperEffect implements EnchantmentEffect {
    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if(!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        if (context.containsKey("is_headshot") && (boolean) context.get("is_headshot")) {
            ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
            if(levelConfig == null) {
                return;
            }
            double multiplier = levelConfig.getDouble("multiplier", 1.25);
            e.setDamage(e.getDamage() * multiplier);
        }
    }
}
