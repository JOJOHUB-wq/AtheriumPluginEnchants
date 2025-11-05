package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class CobwebImmunityEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof PlayerMoveEvent)) {
            return;
        }

        Player player = ((PlayerMoveEvent) event).getPlayer();
        if (player.getLocation().getBlock().getType().name().equals("COBWEB")) {
            player.setFallDistance(0); // Prevent fall damage
            // By setting the velocity to itself, we cancel the slowness of the cobweb
            player.setVelocity(player.getVelocity());
            EffectPlayer.play(player.getLocation(), config.getConfigurationSection("effects"));
        }
    }
}
