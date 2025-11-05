package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class AutoFishEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof PlayerFishEvent) || ((PlayerFishEvent) event).getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        PlayerFishEvent fishEvent = (PlayerFishEvent) event;
        Player player = fishEvent.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.performCommand("cast"); // A simple way to re-cast the rod
                EffectPlayer.play(player.getLocation(), config.getConfigurationSection("effects"));
            }
        }.runTaskLater(AtheriumEnchants.getInstance(), 5L);
    }
}
