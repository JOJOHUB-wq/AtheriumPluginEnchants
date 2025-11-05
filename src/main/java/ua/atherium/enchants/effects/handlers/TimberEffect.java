package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.listeners.GlobalEnchantListener;
import ua.atherium.utils.EffectPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TimberEffect implements EnchantmentEffect {

    private static final BlockFace[] FACES = {
            BlockFace.UP, BlockFace.DOWN,
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
            BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }

        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        Player player = blockBreakEvent.getPlayer();
        Block startBlock = blockBreakEvent.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (player.isSneaking() && AtheriumEnchants.getInstance().getConfigManager().getConfig().getBoolean("synergy.disable_area_effects_on_sneak", true)) {
            return;
        }

        if (!Tag.LOGS.isTagged(startBlock.getType())) {
            return;
        }

        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) {
            return;
        }

        int maxBlocks = levelConfig.getInt("max-blocks", 40);
        long delay = levelConfig.getLong("animation-ticks-per-block", 2);

        Set<Block> blocksToBreak = findTree(startBlock, maxBlocks);
        blocksToBreak.remove(startBlock); // The original block is handled by the event itself

        GlobalEnchantListener listener = (GlobalEnchantListener) context.get("listener");
        if (listener == null) return;


        new BukkitRunnable() {
            private final List<Block> orderedBlocks = new ArrayList<>(blocksToBreak);
            private int index = 0;

            @Override
            public void run() {
                if (index >= orderedBlocks.size()) {
                    this.cancel();
                    return;
                }

                Block block = orderedBlocks.get(index++);
                Collection<ItemStack> drops = block.getDrops(tool, player);
                block.setType(Material.AIR);
                damageTool(tool, player);
                EffectPlayer.play(block.getLocation().add(0.5, 0.5, 0.5), config.getConfigurationSection("effects"));


                // Run synergy for each block's drops
                listener.applySynergy(new ArrayList<>(drops), tool, player, block.getLocation());
            }
        }.runTaskTimer(AtheriumEnchants.getInstance(), delay, delay);
    }

    private Set<Block> findTree(Block startBlock, int maxBlocks) {
        Set<Block> tree = new HashSet<>();
        List<Block> toCheck = new ArrayList<>();
        toCheck.add(startBlock);

        while (!toCheck.isEmpty() && (maxBlocks == -1 || tree.size() < maxBlocks)) {
            Block current = toCheck.remove(0);
            if (tree.contains(current)) {
                continue;
            }
            if (!Tag.LOGS.isTagged(current.getType())) {
                continue;
            }
            tree.add(current);

            for (BlockFace face : FACES) {
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        for(int k = -1; k <= 1; k++) {
                            if (i == 0 && j == 0 && k == 0) continue;
                            Block relative = current.getRelative(i, j, k);
                            if (!tree.contains(relative) && Tag.LOGS.isTagged(relative.getType())) {
                                toCheck.add(relative);
                            }
                        }
                    }
                }
            }
        }
        return tree;
    }

    private void damageTool(ItemStack tool, Player player) {
        if (tool != null && tool.getItemMeta() instanceof Damageable) {
            ItemMeta meta = tool.getItemMeta();
            int unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
            if (new Random().nextDouble() <= (1.0 / (unbreakingLevel + 1))) {
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(meta);
                if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), "entity.item.break", 1, 1);
                }
            }
        }
    }
}
