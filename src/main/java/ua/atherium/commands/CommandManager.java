package ua.atherium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.utils.ChatUtils;
import ua.atherium.utils.PDCUtils;

public class CommandManager implements CommandExecutor {

    private final AtheriumEnchants plugin;
    private final EnchantmentManager enchantmentManager;

    public CommandManager(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Send help message
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "menu":
                handleMenu(sender);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "enchant":
                handleEnchant(sender, args);
                break;
            default:
                // Send help message
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("atherium.reload")) {
            sender.sendMessage(getMessage("no_permission"));
            return;
        }
        plugin.getConfigManager().reloadConfigs();
        plugin.getEnchantmentManager().loadEnchants();
        plugin.getGuiManager().loadGUIs();
        sender.sendMessage(getMessage("reload"));
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("atherium.menu")) {
            sender.sendMessage(getMessage("no_permission"));
            return;
        }
        plugin.getGuiManager().openGUI((Player) sender, "main");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("atherium.give")) {
            sender.sendMessage(getMessage("no_permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /ate give <player> <enchant> [level]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("player_not_found"));
            return;
        }

        CustomEnchant enchant = enchantmentManager.getEnchant(args[2]);
        if (enchant == null) {
            sender.sendMessage(getMessage("enchant_not_found").replace("%enchant%", args[2]));
            return;
        }

        int level = 1;
        if (args.length > 3) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid level.");
                return;
            }
        }
        level = Math.min(Math.max(level, 1), enchant.getMaxLevel());

        ItemStack book = enchantmentManager.createEnchantedBook(enchant.getKey(), level);
        target.getInventory().addItem(book);

        sender.sendMessage(getMessage("give_book")
                .replace("%player%", target.getName())
                .replace("%enchant%", enchant.getDisplayName())
                .replace("%level%", EnchantmentManager.toRoman(level)));
        target.sendMessage(getMessage("receive_book")
                .replace("%enchant%", enchant.getDisplayName())
                .replace("%level%", EnchantmentManager.toRoman(level)));
    }

    private void handleEnchant(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }
        if (!sender.hasPermission("atherium.enchant")) {
            sender.sendMessage(getMessage("no_permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /ate enchant <enchant> [level]");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(getMessage("item_not_in_hand"));
            return;
        }

        CustomEnchant enchant = enchantmentManager.getEnchant(args[1]);
        if (enchant == null) {
            sender.sendMessage(getMessage("enchant_not_found").replace("%enchant%", args[1]));
            return;
        }

        if (!enchantmentManager.canApply(item, enchant)) {
            sender.sendMessage(getMessage("enchant_not_applicable"));
            return;
        }

        if (enchantmentManager.hasConflict(item, enchant)) {
            sender.sendMessage(getMessage("enchant_conflict"));
            return;
        }

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid level.");
                return;
            }
        }
        level = Math.min(Math.max(level, 1), enchant.getMaxLevel());

        PDCUtils.addEnchant(item, enchant.getKey(), level);
        sender.sendMessage(getMessage("enchant_applied")
                .replace("%enchant%", enchant.getDisplayName())
                .replace("%level%", EnchantmentManager.toRoman(level)));
    }

    private String getMessage(String path) {
        String prefix = plugin.getConfigManager().getConfig().getString("prefix", "");
        String message = plugin.getConfigManager().getConfig().getString("messages." + path, "&cMessage not found.");
        return ChatUtils.colorize(message.replace("%prefix%", prefix));
    }
}
