package ua.atherium.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.atherium.AtheriumEnchants;
import ua.atherium.enchants.CustomEnchant;
import ua.atherium.enchants.EnchantmentTrigger;
import ua.atherium.managers.EnchantmentManager;
import ua.atherium.utils.PDCUtils;
import ua.atherium.utils.WorldGuardIntegration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GlobalEnchantListener implements Listener {

    private final AtheriumEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final Map<UUID, ItemStack> thrownTridents = new HashMap<>();

    public GlobalEnchantListener(AtheriumEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        startStaticEffectTask();
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            ItemStack tridentItem = player.getInventory().getItemInMainHand();
            if (tridentItem.getType() == Material.TRIDENT) {
                thrownTridents.put(event.getEntity().getUniqueId(), tridentItem.clone());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!WorldGuardIntegration.getInstance().isAllowed(player)) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) {
            return;
        }

        Map<String, Integer> enchantLevels = PDCUtils.getEnchants(tool);
        if (enchantLevels.isEmpty()) {
            return;
        }

        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.BLOCK_BREAK);
        if (enchants.isEmpty()) {
            return;
        }

        // Synergy context
        Map<String, Object> context = new HashMap<>();
        List<ItemStack> masterDrops = new ArrayList<>(event.getBlock().getDrops(tool, player));
        context.put("drops", masterDrops);
        context.put("listener", this);
        context.put("player", player);
        context.put("tool", tool);


        for (CustomEnchant enchant : enchants) {
            enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
        }

        // Finalize drops
        List<ItemStack> finalDrops = (List<ItemStack>) context.get("drops");
        event.setDropItems(false); // Always cancel default drops and handle them manually

        if (context.containsKey("prevent_default_drops") && (boolean) context.get("prevent_default_drops")) {
             if (!finalDrops.isEmpty()) {
                // Telekinesis or other effects already handled the drops
            }
        } else {
            dropItems(event.getBlock().getLocation(), finalDrops);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (!WorldGuardIntegration.getInstance().isAllowed(player)) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.getType() != Material.AIR) {
            Map<String, Integer> enchantLevels = PDCUtils.getEnchants(tool);
            if (!enchantLevels.isEmpty()) {
                List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.ATTACK_ENTITY);
                Map<String, Object> context = new HashMap<>();
                context.put("player", player);
                context.put("tool", tool);

                for (CustomEnchant enchant : enchants) {
                    enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
                }
            }
        }
        if(event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            for(ItemStack armor : victim.getInventory().getArmorContents()) {
                if(armor != null) {
                    Map<String, Integer> enchantLevels = PDCUtils.getEnchants(armor);
                     if (!enchantLevels.isEmpty()) {
                        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.DEFENSE_PLAYER);
                        Map<String, Object> context = new HashMap<>();
                         for (CustomEnchant enchant : enchants) {
                            enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Arrow) && !(event.getEntity() instanceof Trident)) return;

        Player shooter = null;
        ItemStack weapon = null;
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
             if(!(arrow.getShooter() instanceof Player)) return;
             shooter = (Player) arrow.getShooter();
             if (!WorldGuardIntegration.getInstance().isAllowed(shooter)) return;
             weapon = shooter.getInventory().getItemInMainHand();
        } else {
            Trident trident = (Trident) event.getEntity();
            if(!(trident.getShooter() instanceof Player)) return;
            shooter = (Player) trident.getShooter();
            weapon = thrownTridents.remove(trident.getUniqueId());
        }

        if(weapon == null) return;

        Map<String, Integer> enchantLevels = PDCUtils.getEnchants(weapon);
        if(enchantLevels.isEmpty()) return;

        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.BOW_SHOOT);
        Map<String, Object> context = new HashMap<>();
        if(event.getHitEntity() != null) {
             context.put("is_headshot", event.getHitEntity().getBoundingBox().contains(event.getEntity().getLocation().toVector()));
        }

        for (CustomEnchant enchant : enchants) {
            enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
        }

        if(event.getHitEntity() instanceof Player) {
             Player victim = (Player) event.getHitEntity();
             EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(shooter, victim, EntityDamageByEntityEvent.DamageCause.PROJECTILE, 5);
             for(CustomEnchant enchant : getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.BOW_SHOOT)) {
                 if(enchant.getEffect().getClass().getSimpleName().equals("SniperEffect")) {
                     enchant.getEffect().execute(damageEvent, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
                 }
             }
             victim.damage(damageEvent.getFinalDamage());
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) return;

        Map<String, Integer> enchantLevels = PDCUtils.getEnchants(tool);
        if (enchantLevels.isEmpty()) return;

        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.ATTACK_ENTITY); // Re-use ATTACK_ENTITY for drops
         if (enchants.isEmpty()) {
            return;
        }
        Map<String, Object> context = new HashMap<>();
        List<ItemStack> masterDrops = new ArrayList<>(event.getDrops());
        context.put("drops", masterDrops);

        for (CustomEnchant enchant : enchants) {
            enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
        }

        if (context.containsKey("prevent_default_drops") && (boolean) context.get("prevent_default_drops")) {
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if(boots != null) {
            Map<String, Integer> enchantLevels = PDCUtils.getEnchants(boots);
             if (!enchantLevels.isEmpty()) {
                List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.MOVE);
                Map<String, Object> context = new HashMap<>();
                 for (CustomEnchant enchant : enchants) {
                    enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
                }
            }
        }
    }


    private void startStaticEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    for (ItemStack armor : player.getInventory().getArmorContents()) {
                        if (armor != null) {
                            Map<String, Integer> enchantLevels = PDCUtils.getEnchants(armor);
                            if (enchantLevels.isEmpty()) continue;

                            List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.EQUIP);
                            Map<String, Object> context = new HashMap<>();
                            context.put("player", player);

                            for (CustomEnchant enchant : enchants) {
                                enchant.getEffect().execute(null, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // Check every 5 seconds
    }

    public void applySynergy(List<ItemStack> drops, ItemStack tool, Player player, Location location) {
        if (drops.isEmpty()) return;

        Map<String, Integer> enchantLevels = PDCUtils.getEnchants(tool);
        if (enchantLevels.isEmpty()) {
            dropItems(location, drops);
            return;
        }

        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.BLOCK_BREAK);
        if (enchants.isEmpty()) {
            dropItems(location, drops);
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("drops", drops);

        // Cloned logic from onBlockBreak for synergy application
        for (CustomEnchant enchant : enchants) {
            if (enchant.getEffect().getClass().getSimpleName().equals("TrenchEffect") || enchant.getEffect().getClass().getSimpleName().equals("TimberEffect")) {
                continue; // Prevent recursion
            }
            enchant.getEffect().execute(new BlockBreakEvent(location.getBlock(), player), enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
        }

        if (context.containsKey("prevent_default_drops") && (boolean) context.get("prevent_default_drops")) {
            // Telekinesis already handled adding to inventory
        } else {
            dropItems(location, (List<ItemStack>) context.get("drops"));
        }
    }

    private void dropItems(Location location, List<ItemStack> drops) {
        for(ItemStack drop : drops) {
            location.getWorld().dropItemNaturally(location, drop);
        }
    }


    private List<CustomEnchant> getEnchantsByTrigger(Map<String, Integer> enchantLevels, EnchantmentTrigger trigger) {
        return enchantLevels.keySet().stream()
                .map(enchantmentManager::getEnchant)
                .filter(enchant -> enchant != null && enchant.getTriggers().contains(trigger))
                .sorted(Comparator.comparingInt(e -> getPriority(e.getEffect().getClass().getSimpleName())))
                .collect(Collectors.toList());
    }

    private int getPriority(String effectClassName) {
        switch (effectClassName) {
            case "TrenchEffect":
            case "TimberEffect":
                return 1;
            case "SmeltEffect":
                return 2;
            case "TelekinesisEffect":
                return 3;
            default:
                return 99;
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!WorldGuardIntegration.getInstance().isAllowed(player)) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() != Material.FISHING_ROD) return;

        Map<String, Integer> enchantLevels = PDCUtils.getEnchants(tool);
        if (enchantLevels.isEmpty()) return;

        List<CustomEnchant> enchants = getEnchantsByTrigger(enchantLevels, EnchantmentTrigger.FISHING);
        if (enchants.isEmpty()) return;

        Map<String, Object> context = new HashMap<>();
        for (CustomEnchant enchant : enchants) {
            enchant.getEffect().execute(event, enchant.getEffectConfig(), enchantLevels.get(enchant.getKey()), context);
        }
    }
}
