package ua.atherium;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.config.ConfigManager;
import ua.atherium.gui.MenuManager;
import ua.atherium.manager.EnchantManager;

public final class AtheriumEnchants extends JavaPlugin {

    private static AtheriumEnchants instance;
    private ConfigManager configManager;
    private EnchantManager enchantManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(this);
        enchantManager = new EnchantManager();
        menuManager = new MenuManager(this);
        getLogger().info("AtheriumEnchants has been enabled!");
        getServer().getPluginManager().registerEvents(new ua.atherium.listeners.EnchantmentListener(this), this);
        getServer().getPluginManager().registerEvents(new ua.atherium.listeners.GUIListener(), this);
        enchantManager.registerEnchant(new ua.atherium.enchants.Vampirism());
        this.getCommand("ae").setExecutor(new ua.atherium.commands.AECommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("AtheriumEnchants has been disabled!");
    }

    public static AtheriumEnchants getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}
