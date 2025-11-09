package ua.atherium.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.utils.ChatUtils;
import ua.atherium.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnimatedGUI implements InventoryHolder {

    private final Inventory inventory;
    private final ConfigurationSection config;
    private final Map<Integer, List<String>> clickActions = new HashMap<>();
    private final EnchantmentManager enchantmentManager;

    public AnimatedGUI(ConfigurationSection config, EnchantmentManager enchantmentManager) {
        this.config = config;
        this.enchantmentManager = enchantmentManager;
        this.inventory = Bukkit.createInventory(this, config.getInt("size", 54), ChatUtils.colorize(config.getString("title", "Atherium Enchants")));
    }

    public void open(Player player) {
        player.openInventory(inventory);
        startAnimation();
    }

    private void startAnimation() {
        ConfigurationSection animationSection = config.getConfigurationSection("animation");
        if (animationSection == null) return;

        final Map<Integer, List<String>> timeline = parseTimeline(animationSection.getStringList("timeline"));

        new BukkitRunnable() {
            private int currentTick = 0;
            private final int maxTick = timeline.keySet().stream().max(Integer::compareTo).orElse(0);

            @Override
            public void run() {
                if (timeline.containsKey(currentTick)) {
                    List<String> opcodes = timeline.get(currentTick);
                    for (String opcode : opcodes) {
                        executeOpcode(opcode);
                    }
                }

                if (currentTick >= maxTick) {
                    this.cancel();
                }
                currentTick++;
            }
        }.runTaskTimer(AtheriumEnchants.getInstance(), 0L, animationSection.getLong("base_tick", 1L));
    }

    private Map<Integer, List<String>> parseTimeline(List<String> rawTimeline) {
        Map<Integer, List<String>> timeline = new TreeMap<>();
        for (String entry : rawTimeline) {
            String[] parts = entry.split(":", 2);
            try {
                int tick = Integer.parseInt(parts[0].trim());
                timeline.computeIfAbsent(tick, k -> new ArrayList<>()).add(parts[1].trim());
            } catch (NumberFormatException ignored) {}
        }
        return timeline;
    }

    private void executeOpcode(String opcode) {
        String[] parts = opcode.split(":", 2);
        String command = parts[0].trim();
        String[] args = parts[1].trim().split("\\s+");
        String itemKey = args[0];
        String[] slots = args[1].split(",");

        if (command.equalsIgnoreCase("set")) {
            ItemStack item = getItem(itemKey);
            if (item == null) return;
            for (String slotStr : slots) {
                try {
                    int slot = Integer.parseInt(slotStr.trim());
                    inventory.setItem(slot, item);
                    clickActions.put(slot, config.getStringList("items." + itemKey + ".click_actions"));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private ItemStack getItem(String itemKey) {
        ConfigurationSection itemConfig = config.getConfigurationSection("items." + itemKey);
        if (itemConfig == null) return null;

        Material material = Material.matchMaterial(itemConfig.getString("material", "STONE"));
        if (material == null) return null;

        ItemBuilder builder;
        if (itemConfig.contains("enchant_key")) {
            String enchantKey = itemConfig.getString("enchant_key");
            CustomEnchant enchant = enchantmentManager.getEnchant(enchantKey);
            if (enchant == null) return null;

            List<String> lore = new ArrayList<>(enchant.getDescription(1));
            lore.add(" ");
            lore.add(ChatUtils.colorize("&fЗастосовується до: &e" + String.join(", ", enchant.getAppliesTo())));
            if (!enchant.getConflicts().isEmpty()) {
                lore.add(ChatUtils.colorize("&fКонфліктує з: &c" + String.join(", ", enchant.getConflicts())));
            }
            lore.add(ChatUtils.colorize("&fРідкість: &b" + enchant.getRarity()));

            builder = new ItemBuilder(material)
                    .setDisplayName(enchant.getDisplayName())
                    .setLore(lore);
        } else {
            builder = new ItemBuilder(material)
                    .setDisplayName(itemConfig.getString("display_name", " "))
                    .setLore(itemConfig.getStringList("lore"));
        }

        itemConfig.getStringList("item_flags").forEach(flag -> {
            try {
                builder.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        });

        itemConfig.getStringList("enchantments").forEach(ench -> {
            String[] parts = ench.split(";");
            Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
            if (enchantment != null) {
                try {
                    builder.addEnchant(enchantment, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {}
            }
        });

        return builder.build();
    }

    public List<String> getClickActions(int slot) {
        return clickActions.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
