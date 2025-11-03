package ua.atherium.gui;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI extends GUI {

    private final FileConfiguration config;

    public ShopGUI(String title, int size, FileConfiguration config) {
        super(title, size);
        this.config = config;
        createInventory();
    }

    private void createInventory() {
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            Material material = Material.matchMaterial(config.getString(path + ".material", "STONE"));
            int slot = config.getInt(path + ".slot", 0);
            String name = config.getString(path + ".name", "");
            List<String> lore = config.getStringList(path + ".lore");

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name.replace("&", "§"));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace("&", "§"));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
        }
    }

    public String getCommand(int slot) {
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            if (config.getInt(path + ".slot") == slot) {
                return config.getString(path + ".command");
            }
        }
        return null;
    }
}
