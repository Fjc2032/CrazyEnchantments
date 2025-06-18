package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.AuraActiveEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuraListener implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Server server = this.plugin.getServer();

    private final PluginManager pluginManager = this.server.getPluginManager();

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    private final CEnchantments[] AURA_ENCHANTMENTS = {
            CEnchantments.BLIZZARD,
            CEnchantments.ACIDRAIN,
            CEnchantments.SANDSTORM,
            CEnchantments.RADIANT,
            CEnchantments.INTIMIDATE
    };

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        final List<Player> players = getNearbyPlayers(player);

        if (players.isEmpty()) return;

        final EntityEquipment playerEquipment = player.getEquipment();

        for (ItemStack item : playerEquipment.getArmorContents()) { // The player that moves.
            final Map<CEnchantment, Integer> itemEnchantments = this.enchantmentBookSettings.getEnchantments(item);

            itemEnchantments.forEach((enchantment, level) -> {
                CEnchantments enchantmentEnum = getAuraEnchantmentEnum(enchantment);

                if (enchantmentEnum != null) players.forEach((other) -> this.pluginManager.callEvent(new AuraActiveEvent(player, other, enchantmentEnum, level)));
            });
        }

        for (final Player other : players) {
            final EntityEquipment otherEquipment = other.getEquipment();

            for (final ItemStack item : otherEquipment.getArmorContents()) { // The other players moving.
                final Map<CEnchantment, Integer> itemEnchantments = this.enchantmentBookSettings.getEnchantments(item);

                itemEnchantments.forEach((enchantment, level) -> {
                    final CEnchantments enchantmentEnum = getAuraEnchantmentEnum(enchantment);

                    if (enchantmentEnum != null) this.pluginManager.callEvent(new AuraActiveEvent(other, player, enchantmentEnum, level));
                });
            }
        }
    }

    private CEnchantments getAuraEnchantmentEnum(final CEnchantment enchantment) {
        return Arrays.stream(AURA_ENCHANTMENTS).filter(enchantmentEnum -> enchantmentEnum.getName().equals(enchantment.getName())).findFirst().orElse(null);
    }

    private List<Player> getNearbyPlayers(final Player player) {
        return player.getNearbyEntities(3, 3, 3).stream().filter((entity) ->
                entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())).map(entity -> (Player) entity).collect(Collectors.toList());
    }
}