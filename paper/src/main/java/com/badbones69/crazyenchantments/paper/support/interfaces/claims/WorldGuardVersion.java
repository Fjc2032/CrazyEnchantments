package com.badbones69.crazyenchantments.paper.support.interfaces.claims;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface WorldGuardVersion {
    
    boolean allowsPVP(final Location loc);
    
    boolean allowsBreak(final Location loc);
    
    boolean allowsExplosions(final Location loc);
    
    boolean inRegion(final String regionName, final Location loc);
    
    boolean isMember(final Player player);
    
    boolean isOwner(final Player player);
    
}