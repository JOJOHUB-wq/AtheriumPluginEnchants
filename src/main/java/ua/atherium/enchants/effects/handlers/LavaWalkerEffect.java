package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class LavaWalkerEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof PlayerMoveEvent)) {
            return;
        }

        PlayerMoveEvent e = (PlayerMoveEvent) event;
        Player player = e.getPlayer();
        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        int radius = levelConfig.getInt("radius", 2);
        boolean magma = false;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = player.getLocation().clone().add(x, -1, z).getBlock();
                if (block.getType() == Material.LAVA) {
                    block.setType(Material.MAGMA_BLOCK);
                    magma = true;
                }
            }
        }
        if (magma) {
            EffectPlayer.play(player.getLocation(), config.getConfigurationSection("effects"));
        }
    }
}
