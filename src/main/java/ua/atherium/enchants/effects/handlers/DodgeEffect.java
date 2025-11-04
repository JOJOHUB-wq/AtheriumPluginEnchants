package ua.atherium.enchants.effects.handlers;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;

import java.util.Map;
import java.util.Random;

public class DodgeEffect implements EnchantmentEffect {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        double chance = levelConfig.getDouble("chance", 7.0);
        if (RANDOM.nextDouble() * 100 > chance) {
            return;
        }

        e.setCancelled(true);
        Player player = (Player) e.getEntity();
        player.playSound(player.getLocation(), Sound.valueOf(levelConfig.getString("sound", "ENTITY_BAT_TAKEOFF")), 1, 1);
    }
}
