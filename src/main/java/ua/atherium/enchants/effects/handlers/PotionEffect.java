package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;
import java.util.Random;

public class PotionEffect implements EnchantmentEffect {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity)) {
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

        String target = config.getString("target", "VICTIM").toUpperCase();
        LivingEntity targetEntity = target.equals("SELF") ? (LivingEntity) e.getDamager() : (LivingEntity) e.getEntity();

        PotionEffectType type = PotionEffectType.getByName(levelConfig.getString("potion-type"));
        int duration = levelConfig.getInt("duration-ticks");
        int amplifier = levelConfig.getInt("amplifier");

        if (type != null) {
            targetEntity.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
            EffectPlayer.play(targetEntity.getLocation(), config.getConfigurationSection("effects"));
        }
    }
}
