package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import ua.atherium.enchants.effects.EnchantmentEffect;
import java.util.Map;

public class StaticPotionEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!context.containsKey("player")) {
            return;
        }

        Player player = (Player) context.get("player");

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        PotionEffectType type = PotionEffectType.getByName(levelConfig.getString("potion-type"));
        int amplifier = levelConfig.getInt("amplifier");

        if (type != null) {
            // Apply effect for slightly longer than the check interval to avoid flickering
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, 20 * 10, amplifier, true, false));
        }
    }
}
