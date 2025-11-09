package ua.atherium.enchants.effects.handlers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class ScoutEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof ProjectileHitEvent)) return;

        ProjectileHitEvent e = (ProjectileHitEvent) event;
        if (e.getEntity().getType() != EntityType.TRIDENT || !(e.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) e.getEntity().getShooter();

        // Перевірка на елітри
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == org.bukkit.Material.ELYTRA && player.isGliding()) {
            return;
        }

        Location hitLocation = e.getHitBlock() != null ? e.getHitBlock().getLocation() : e.getHitEntity().getLocation();

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) return;

        double maxDistance = levelConfig.getDouble("max_distance");
        if (player.getLocation().distance(hitLocation) > maxDistance) return;

        // Логіка притягування
        Vector direction = hitLocation.toVector().subtract(player.getLocation().toVector()).normalize();
        double strength = config.getDouble("strength_multiplier", 0.6); // 60% сили
        player.setVelocity(direction.multiply(strength));

        EffectPlayer.play(hitLocation, config.getConfigurationSection("effects"));
    }
}
