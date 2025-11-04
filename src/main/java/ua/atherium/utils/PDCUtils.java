package ua.atherium.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.AtheriumEnchants;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PDCUtils {

    private static NamespacedKey ENCHANTS_KEY;
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

    public static void init(AtheriumEnchants plugin) {
        if (ENCHANTS_KEY == null) {
            ENCHANTS_KEY = new NamespacedKey(plugin, "custom_enchants");
        }
    }

    private static PersistentDataContainer getContainer(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer();
    }

    public static void addEnchant(ItemStack item, String enchantKey, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Map<String, Integer> enchants = getEnchants(item);
        enchants.put(enchantKey, level);
        container.set(ENCHANTS_KEY, PersistentDataType.STRING, GSON.toJson(enchants));
        item.setItemMeta(meta);
    }

    public static void removeEnchant(ItemStack item, String enchantKey) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Map<String, Integer> enchants = getEnchants(item);
        if (enchants.remove(enchantKey) != null) {
            container.set(ENCHANTS_KEY, PersistentDataType.STRING, GSON.toJson(enchants));
            item.setItemMeta(meta);
        }
    }

    public static Map<String, Integer> getEnchants(ItemStack item) {
        PersistentDataContainer container = getContainer(item);
        if (container == null || !container.has(ENCHANTS_KEY, PersistentDataType.STRING)) {
            return new HashMap<>();
        }
        String json = container.get(ENCHANTS_KEY, PersistentDataType.STRING);
        Map<String, Integer> enchants = GSON.fromJson(json, TYPE);
        return enchants == null ? new HashMap<>() : enchants;
    }

    public static boolean hasEnchant(ItemStack item, String enchantKey) {
        return getEnchants(item).containsKey(enchantKey);
    }

    public static int getLevel(ItemStack item, String enchantKey) {
        return getEnchants(item).getOrDefault(enchantKey, 0);
    }
}
