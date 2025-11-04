package ua.atherium.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EffectPlayer {

    public static void play(Location location, ConfigurationSection config) {
        if (config == null) return;

        if (config.getBoolean("sound.enabled", false)) {
            try {
                Sound sound = Sound.valueOf(config.getString("sound.name", "").toUpperCase());
                float volume = (float) config.getDouble("sound.volume", 1.0);
                float pitch = (float) config.getDouble("sound.pitch", 1.0);
                location.getWorld().playSound(location, sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                // Invalid sound name in config
            }
        }

        if (config.getBoolean("particles.enabled", false)) {
            try {
                Particle particle = Particle.valueOf(config.getString("particles.name", "").toUpperCase());
                int count = config.getInt("particles.count", 10);
                double offset = config.getDouble("particles.offset", 0.5);
                location.getWorld().spawnParticle(particle, location, count, offset, offset, offset, 0);
            } catch (IllegalArgumentException e) {
                // Invalid particle name in config
            }
        }
    }
}
