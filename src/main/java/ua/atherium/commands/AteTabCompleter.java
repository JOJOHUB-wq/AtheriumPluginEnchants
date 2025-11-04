package ua.atherium.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.utils.PDCUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AteTabCompleter implements TabCompleter {

    private final EnchantmentManager enchantmentManager;
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "menu", "give", "enchant");

    public AteTabCompleter(AtheriumEnchants plugin) {
        this.enchantmentManager = plugin.getEnchantmentManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("enchant")) {
            if (!(sender instanceof Player)) {
                return Collections.emptyList();
            }

            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                return Collections.emptyList();
            }

            List<String> suggestions = enchantmentManager.getRegisteredEnchants().values().stream()
                    .filter(enchant -> enchantmentManager.canApply(item, enchant))
                    .filter(enchant -> !PDCUtils.hasEnchant(item, enchant.getKey()))
                    .filter(enchant -> !enchantmentManager.hasConflict(item, enchant))
                    .map(CustomEnchant::getKey)
                    .collect(Collectors.toList());

            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                List<String> enchantKeys = new ArrayList<>(enchantmentManager.getRegisteredEnchants().keySet());
                return StringUtil.copyPartialMatches(args[1], enchantKeys, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}
