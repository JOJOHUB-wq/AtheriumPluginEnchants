package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;
import java.util.Random;

public class ExplosiveEffect implements EnchantmentEffect {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof ProjectileHitEvent)) {
            return;
        }

        ProjectileHitEvent e = (ProjectileHitEvent) event;
        if (!(e.getEntity() instanceof Arrow) || !(((Arrow) e.getEntity()).getShooter() instanceof org.bukkit.entity.Player)) {
            return;
        }

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        double chance = levelConfig.getDouble("chance", 100.0);
        if (RANDOM.nextDouble() * 100 > chance) {
            return;
        }

        float power = (float) levelConfig.getDouble("power", 1.5);
        boolean breakBlocks = levelConfig.getBoolean("break_blocks", false);

        if (e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), power, false, breakBlocks)) {
            EffectPlayer.play(e.getEntity().getLocation(), config.getConfigurationSection("effects"));
        }
    }
}
