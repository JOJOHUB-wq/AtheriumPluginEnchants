package ua.atherium.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.atherium.AtheriumEnchants;
import ua.atherium.gui.AnimatedGUI;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {

    private final AtheriumEnchants plugin;
    private final Map<String, AnimatedGUI> cachedGuis = new HashMap<>();

    public GUIManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        loadGUIs();
    }

    public void loadGUIs() {
        cachedGuis.clear();
        Map<String, FileConfiguration> menuConfigs = plugin.getConfigManager().getMenuConfigs();
        for (Map.Entry<String, FileConfiguration> entry : menuConfigs.entrySet()) {
            cachedGuis.put(entry.getKey(), new AnimatedGUI(entry.getValue(), plugin.getEnchantmentManager()));
        }
    }

    public void openGUI(Player player, String menuId) {
        AnimatedGUI gui = cachedGuis.get(menuId);
        if (gui != null) {
            gui.open(player);
        }
    }
}
