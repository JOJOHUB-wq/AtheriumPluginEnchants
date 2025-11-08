package ua.atherium.enchants.effects.handlers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class ScoutEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof ProjectileHitEvent)) return;

        ProjectileHitEvent e = (ProjectileHitEvent) event;
        if (!(e.getEntity() instanceof Trident) || !(e.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) e.getEntity().getShooter();
        Location hitLocation = e.getHitBlock() != null ? e.getHitBlock().getLocation() : e.getHitEntity().getLocation();

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) return;

        double maxDistance = levelConfig.getDouble("max_distance");
        if (player.getLocation().distance(hitLocation) <= maxDistance) {
            player.teleport(hitLocation.setDirection(player.getLocation().getDirection()));
            EffectPlayer.play(hitLocation, config.getConfigurationSection("effects"));
        }
    }
}
