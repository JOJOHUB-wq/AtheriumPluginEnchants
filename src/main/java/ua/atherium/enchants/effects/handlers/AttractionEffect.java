package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;
import java.util.Random;

public class AttractionEffect implements EnchantmentEffect {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) e.getDamager();
        LivingEntity target = (LivingEntity) e.getEntity();

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) return;

        double chance = levelConfig.getDouble("chance");
        if (RANDOM.nextDouble() * 100 < chance) {
            double strength = levelConfig.getDouble("strength");
            Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            target.setVelocity(direction.multiply(strength));
            EffectPlayer.play(target.getLocation(), config.getConfigurationSection("effects"));
        }
    }
}
