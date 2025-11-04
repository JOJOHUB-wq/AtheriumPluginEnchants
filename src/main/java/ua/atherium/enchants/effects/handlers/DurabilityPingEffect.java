package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.ChatUtils;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class DurabilityPingEffect implements EnchantmentEffect {
    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!context.containsKey("player")) {
            return;
        }

        Player player = (Player) context.get("player");
        ItemStack tool = (ItemStack) context.get("tool");

        if (tool.getItemMeta() instanceof Damageable) {
            Damageable meta = (Damageable) tool.getItemMeta();
            ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
            if(levelConfig == null) {
                return;
            }
            int threshold = levelConfig.getInt("threshold", 50);
            if (tool.getType().getMaxDurability() - meta.getDamage() <= threshold) {
                player.sendMessage(ChatUtils.colorize(levelConfig.getString("message")));
                EffectPlayer.play(player.getLocation(), config.getConfigurationSection("effects"));
            }
        }
    }
}
