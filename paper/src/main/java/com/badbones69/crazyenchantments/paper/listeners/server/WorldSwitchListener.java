package com.badbones69.crazyenchantments.paper.listeners.server;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldSwitchListener implements Listener {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final ConfigOptions options = this.plugin.getOptions();

    @EventHandler(ignoreCancelled = true)
    public void onWorldSwitch(PlayerChangedWorldEvent event) {
        if (this.options.isRefreshingEffectsOnWorldChange()) this.crazyManager.updatePlayerEffects(event.getPlayer());
    }
}