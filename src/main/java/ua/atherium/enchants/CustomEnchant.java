package ua.atherium.enchants;

import org.bukkit.configuration.ConfigurationSection;
import ua.atherium.enchants.effects.EnchantmentEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomEnchant {

    private final String key;
    private final boolean enabled;
    private final String displayName;
    private final List<String> description;
    private final String rarity;
    private final int maxLevel;
    private final List<String> conflicts;
    private final List<String> appliesTo;
    private final boolean enchantingTableEnabled;
    private final int chanceWeight;
    private final List<EnchantmentTrigger> triggers;
    private final ConfigurationSection effectConfig;
    private EnchantmentEffect effect;

    public CustomEnchant(String key, ConfigurationSection config) {
        this.key = key;
        this.enabled = config.getBoolean("enabled", false);
        this.displayName = config.getString("display_name", "");
        this.description = config.getStringList("description");
        this.rarity = config.getString("rarity", "COMMON");
        this.maxLevel = config.getInt("max_level", 1);
        this.conflicts = config.getStringList("conflicts");
        this.appliesTo = config.getStringList("applies_to");
        this.enchantingTableEnabled = config.getBoolean("enchanting_table.enabled", false);
        this.chanceWeight = config.getInt("enchanting_table.chance_weight", 1);
        this.triggers = new ArrayList<>();
        String triggerString = config.getString("trigger", "");
        if (triggerString.contains(",")) {
            for (String s : triggerString.split(",")) {
                triggers.add(EnchantmentTrigger.valueOf(s.trim().toUpperCase()));
            }
        } else {
            triggers.add(EnchantmentTrigger.valueOf(triggerString.toUpperCase()));
        }

        this.effectConfig = config.getConfigurationSection("effect");
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getRarity() {
        return rarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public List<String> getAppliesTo() {
        return appliesTo;
    }

    public boolean isEnchantingTableEnabled() {
        return enchantingTableEnabled;
    }

    public int getChanceWeight() {
        return chanceWeight;
    }

    public List<EnchantmentTrigger> getTriggers() {
        return triggers;
    }

    public ConfigurationSection getEffectConfig() {
        return effectConfig;
    }

    public EnchantmentEffect getEffect() {
        return effect;
    }

    public void setEffect(EnchantmentEffect effect) {
        this.effect = effect;
    }
}
