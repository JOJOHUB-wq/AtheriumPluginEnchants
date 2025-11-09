package ua.atherium;

import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.commands.AteTabCompleter;
import ua.atherium.commands.CommandManager;
import ua.atherium.listeners.GUIListener;
import ua.atherium.listeners.GlobalEnchantListener;
import ua.atherium.listeners.IntegrationListener;
import ua.atherium.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import ua.atherium.commands.AteTabCompleter;
import ua.atherium.commands.CommandManager;
import ua.atherium.listeners.GUIListener;
import ua.atherium.listeners.GlobalEnchantListener;
import ua.atherium.listeners.IntegrationListener;
import ua.atherium.managers.ConfigManager;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.managers.GUIManager;
import ua.atherium.managers.LoreManager;
import ua.atherium.utils.PDCUtils;
import ua.atherium.utils.WorldGuardIntegration;

public final class AtheriumEnchants extends JavaPlugin {

    private static AtheriumEnchants instance;

    private ConfigManager configManager;
    private EnchantmentManager enchantmentManager;
    private GUIManager guiManager;
    private LoreManager loreManager;

    @Override
    public void onEnable() {
        instance = this;

        PDCUtils.init(this);
        configManager = new ConfigManager(this);
        enchantmentManager = new EnchantmentManager(this);
        loreManager = new LoreManager(enchantmentManager);
        guiManager = new GUIManager(this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            new WorldGuardIntegration(this);
        }

        getCommand("atheriumenchants").setExecutor(new CommandManager(this));
        getCommand("atheriumenchants").setTabCompleter(new AteTabCompleter(this));

        getServer().getPluginManager().registerEvents(new GlobalEnchantListener(this), this);
        getServer().getPluginManager().registerEvents(new IntegrationListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static AtheriumEnchants getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public LoreManager getLoreManager() {
        return loreManager;
    }
}
