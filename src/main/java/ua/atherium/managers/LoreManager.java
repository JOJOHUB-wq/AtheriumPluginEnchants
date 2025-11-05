package ua.atherium.managers;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.utils.ChatUtils;
import ua.atherium.utils.PDCUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoreManager {

    private final EnchantmentManager enchantmentManager;

    public LoreManager(EnchantmentManager enchantmentManager) {
        this.enchantmentManager = enchantmentManager;
    }

    public void updateLore(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> originalLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        List<String> newLore = new ArrayList<>();

        // Add custom enchantments to the top
        Map<String, Integer> enchants = PDCUtils.getEnchants(item);
        if (!enchants.isEmpty()) {
            enchants.entrySet().stream()
                    .map(entry -> {
                        CustomEnchant enchant = enchantmentManager.getEnchant(entry.getKey());
                        if (enchant == null) return null;
                        String roman = EnchantmentManager.toRoman(entry.getValue());
                        return ChatUtils.colorize(enchant.getDisplayName() + (roman.isEmpty() ? "" : " " + roman));
                    })
                    .filter(line -> line != null && !line.isEmpty())
                    .forEach(newLore::add);
        }

        // Add original lore, filtering out old custom enchantments
        if (originalLore != null) {
            originalLore.stream()
                .filter(line -> !line.contains("§")) // A simple way to filter out old colorized lines
                .forEach(newLore::add);
        }

        // Remove duplicates
        List<String> finalLore = newLore.stream().distinct().collect(Collectors.toList());

        meta.setLore(finalLore);

        // Add glint if there are custom enchants and no vanilla enchants
        if (!enchants.isEmpty() && meta.getEnchants().isEmpty()) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, false);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
    }
}
