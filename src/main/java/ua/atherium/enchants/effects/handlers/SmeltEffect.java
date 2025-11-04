package ua.atherium.enchants.effects.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ua.atherium.AtheriumEnchants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmeltEffect implements EnchantmentEffect {

    private final Map<Material, Material> smeltingRecipes;

    public SmeltEffect() {
        ConfigurationSection recipesSection = AtheriumEnchants.getInstance().getConfigManager().getConfig().getConfigurationSection("smelting-recipes");
        this.smeltingRecipes = recipesSection.getKeys(false).stream()
                .collect(Collectors.toMap(
                        key -> Material.valueOf(key.toUpperCase()),
                        key -> Material.valueOf(recipesSection.getString(key).toUpperCase())
                ));
    }

    @Override
    public void execute(Event event, ConfigurationSection config, int level, Map<String, Object> context) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }

        List<ItemStack> drops = (List<ItemStack>) context.get("drops");
        if (drops == null) {
            return;
        }

        List<ItemStack> smeltedDrops = new ArrayList<>();
        for (ItemStack drop : drops) {
            Material result = smeltingRecipes.get(drop.getType());
            if (result != null) {
                smeltedDrops.add(new ItemStack(result, drop.getAmount()));
            } else {
                smeltedDrops.add(drop);
            }
        }

        context.put("drops", smeltedDrops);
    }
}
