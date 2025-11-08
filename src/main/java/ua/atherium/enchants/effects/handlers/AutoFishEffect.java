package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;

public class AutoFishEffect implements EnchantmentEffect {

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof PlayerFishEvent)) {
            return;
        }

        PlayerFishEvent fishEvent = (PlayerFishEvent) event;
        Player player = fishEvent.getPlayer();

        // Крок 1: Автоматично підсікаємо при клюванні
        if (fishEvent.getState() == PlayerFishEvent.State.BITE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Симулюємо підсічку, притягуючи поплавок до гравця
                    Vector direction = player.getLocation().toVector().subtract(fishEvent.getHook().getLocation().toVector());
                    fishEvent.getHook().setVelocity(direction.multiply(0.18)); // Швидкість підібрана для імітації
                }
            }.runTask(AtheriumEnchants.getInstance());
        }

        // Крок 2: Автоматично перезакидаємо після вдалої рибалки
        if (fishEvent.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            EffectPlayer.play(player.getLocation(), config.getConfigurationSection("effects"));

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack rod = player.getInventory().getItemInMainHand();
                    if (rod != null && rod.getType() == Material.FISHING_ROD) {
                        // Надійний спосіб перезакинути вудку: імітуємо, що гравець знову взяв її в руки
                        ItemStack temp = rod.clone();
                        player.getInventory().setItemInMainHand(null);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.getInventory().setItemInMainHand(temp);
                            }
                        }.runTaskLater(AtheriumEnchants.getInstance(), 1L);
                    }
                }
            }.runTaskLater(AtheriumEnchants.getInstance(), 15L); // Затримка для реалістичності
        }
    }
}
