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
import java.util.stream.Collectors;

public class AnimatedGUI implements InventoryHolder {

    private final Inventory inventory;
    private final ConfigurationSection config;
    private final Map<Integer, List<String>> clickActions = new HashMap<>();

    public AnimatedGUI(ConfigurationSection config, EnchantmentManager enchantmentManager) {
        this.config = config;
        this.inventory = Bukkit.createInventory(this, config.getInt("size", 54), ChatUtils.colorize(config.getString("title", "Atherium Enchants")));
        loadItems(enchantmentManager);
    }

    private void loadItems(EnchantmentManager enchantmentManager) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(key);
            if (itemConfig == null) continue;

            ItemBuilder builder;
            if (itemConfig.contains("enchant_key")) {
                String enchantKey = itemConfig.getString("enchant_key");
                CustomEnchant enchant = enchantmentManager.getEnchant(enchantKey);
                if (enchant == null) continue;
                builder = new ItemBuilder(Material.valueOf(itemConfig.getString("material", "ENCHANTED_BOOK")))
                        .setDisplayName(enchant.getDisplayName())
                        .setLore(enchant.getDescription());
            } else {
                builder = new ItemBuilder(Material.valueOf(itemConfig.getString("material", "STONE")))
                        .setDisplayName(itemConfig.getString("display_name", " "))
                        .setLore(itemConfig.getStringList("lore"));
            }

            if (itemConfig.contains("item_flags")) {
                itemConfig.getStringList("item_flags").forEach(flag -> builder.addItemFlags(ItemFlag.valueOf(flag)));
            }
            if (itemConfig.contains("enchantments")) {
                itemConfig.getStringList("enchantments").forEach(ench -> {
                    String[] parts = ench.split(";");
                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    if (enchantment != null) {
                        builder.addEnchant(enchantment, Integer.parseInt(parts[1]));
                    }
                });
            }
            // This is a template item, it will be placed by animation
            // we just need to store the click actions if any
            if (itemConfig.contains("click_actions")) {
                // This is a simplified approach, we will map actions to the key, not the item itself
                // The animation will place items and we'll retrieve actions by item key later
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        startAnimation();
    }

    private void startAnimation() {
        ConfigurationSection animationSection = config.getConfigurationSection("animation");
        if (animationSection == null) return;

        new BukkitRunnable() {
            private final Map<Integer, List<String>> opcodesByTick = parseOpcodes();
            private int currentTick = 0;

            @Override
            public void run() {
                if (!opcodesByTick.containsKey(currentTick)) {
                    if (currentTick > opcodesByTick.keySet().stream().max(Integer::compareTo).orElse(0)) {
                        this.cancel();
                    }
                    currentTick++;
                    return;
                }

                List<String> opcodes = opcodesByTick.get(currentTick);
                for (String opcode : opcodes) {
                    String[] parts = opcode.split(":");
                    String command = parts[0];
                    String[] args = parts[1].trim().split("\\s+");
                    String itemKey = args[0];
                    String[] slots = args[1].split(",");

                    if (command.equalsIgnoreCase("set")) {
                        ItemStack item = getItem(itemKey);
                        for (String slotStr : slots) {
                            int slot = Integer.parseInt(slotStr);
                            inventory.setItem(slot, item);
                            clickActions.put(slot, getClickActions(itemKey));
                        }
                    }
                }
                currentTick++;
            }
        }.runTaskTimer(AtheriumEnchants.getInstance(), 0L, animationSection.getLong("tick", 1L));
    }


    private Map<Integer, List<String>> parseOpcodes() {
        Map<Integer, List<String>> opcodesByTick = new HashMap<>();
        ConfigurationSection animationSection = config.getConfigurationSection("animation");
        for (String key : animationSection.getKeys(false)) {
            if (key.startsWith("tick")) {
                try {
                    int tick = Integer.parseInt(key.substring(4));
                    opcodesByTick.put(tick, animationSection.getStringList("opcodes"));
                } catch (NumberFormatException e) {
                    // ignore, not a tick key
                }
            }
        }
        //This is a workaround for the user providing tick as a child of animation instead of part of the key
        ConfigurationSection tickSection = config.getConfigurationSection("animation");
        if (tickSection != null) {
            for(String key : tickSection.getKeys(false)) {
                if (!key.equalsIgnoreCase("tick") && key.startsWith("tick")) {
                    int tick = Integer.parseInt(key.replace("tick-", ""));
                    opcodesByTick.put(tick, tickSection.getStringList(key));
                } else if(key.equalsIgnoreCase("opcodes")) {
                    // Fallback for single opcode list
                    int tick = tickSection.getInt("tick", 1);
                    opcodesByTick.put(tick, tickSection.getStringList("opcodes"));
                }
            }
        }
        return opcodesByTick;
    }


    private ItemStack getItem(String itemKey) {
        ConfigurationSection itemConfig = config.getConfigurationSection("items." + itemKey);
        if (itemConfig == null) return new ItemStack(Material.AIR);
        ItemBuilder builder;
        EnchantmentManager enchantmentManager = AtheriumEnchants.getInstance().getEnchantmentManager();
        if (itemConfig.contains("enchant_key")) {
            String enchantKey = itemConfig.getString("enchant_key");
            CustomEnchant enchant = enchantmentManager.getEnchant(enchantKey);
            if (enchant == null) return new ItemStack(Material.AIR);

            List<String> lore = new ArrayList<>(enchant.getDescription());
            lore.add(" ");
            lore.add(ChatUtils.colorize("&fПрименимо к: &e" + String.join(", ", enchant.getAppliesTo())));
            if (!enchant.getConflicts().isEmpty()) {
                lore.add(ChatUtils.colorize("&fКонфликтует с: &c" + String.join(", ", enchant.getConflicts())));
            }
            lore.add(ChatUtils.colorize("&fРедкость: &b" + enchant.getRarity()));


            builder = new ItemBuilder(Material.valueOf(itemConfig.getString("material", "ENCHANTED_BOOK")))
                    .setDisplayName(enchant.getDisplayName())
                    .setLore(lore);
        } else {
            builder = new ItemBuilder(Material.valueOf(itemConfig.getString("material", "STONE")))
                    .setDisplayName(itemConfig.getString("display_name", " "))
                    .setLore(itemConfig.getStringList("lore"));
        }

        if (itemConfig.contains("item_flags")) {
            itemConfig.getStringList("item_flags").forEach(flag -> builder.addItemFlags(ItemFlag.valueOf(flag.toUpperCase())));
        }
        if (itemConfig.contains("enchantments")) {
            itemConfig.getStringList("enchantments").forEach(ench -> {
                String[] parts = ench.split(";");
                Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                if (enchantment != null) {
                    builder.addEnchant(enchantment, Integer.parseInt(parts[1]));
                }
            });
        }
        return builder.build();
    }


    public List<String> getClickActions(int slot) {
        return clickActions.get(slot);
    }

    private List<String> getClickActions(String itemKey) {
        return config.getStringList("items." + itemKey + ".click_actions");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}