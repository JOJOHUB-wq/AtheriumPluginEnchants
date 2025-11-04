package ua.atherium.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.atherium.AtheriumEnchants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final AtheriumEnchants plugin;
    private FileConfiguration config;
    private FileConfiguration enchantsConfig;
    private final Map<String, FileConfiguration> menuConfigs = new HashMap<>();

    public ConfigManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
        if (!enchantsFile.exists()) {
            plugin.saveResource("enchants.yml", false);
        }
        enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);

        loadMenuConfigs();
    }

    private void loadMenuConfigs() {
        menuConfigs.clear();
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
            // Save default menus
            plugin.saveResource("menus/main.yml", false);
            plugin.saveResource("menus/weapons.yml", false);
            plugin.saveResource("menus/tools.yml", false);
            plugin.saveResource("menus/armor.yml", false);
        }

        File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (menuFiles != null) {
            for (File menuFile : menuFiles) {
                String menuId = menuFile.getName().replace(".yml", "");
                menuConfigs.put(menuId, YamlConfiguration.loadConfiguration(menuFile));
            }
        }
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        File enchantsFile = new File(plugin.getDataFolder(), "enchants.yml");
        enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);

        loadMenuConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getEnchantsConfig() {
        return enchantsConfig;
    }

    public FileConfiguration getMenuConfig(String menuId) {
        return menuConfigs.get(menuId);
    }

    public Map<String, FileConfiguration> getMenuConfigs() {
        return menuConfigs;
    }
}
