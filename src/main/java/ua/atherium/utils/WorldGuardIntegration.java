package ua.atherium.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ua.atherium.AtheriumEnchants;

import java.util.List;

public class WorldGuardIntegration {

    private static WorldGuardIntegration instance;
    private List<String> disabledRegions;

    public WorldGuardIntegration(AtheriumEnchants plugin) {
        instance = this;
        disabledRegions = plugin.getConfigManager().getConfig().getStringList("disabled-regions");
    }

    public static WorldGuardIntegration getInstance() {
        return instance;
    }

    public boolean isAllowed(Player player) {
        if (disabledRegions.isEmpty()) {
            return true;
        }

        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        for (ProtectedRegion region : regions) {
            if (disabledRegions.contains(region.getId())) {
                return false;
            }
        }
        return true;
    }
}
