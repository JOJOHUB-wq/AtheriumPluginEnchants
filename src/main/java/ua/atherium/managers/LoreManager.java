package ua.atherium.managers;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
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
        List<String> originalLore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        List<String> newLore = new ArrayList<>();

        // Зберігаємо ванільні зачарування
        Map<Enchantment, Integer> vanillaEnchants = meta.getEnchants();
        boolean hasCustomEnchants = !PDCUtils.getEnchants(item).isEmpty();

        // Додаємо кастомні зачарування
        PDCUtils.getEnchants(item).forEach((key, level) -> {
            CustomEnchant enchant = enchantmentManager.getEnchant(key);
            if (enchant != null) {
                String roman = EnchantmentManager.toRoman(level);
                newLore.add(ChatUtils.colorize(enchant.getDisplayName() + (roman.isEmpty() ? "" : " " + roman)));
            }
        });

        // Додаємо ванільні зачарування, якщо є кастомні
        if (hasCustomEnchants && !vanillaEnchants.isEmpty()) {
             vanillaEnchants.forEach((enchant, level) -> {
                String roman = EnchantmentManager.toRoman(level);
                newLore.add(ChatUtils.colorize("&7" + getEnchantmentName(enchant) + (roman.isEmpty() ? "" : " " + roman)));
            });
        }


        // Додаємо оригінальний лор, фільтруючи старі рядки зачарувань
        originalLore.stream()
            .filter(line -> !isEnchantmentLine(line))
            .forEach(newLore::add);

        meta.setLore(newLore);

        // Додаємо світіння, якщо є кастомні чари, і приховуємо стандартний список
        if (hasCustomEnchants) {
            if (meta.getEnchants().isEmpty()) {
                meta.addEnchant(Enchantment.LURE, 1, false);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
    }

    private boolean isEnchantmentLine(String line) {
        String stripped = ChatUtils.colorize(line).replaceAll("§[a-f0-9]", "");
        // Проста перевірка, чи схожий рядок на опис зачарування
        return enchantmentManager.getRegisteredEnchants().values().stream()
                .anyMatch(enchant -> stripped.startsWith(ChatUtils.colorize(enchant.getDisplayName()).replaceAll("§[a-f0-9]", ""))) ||
                isVanillaEnchantmentLine(stripped);
    }

    private boolean isVanillaEnchantmentLine(String line) {
        for (Enchantment enchant : Enchantment.values()) {
            if (line.startsWith(getEnchantmentName(enchant))) {
                return true;
            }
        }
        return false;
    }

    // Метод для отримання "чистої" назви ванільного зачарування
    private String getEnchantmentName(Enchantment enchant) {
        String name = enchant.getKey().getKey();
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ");
    }
}
