package com.badbones69.crazyenchantments.paper.support.claims;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.support.interfaces.claims.ClaimSupport;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class LandsSupport implements ClaimSupport {

    private static final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);
    private static final LandsIntegration api = LandsIntegration.of(plugin);

    public boolean isFriendly(final Player player, final Player other) {
        Land land = api.getLandPlayer(other.getUniqueId()).getOwningLand();

        return (land != null && land.isTrusted(player.getUniqueId()));
    }

    public boolean inTerritory(final Player player) {
        final UUID uuid = player.getUniqueId();
        final Chunk chunk = player.getChunk();
        final Land land = api.getLandByChunk(player.getWorld(), chunk.getX(), chunk.getZ());

        if (land == null) return false;

        return (land.getOwnerUID() == uuid || land.isTrusted(uuid));
    }

    @Override
    public boolean canBreakBlock(final Player player, final Block block) {
        return false;
    }
}