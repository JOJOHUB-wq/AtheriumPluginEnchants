package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.enchants.effects.EnchantmentEffect;
import ua.atherium.utils.EffectPlayer;

import java.util.Map;
import java.util.Random;

public class FryUpEffect implements EnchantmentEffect {

    private static final Random RANDOM = new Random();

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof PlayerFishEvent) || ((PlayerFishEvent) event).getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        PlayerFishEvent fishEvent = (PlayerFishEvent) event;
        ConfigurationSection levelConfig = config.getConfigurationSection("level-" + level);
        if (levelConfig == null) return;

        double chance = levelConfig.getDouble("chance");
        if (RANDOM.nextDouble() * 100 < chance) {
            if (fishEvent.getCaught() instanceof Item) {
                Item caught = (Item) fishEvent.getCaught();
                ItemStack itemStack = caught.getItemStack();
                Material cooked = getCookedVersion(itemStack.getType());
                if (cooked != null) {
                    itemStack.setType(cooked);
                    caught.setItemStack(itemStack);
                    EffectPlayer.play(caught.getLocation(), config.getConfigurationSection("effects"));
                }
            }
        }
    }

    private Material getCookedVersion(Material material) {
        switch (material) {
            case COD: return Material.COOKED_COD;
            case SALMON: return Material.COOKED_SALMON;
            default: return null;
        }
    }
}
