package ua.atherium.enchants;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Vampirism extends CustomEnchant {

    public Vampirism() {
        super("Vampirism", 3, 0, 0.1);
    }

    @Override
    public void handleEvent(Event event) {
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getDamager();
        int level = player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER);
        double damage = e.getDamage();
        double healAmount = damage * (0.05 * level); // 5% of damage per level

        if (Math.random() < getChance()) {
            player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
        }
    }
}
