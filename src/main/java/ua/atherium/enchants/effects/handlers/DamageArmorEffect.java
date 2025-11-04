package ua.atherium.enchants.effects.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;
import java.util.Random;

public class DamageArmorEffect implements EnchantmentEffect {
    private static final Random RANDOM = new Random();
    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if(!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) e.getEntity();
            ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
            if (levelConfig == null) {
                return;
            }

            double chance = levelConfig.getDouble("chance", 15.0);
            if(RANDOM.nextDouble() * 100 > chance) {
                return;
            }

            int damageAmount = levelConfig.getInt("damage_amount", 5);

            boolean damaged = false;
            for (ItemStack armor : victim.getEquipment().getArmorContents()) {
                if (armor != null && armor.getItemMeta() instanceof Damageable) {
                    Damageable meta = (Damageable) armor.getItemMeta();
                    meta.setDamage(meta.getDamage() + damageAmount);
                    armor.setItemMeta(meta);
                    damaged = true;
                }
            }
            if (damaged) {
                EffectPlayer.play(victim.getLocation(), config.getConfigurationSection("effects"));
            }
        }
    }
}
