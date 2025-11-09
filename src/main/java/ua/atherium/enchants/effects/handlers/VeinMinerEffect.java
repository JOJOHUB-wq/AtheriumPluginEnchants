package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VeinMinerEffect implements EnchantmentEffect {

    private static final BlockFace[] FACES = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof BlockBreakEvent)) return;

        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        Player player = blockBreakEvent.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block startBlock = blockBreakEvent.getBlock();

        if (!isOre(startBlock.getType())) return;

        List<ItemStack> totalDrops = (List<ItemStack>) context.get("drops");
        Set<Block> vein = findVein(startBlock, config.getInt("max_blocks", 64));
        vein.remove(startBlock);

        for (Block block : vein) {
            totalDrops.addAll(block.getDrops(tool));
            block.setType(Material.AIR);
            EffectPlayer.play(block.getLocation().add(0.5, 0.5, 0.5), config.getConfigurationSection("effects"));
        }
    }

    private Set<Block> findVein(Block startBlock, int limit) {
        Set<Block> vein = new HashSet<>();
        Set<Block> toCheck = new HashSet<>();
        toCheck.add(startBlock);
        Material type = startBlock.getType();

        while (!toCheck.isEmpty() && vein.size() < limit) {
            Block current = toCheck.iterator().next();
            toCheck.remove(current);
            vein.add(current);

            for (BlockFace face : FACES) {
                Block relative = current.getRelative(face);
                if (relative.getType() == type && !vein.contains(relative)) {
                    toCheck.add(relative);
                }
            }
        }
        return vein;
    }

    private boolean isOre(Material material) {
        return material.toString().endsWith("_ORE");
    }
}
