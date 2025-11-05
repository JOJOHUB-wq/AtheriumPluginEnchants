package ua.atherium.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.enchants.effects.EffectFactory;
import ua.atherium.utils.ChatUtils;
import ua.atherium.utils.ItemBuilder;
import ua.atherium.utils.PDCUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantmentManager {

    private final AtheriumEnchants plugin;
    private final Map<String, CustomEnchant> registeredEnchants = new HashMap<>();

    public EnchantmentManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        loadEnchants();
    }

    public void loadEnchants() {
        registeredEnchants.clear();
        ConfigurationSection enchantsSection = plugin.getConfigManager().getEnchantsConfig().getRoot();
        if (enchantsSection == null) return;

        for (String key : enchantsSection.getKeys(false)) {
            ConfigurationSection enchantConfig = enchantsSection.getConfigurationSection(key);
            if (enchantConfig != null && enchantConfig.getBoolean("enabled", false)) {
                CustomEnchant enchant = new CustomEnchant(key, enchantConfig);
                enchant.setEffect(EffectFactory.createEffect(enchantConfig.getString("effect.type")));
                if (enchant.getEffect() != null) {
                    registeredEnchants.put(key.toLowerCase(), enchant);
                }
            }
        }
    }

    public CustomEnchant getEnchant(String key) {
        return registeredEnchants.get(key.toLowerCase());
    }

    public Map<String, CustomEnchant> getRegisteredEnchants() {
        return registeredEnchants;
    }

    public ItemStack createEnchantedBook(String enchantKey, int level) {
        CustomEnchant enchant = getEnchant(enchantKey);
        if (enchant == null) return null;

        ItemStack book = new ItemBuilder(Material.ENCHANTED_BOOK)
                .setDisplayName(enchant.getBookDisplayName() + " " + toRoman(level))
                .setLore(enchant.getDescription())
                .build();
        PDCUtils.addEnchant(book, enchant.getKey(), level);
        return book;
    }

    public boolean canApply(ItemStack item, CustomEnchant enchant) {
        if (item == null || enchant == null) {
            return false;
        }
        String itemType = item.getType().name();
        for (String applicableType : enchant.getAppliesTo()) {
            if (itemType.contains(applicableType)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasConflict(ItemStack item, CustomEnchant newEnchant) {
        Map<String, Integer> existingEnchants = PDCUtils.getEnchants(item);
        for (String existingKey : existingEnchants.keySet()) {
            CustomEnchant existingEnchant = getEnchant(existingKey);
            if (existingEnchant != null && (existingEnchant.getConflicts().contains(newEnchant.getKey()) || newEnchant.getConflicts().contains(existingEnchant.getKey()))) {
                return true;
            }
        }
        return false;
    }

    public static String toRoman(int number) {
        if (number <= 1 || number > 10) return ""; // Return empty for level 1
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return r[number];
    }
}
