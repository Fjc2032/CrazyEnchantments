package com.badbones69.crazyenchantments.paper.support;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.utils.WorldGuardUtils;
import com.badbones69.crazyenchantments.paper.support.claims.GriefPreventionSupport;
import com.badbones69.crazyenchantments.paper.support.claims.LandsSupport;
import com.badbones69.crazyenchantments.paper.support.claims.TownySupport;
import com.badbones69.crazyenchantments.paper.support.factions.FactionsUUIDSupport;
import com.badbones69.crazyenchantments.paper.support.interfaces.claims.ClaimSupport;
import com.gmail.nossr50.api.PartyAPI;
import com.google.common.collect.Maps;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class PluginSupport {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    private ClaimSupport claimPlugin = null;

    private WorldGuardUtils worldGuardUtils;

    private final Map<SupportedPlugins, Boolean> cachedPlugins = Maps.newHashMap();

    public void initializeWorldGuard() {
        if (SupportedPlugins.WORLDGUARD.isPluginLoaded() && SupportedPlugins.WORLDEDIT.isPluginLoaded()) {
            this.worldGuardUtils = new WorldGuardUtils();
            this.worldGuardUtils.init();
        }
    }

    public boolean inTerritory(final Player player) {
        if (this.claimPlugin != null) return this.claimPlugin.inTerritory(player);

        return SupportedPlugins.SUPERIORSKYBLOCK.isPluginLoaded() && this.starter.getSuperiorSkyBlockSupport().inTerritory(player);
    }

    public boolean isFriendly(final Entity pEntity, final Entity oEntity) {
        if (!(pEntity instanceof Player player) || !(oEntity instanceof Player otherPlayer)) return false;

        if (this.claimPlugin != null) return this.claimPlugin.isFriendly(player, otherPlayer);

        if (SupportedPlugins.SUPERIORSKYBLOCK.isPluginLoaded() && this.starter.getSuperiorSkyBlockSupport().isFriendly(player, otherPlayer)) return true;

        if (SupportedPlugins.MCMMO.isPluginLoaded()) return PartyAPI.inSameParty(player, otherPlayer);

        return false;

    }

    public boolean allowCombat(final Location location) {
        if (SupportedPlugins.TOWNYADVANCED.isPluginLoaded()) return TownySupport.allowsCombat(location);

        return !SupportedPlugins.WORLDEDIT.isPluginLoaded() || !SupportedPlugins.WORLDGUARD.isPluginLoaded() || this.worldGuardUtils.getWorldGuardSupport().allowsPVP(location);
    }

    public boolean allowDestruction(final Location location) {
        return !SupportedPlugins.WORLDEDIT.isPluginLoaded() || !SupportedPlugins.WORLDGUARD.isPluginLoaded() || this.worldGuardUtils.getWorldGuardSupport().allowsBreak(location);
    }

    public boolean allowExplosion(final Location location) {
        return !SupportedPlugins.WORLDEDIT.isPluginLoaded() || !SupportedPlugins.WORLDGUARD.isPluginLoaded() || this.worldGuardUtils.getWorldGuardSupport().allowsExplosions(location);
    }

    public void updateHooks() {
        this.cachedPlugins.clear();

        for (final SupportedPlugins supportedPlugin : SupportedPlugins.values()) {
            if (supportedPlugin.isPluginLoaded()) {

                final String website = supportedPlugin.getLoadedPlugin().getDescription().getWebsite();

                switch (supportedPlugin) {
                    case FACTIONS_UUID -> {
                        if (website != null) supportedPlugin.addPlugin(website.equals("https://www.spigotmc.org/resources/factionsuuid.1035/"));
                    }

                    case MCMMO -> {
                        if (website != null) supportedPlugin.addPlugin(website.equals("https://www.mcmmo.org"));
                    }

                    default -> supportedPlugin.addPlugin(true);
                }

                updateClaimHooks(supportedPlugin);
            } else {
                supportedPlugin.addPlugin(false);
            }
        }

        printHooks();
    }

    public WorldGuardUtils getWorldGuardUtils() {
        return this.worldGuardUtils;
    }

    public void updateClaimHooks(final SupportedPlugins supportedPlugin) {
        switch (supportedPlugin) {
            case GRIEF_PREVENTION -> this.claimPlugin = new GriefPreventionSupport();
            case TOWNYADVANCED -> this.claimPlugin = new TownySupport();
            case LANDS -> this.claimPlugin = new LandsSupport();
            case FACTIONS_UUID -> this.claimPlugin = new FactionsUUIDSupport();
        }
    }

    private final ComponentLogger logger = this.plugin.getComponentLogger();

    public void printHooks() {
        if (this.cachedPlugins.isEmpty()) updateHooks();

        final MiniMessage miniMessage = MiniMessage.miniMessage();

        this.logger.warn(miniMessage.deserialize("<dark_gray><b>=== <yellow><b>CrazyEnchantment Hook Status <dark_gray><b>==="));

        this.cachedPlugins.keySet().forEach(value -> {
            if (value.isPluginLoaded()) {
                this.logger.warn(miniMessage.deserialize("<gold><b>" + value.name() + " <green><b>FOUND"));
            } else {
                this.logger.warn(miniMessage.deserialize("<gold><b>" + value.name() + " <red><b>NOT FOUND"));
            }
        });
    }

    public enum SupportedPlugins {
        // Economy Plugins
        VAULT("Vault"),

        // Random Plugins
        MCMMO("McMMO"),

        // Faction Plugins
        FACTIONS_UUID("Factions"),

        GRIEF_PREVENTION("GriefPrevention"),

        // Sky Block Plugins
        SUPERIORSKYBLOCK("SuperiorSkyblock2"),

        // Region Protection
        WORLDGUARD("WorldGuard"),
        WORLDEDIT("WorldEdit"),

        TOWNYADVANCED("Towny"),

        LANDS("Lands"),

        PLOT_SQUARED("PlotSquared");

        private final String pluginName;

        SupportedPlugins(final String pluginName) {
            this.pluginName = pluginName;
        }

        @NotNull
        private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

        private final Server server = this.plugin.getServer();

        private final PluginManager pluginManager = this.server.getPluginManager();

        @NotNull
        private final Starter starter = this.plugin.getStarter();

        @NotNull
        private final PluginSupport pluginSupport = this.starter.getPluginSupport();

        public boolean isPluginLoaded() {
            return this.pluginManager.isPluginEnabled(this.pluginName);
        }

        public Plugin getLoadedPlugin() {
            return this.pluginManager.getPlugin(this.pluginName);
        }

        public boolean isCachedPluginLoaded() {
            return this.pluginSupport.cachedPlugins.get(this);
        }

        public void addPlugin(final boolean value) {
            this.pluginSupport.cachedPlugins.put(this, value);
        }

        public void removePlugin() {
            this.pluginSupport.cachedPlugins.remove(this);
        }

        public boolean isPluginEnabled() {
            return this.pluginSupport.cachedPlugins.get(this);
        }
    }
}