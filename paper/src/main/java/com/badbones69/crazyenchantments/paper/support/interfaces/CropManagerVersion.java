package com.badbones69.crazyenchantments.paper.support.interfaces;

import org.bukkit.block.Block;

public interface CropManagerVersion {
    
    void fullyGrowPlant(final Block block);
    
    boolean isFullyGrown(final Block block);
    
    void hydrateSoil(final Block soil);
    
}