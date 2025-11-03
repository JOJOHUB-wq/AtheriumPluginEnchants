package ua.atherium.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ua.atherium.AtheriumEnchants;

import java.util.ArrayList;
import java.util.List;

public class AECommand implements CommandExecutor, TabCompleter {

    private final AtheriumEnchants plugin;

    public AECommand(AtheriumEnchants plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // TODO: Show help message
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "get":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ae get <enchantment> [level]");
                    return true;
                }
                String enchantmentName = args[1];
                int level = 1;
                if (args.length > 2) {
                    try {
                        level = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid level specified.");
                        return true;
                    }
                }
                plugin.getEnchantManager().giveEnchantedBook(sender, enchantmentName, level);
                break;
            case "apply":
                // TODO: Handle apply subcommand
                break;
            case "menu":
                if (sender instanceof Player) {
                    plugin.getMenuManager().openMenu((Player) sender, "enchant_shop");
                } else {
                    sender.sendMessage("This command can only be used by a player.");
                }
                break;
            case "reload":
                plugin.getConfigManager().reloadConfig();
                sender.sendMessage("Configuration reloaded.");
                break;
            default:
                // TODO: Show unknown command message
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("get");
            subCommands.add("apply");
            subCommands.add("menu");
            subCommands.add("reload");
            return subCommands;
        }
        return null;
    }
}
