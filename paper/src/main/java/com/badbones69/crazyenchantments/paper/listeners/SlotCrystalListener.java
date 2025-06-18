package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.Messages;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class SlotCrystalListener implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        final ItemStack crystalItem = event.getCursor();
        final ItemStack item = event.getCurrentItem();

        if (item == null || item.isEmpty() || !isSlotCrystal(crystalItem) || isSlotCrystal(item)) return;

        final int maxEnchants = this.crazyManager.getPlayerMaxEnchantments(player);
        final int enchAmount = this.enchantmentBookSettings.getEnchantmentAmount(item, this.crazyManager.checkVanillaLimit());
        final int baseEnchants = this.crazyManager.getPlayerBaseEnchantments(player);
        final int limiter = this.crazyManager.getEnchantmentLimiter(item);

        event.setCancelled(true);

        if (enchAmount >= maxEnchants) {
            player.sendMessage(Messages.HIT_ENCHANTMENT_MAX.getMessage());

            return;
        }
        if ((baseEnchants - limiter) >= maxEnchants) {
            player.sendMessage(Messages.MAX_SLOTS_UNLOCKED.getMessage());

            return;
        }

        crystalItem.setAmount(crystalItem.getAmount() - 1);

        event.getCursor().setAmount(crystalItem.getAmount());
        event.setCurrentItem(this.crazyManager.changeEnchantmentLimiter(item, -1));

        player.sendMessage(Messages.APPLIED_SLOT_CRYSTAL.getMessage(new HashMap<>(4) {{
            put("%slot%", String.valueOf(-(limiter - 1)));
            put("%maxEnchants%", String.valueOf(maxEnchants));
            put("%enchantAmount%", String.valueOf(enchAmount));
            put("baseEnchants", String.valueOf(baseEnchants));
        }}));
    }

    private boolean isSlotCrystal(final ItemStack crystalItem) {
        if (crystalItem == null || crystalItem.isEmpty()) return false;

        return crystalItem.getPersistentDataContainer().has(DataKeys.slot_crystal.getNamespacedKey());
    }
}