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

import java.util.ArrayList;
import java.util.Collection;
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
        Block originalBlock = blockBreakEvent.getBlock();
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
        Location center = originalBlock.getLocation();

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

        for (Block block : blocksToBreak) {
            totalDrops.addAll(block.getDrops(tool, player));
            block.setType(Material.AIR);
            damageTool(tool, player);
        }
    }

    private boolean isBreakable(Block block, ItemStack tool, ConfigurationSection config) {
        if (block.isEmpty() || block.isLiquid()) {
            return false;
        }
        String toolType = tool.getType().name();
        String blockType = block.getType().name();

        ConfigurationSection toolTargets = config.getConfigurationSection("tool-targets");
        if (toolTargets == null) return false;

        String TRENCH_PICKAXE = toolType;
        if (TRENCH_PICKAXE.contains("PICKAXE")) TRENCH_PICKAXE = "PICKAXE";
        if (TRENCH_PICKAXE.contains("SHOVEL")) TRENCH_PICKAXE = "SHOVEL";
        if (TRENCH_PICKAXE.contains("AXE")) TRENCH_PICKAXE = "AXE";

        List<String> allowedMaterials = toolTargets.getStringList(TRENCH_PICKAXE);
        return allowedMaterials.contains(blockType);
    }

    private void damageTool(ItemStack tool, Player player) {
        if (tool.getItemMeta() instanceof Damageable) {
            ItemMeta meta = tool.getItemMeta();
            int unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
            if (new Random().nextDouble() <= (1.0 / (unbreakingLevel + 1))) {
                ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + 1);
                tool.setItemMeta(meta);
                if (((Damageable) meta).getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), "entity.item.break", 1, 1);
                }
            }
        }
    }
}
