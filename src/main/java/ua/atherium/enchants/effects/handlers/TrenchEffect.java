package ua.atherium.enchants.effects.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrenchEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }

        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        Player player = blockBreakEvent.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (player.isSneaking() && AtheriumEnchants.getInstance().getConfigManager().getConfig().getBoolean("synergy.disable_area_effects_on_sneak", true)) {
            return;
        }

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        String[] shape = levelConfig.getString("shape", "3x1x3").split("x");
        int width = (Integer.parseInt(shape[0]) - 1) / 2;
        int height = (Integer.parseInt(shape[1]) - 1) / 2;
        int depth = (Integer.parseInt(shape[2]) - 1) / 2;

        List<Block> blocksToBreak = new ArrayList<>();
        Location center = blockBreakEvent.getBlock().getLocation();

        for (int x = -width; x <= width; x++) {
            for (int y = -height; y <= height; y++) {
                for (int z = -depth; z <= depth; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block targetBlock = center.clone().add(x, y, z).getBlock();
                    if (isBreakable(targetBlock, tool, config)) {
                        blocksToBreak.add(targetBlock);
                    }
                }
            }
        }

        List<ItemStack> totalDrops = (List<ItemStack>) context.get("drops");
        ConfigurationSection effectsConfig = config.getConfigurationSection("effects");

        for (Block block : blocksToBreak) {
            totalDrops.addAll(block.getDrops(tool, player));
            block.setType(Material.AIR);
            damageTool(tool, player);
            EffectPlayer.play(block.getLocation().add(0.5, 0.5, 0.5), effectsConfig);
        }
        // Play effect for the original block as well
        EffectPlayer.play(center.clone().add(0.5, 0.5, 0.5), effectsConfig);
    }

    private boolean isBreakable(Block block, ItemStack tool, ConfigurationSection config) {
        if (block.isEmpty() || block.isLiquid()) {
            return false;
        }
        String toolType = tool.getType().name();
        String blockType = block.getType().name();

        ConfigurationSection toolTargets = config.getConfigurationSection("tool-targets");
        if (toolTargets == null) return false;

        String resolvedToolType = "";
        if (toolType.contains("PICKAXE")) {
            resolvedToolType = "PICKAXE";
        } else if (toolType.contains("SHOVEL")) {
            resolvedToolType = "SHOVEL";
        } else if (toolType.contains("AXE")) {
            resolvedToolType = "AXE";
        }

        if (resolvedToolType.isEmpty()) return false;

        List<String> allowedMaterials = toolTargets.getStringList(resolvedToolType);
        return allowedMaterials.contains(blockType);
    }

    private void damageTool(ItemStack tool, Player player) {
        if (tool != null && tool.hasItemMeta() && tool.getItemMeta() instanceof Damageable) {
            Damageable meta = (Damageable) tool.getItemMeta();
            int unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
            if (new Random().nextDouble() <= (1.0 / (unbreakingLevel + 1))) {
                meta.setDamage(meta.getDamage() + 1);
                tool.setItemMeta(meta);
                if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), "entity.item.break", 1, 1);
                }
            }
        }
    }
}
