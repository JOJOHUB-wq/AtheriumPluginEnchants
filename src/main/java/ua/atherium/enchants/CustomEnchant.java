package ua.atherium.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import ua.atherium.AtheriumEnchants;

public abstract class CustomEnchant {

    private final String name;
    private final NamespacedKey key;
    private final int maxLevel;
    private final int cooldown;
    private final double chance;

    public CustomEnchant(String name, int maxLevel, int cooldown, double chance) {
        this.name = name;
        this.key = new NamespacedKey(AtheriumEnchants.getInstance(), name.toLowerCase());
        this.maxLevel = maxLevel;
        this.cooldown = cooldown;
        this.chance = chance;
    }

    public String getName() {
        return name;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getChance() {
        return chance;
    }

    public abstract void handleEvent(Event event);
}
