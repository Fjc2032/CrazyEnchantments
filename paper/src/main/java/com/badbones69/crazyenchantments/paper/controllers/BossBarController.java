package com.badbones69.crazyenchantments.paper.controllers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarController { //todo() replace with adventure api

    private final Map<UUID, BossBar> bossBars;
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    public BossBarController() {
        this.bossBars = new HashMap<>();
    }

    /**
     *
     * @param player {@link Player}
     * @return true if the player currently has a boss bar.
     */
    public boolean hasBossBar(final Player player) {
        return this.bossBars.containsKey(player.getUniqueId());
    }

    /**
     *
     * @param player the {@link Player} whose boss bar you want to get
     * @return the current boss bar or null.
     */
    public BossBar getBossBar(final Player player) {
        return bossBars.get(player.getUniqueId());
    }

    private void createBossBars(final Player player, final Component displayText, final float progress) {
        if (hasBossBar(player)) return;

        final BossBar bossBar = BossBar.bossBar(displayText, progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);

        this.bossBars.put(player.getUniqueId(), bossBar);

        player.showBossBar(bossBar);

        new FoliaScheduler(this.plugin, null, player) {
            @Override
            public void run() {
                removeBossBar(player);
            }
        }.runDelayed(600L);
    }

    /**
     * Updates the current boss bar or creates a
     * new one if the player does not have one yet
     * @param player {@link Player} whose boss bar you want to update
     * @param text the message that you want to be displayed by the boss bar
     * @param progress value between 0f and 1f of how much the progress bar should be filled
     */
    public void updateBossBar(final Player player, final Component text, final float progress) {
        if (!hasBossBar(player)) {
            createBossBars(player, text, progress);
        } else {
            this.bossBars.replace(player.getUniqueId(), getBossBar(player).name(text).progress(progress));
        }

        player.showBossBar(getBossBar(player));
    }

    /**
     * @see #updateBossBar(Player, Component, float)
     */
    public void updateBossBar(final Player player, final String text, final float progress) {
        updateBossBar(player, ColorUtils.legacyTranslateColourCodes(text), progress);
    }

    /**
     * Removes the players boss bar
     * @param player the {@link Player} whose boss bar you wish to remove
     */
    public void removeBossBar(final Player player) {
        if (!hasBossBar(player)) return;

        player.hideBossBar(getBossBar(player));

        this.bossBars.remove(player.getUniqueId());
    }

    private final Server server = this.plugin.getServer();

    /**
     * Deletes all boss bars.
     */
    public void removeAllBossBars() {
        if (this.bossBars.isEmpty()) return;

        for (Map.Entry<UUID, BossBar> entry : this.bossBars.entrySet()) {
            Player player = this.server.getPlayer(entry.getKey());

            if (player == null) {
                continue;
            }

            player.hideBossBar(entry.getValue());
        }

        this.bossBars.clear();
    }
}