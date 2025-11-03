package ua.atherium.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ua.atherium.AtheriumEnchants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MenuManager {

    private final AtheriumEnchants plugin;
    private final Map<String, GUI> menus = new HashMap<>();

    public MenuManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        loadMenus();
    }

    public void loadMenus() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        for (File menuFile : menusFolder.listFiles()) {
            if (menuFile.getName().endsWith(".yml")) {
                FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);
                String menuName = menuFile.getName().replace(".yml", "");
                String title = menuConfig.getString("title", "Menu");
                int size = menuConfig.getInt("size", 27);
                menus.put(menuName, new ShopGUI(title, size, menuConfig));
            }
        }
    }

    public GUI getMenu(String name) {
        return menus.get(name);
    }

    public void openMenu(Player player, String name) {
        GUI menu = getMenu(name);
        if (menu != null) {
            menu.open(player);
        }
    }
}
