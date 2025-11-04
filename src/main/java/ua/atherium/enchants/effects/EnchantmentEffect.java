package ua.atherium.enchants.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;

import java.util.Map;

public interface EnchantmentEffect {
    void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context);
}
