package ua.atherium.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.utils.PDCUtils;
import java.util.Map;
import java.util.Random;

public class IntegrationListener implements Listener {

    private final AtheriumEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final Random random = new Random();

    public IntegrationListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("enchanting_table.enabled", true)) {
            return;
        }

        double chance = plugin.getConfigManager().getConfig().getDouble("enchanting_table.custom_enchant_chance", 30.0);
        if (random.nextDouble() * 100 > chance) {
            return;
        }

        ItemStack item = event.getItem();
        CustomEnchant chosenEnchant = enchantmentManager.getRegisteredEnchants().values().stream()
                .filter(CustomEnchant::isEnchantingTableEnabled)
                .filter(enchant -> enchantmentManager.canApply(item, enchant))
                .filter(enchant -> !enchantmentManager.hasConflict(item, enchant))
                .findAny()
                .orElse(null);

        if (chosenEnchant != null) {
            int level = 1 + random.nextInt(chosenEnchant.getMaxLevel());
            PDCUtils.addEnchant(item, chosenEnchant.getKey(), level);
            // Optionally, remove a vanilla enchant to "replace" it
            if (!event.getEnchantsToAdd().isEmpty()) {
                Enchantment toRemove = (Enchantment) event.getEnchantsToAdd().keySet().toArray()[0];
                event.getEnchantsToAdd().remove(toRemove);
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        ItemStack result = event.getResult();

        if (first == null || second == null) {
            return;
        }

        if (second.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) second.getItemMeta();
            Map<String, Integer> bookEnchants = PDCUtils.getEnchants(second);

            if (!bookEnchants.isEmpty()) {
                String enchantKey = bookEnchants.keySet().stream().findFirst().get();
                int level = bookEnchants.get(enchantKey);
                CustomEnchant enchant = enchantmentManager.getEnchant(enchantKey);

                if (enchant != null && enchantmentManager.canApply(first, enchant) && !enchantmentManager.hasConflict(first, enchant)) {
                    result = first.clone();
                    PDCUtils.addEnchant(result, enchantKey, level);
                    event.setResult(result);
                    event.getInventory().setRepairCost(10); // Example cost
                }
            }
        } else {
             Map<String, Integer> firstEnchants = PDCUtils.getEnchants(first);
             Map<String, Integer> secondEnchants = PDCUtils.getEnchants(second);
             if(!firstEnchants.isEmpty() || !secondEnchants.isEmpty()){
                 result = first.clone();
                 for(Map.Entry<String, Integer> entry : secondEnchants.entrySet()){
                     if(!PDCUtils.hasEnchant(result, entry.getKey())) {
                         PDCUtils.addEnchant(result, entry.getKey(), entry.getValue());
                     }
                 }
                event.setResult(result);
                event.getInventory().setRepairCost(10);
             }
        }
    }
}
