package com.badbones69.crazyenchantments.paper.support.claims;

import com.badbones69.crazyenchantments.paper.support.interfaces.claims.ClaimSupport;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jline.utils.Log;

public class TownySupport implements ClaimSupport {

    public boolean isFriendly(final Player player, final Player other) {
        return CombatUtil.isAlly(player.getName(), other.getName());
    }

    public boolean inTerritory(final Player player) {
        final TownyAPI api = TownyAPI.getInstance();

        if (api == null) return true;

        final TownBlock block = api.getTownBlock(player.getLocation());

        final Resident resident = api.getResident(player.getUniqueId());

        try {
            if (block != null && block.hasTown()) {
                assert resident != null;

                if (resident.hasTown() && resident.getTown().equals(block.getTown())) return true;
            }
        } catch (final NotRegisteredException e) {
            Log.error(e);
        }

        return false;
    }

    public boolean canBreakBlock(final Player player, final Block block) { return true; }

    public static boolean allowsCombat(final Location location) {
        TownyAPI api = TownyAPI.getInstance();

        if (api == null) return true;

        TownBlock block = api.getTownBlock(location);

        return block == null || !CombatUtil.preventPvP(block.getWorld(), block);
    }
}