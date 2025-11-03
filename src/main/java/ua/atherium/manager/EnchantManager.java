package ua.atherium.manager;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ua.atherium.enchants.CustomEnchant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnchantManager {

    private final Map<String, CustomEnchant> enchantments = new HashMap<>();

    public void registerEnchant(CustomEnchant enchant) {
        enchantments.put(enchant.getName().toLowerCase(), enchant);
    }

    public void unregisterEnchant(CustomEnchant enchant) {
        enchantments.remove(enchant.getName().toLowerCase());
    }

    public CustomEnchant getEnchant(String name) {
        return enchantments.get(name.toLowerCase());
    }

    public Map<String, CustomEnchant> getEnchantments() {
        return enchantments;
    }

    public void giveEnchantedBook(CommandSender sender, String enchantmentName, int level) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return;
        }

        CustomEnchant enchant = getEnchant(enchantmentName);
        if (enchant == null) {
            sender.sendMessage("Unknown enchantment: " + enchantmentName);
            return;
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            sender.sendMessage("Invalid level for this enchantment.");
            return;
        }

        Player player = (Player) sender;
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName("§b" + enchant.getName() + " " + level);
        meta.setLore(Collections.singletonList("§7Custom Enchantment"));
        meta.getPersistentDataContainer().set(enchant.getKey(), PersistentDataType.INTEGER, level);
        book.setItemMeta(meta);

        player.getInventory().addItem(book);
        player.sendMessage("You have received the enchanted book.");
    }
}
