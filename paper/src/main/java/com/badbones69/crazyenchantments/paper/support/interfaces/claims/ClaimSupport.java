package com.badbones69.crazyenchantments.paper.support.interfaces.claims;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ClaimSupport {

    String wilderness = "Wilderness";

    boolean isFriendly(final Player player, final Player other);

    boolean inTerritory(final Player player);

    boolean canBreakBlock(final Player player, final Block block);

}