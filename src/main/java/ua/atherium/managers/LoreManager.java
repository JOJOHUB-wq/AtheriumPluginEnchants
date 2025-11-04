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
        List<String> newLore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove old custom enchant lore
        newLore.removeIf(line -> line.startsWith(ChatUtils.colorize("&#")));

        Map<String, Integer> enchants = PDCUtils.getEnchants(item);
        if (!enchants.isEmpty()) {
            List<String> enchantLore = enchants.entrySet().stream()
                    .map(entry -> {
                        CustomEnchant enchant = enchantmentManager.getEnchant(entry.getKey());
                        if (enchant == null) return "";
                        return enchant.getDisplayName() + " " + EnchantmentManager.toRoman(entry.getValue());
                    })
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
            newLore.addAll(0, enchantLore);
        }

        meta.setLore(newLore);
        item.setItemMeta(meta);
    }
}
