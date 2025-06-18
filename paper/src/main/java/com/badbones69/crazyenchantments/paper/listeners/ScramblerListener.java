package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.Messages;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.objects.items.ScramblerData;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScramblerListener implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final ComponentLogger logger = this.plugin.getComponentLogger();

    private final Server server = this.plugin.getServer();

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final ScramblerData scramblerData = this.crazyManager.getScramblerData();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    private final Map<Player, ScheduledTask> roll = new HashMap<>();

    private void setGlass(final Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            if (slot != 4) {
                inventory.setItem(slot, ColorUtils.getRandomPaneColor().setName(" ").build());
                inventory.setItem(slot + 18, ColorUtils.getRandomPaneColor().setName(" ").build());
            } else {
                inventory.setItem(slot, this.scramblerData.getPointer().build());
                inventory.setItem(slot + 18, this.scramblerData.getPointer().build());
            }
        }
    }

    public void openScrambler(final Player player, final ItemStack book) {
        final Inventory inventory = this.server.createInventory(null, 27, this.scramblerData.getGuiName()); //todo() inventory holders...

        setGlass(inventory);

        for (int slot = 9; slot > 8 && slot < 18; slot++) {
            inventory.setItem(slot, this.enchantmentBookSettings.getNewScrambledBook(book));
        }

        player.openInventory(inventory);

        startScrambler(player, inventory, book);
    }

    private void startScrambler(final Player player, final Inventory inventory, final ItemStack book) {
        this.roll.put(player, new FoliaScheduler(this.plugin, null, player) {
            int time = 1;
            int full = 0;
            int open = 0;

            @Override
            public void run() {
                if (this.full <= 50) { // When spinning.
                    moveItems(inventory, book);

                    setGlass(inventory);

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }

                this.open++;

                if (this.open >= 5) {
                    player.openInventory(inventory);

                    this.open = 0;
                }

                this.full++;

                if (this.full > 51) {
                    if (slowSpin().contains(this.time)) { // When Slowing Down
                        moveItems(inventory, book);

                        setGlass(inventory);

                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }

                    this.time++;

                    if (this.time == 60) { // When done
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                        cancel();

                        roll.remove(player);

                        final ItemStack item = inventory.getItem(13);

                        if (item != null) {
                            ItemStack clone;

                            clone = item.withType(enchantmentBookSettings.getEnchantmentBookItem().getType());

                            methods.setDurability(item, methods.getDurability(enchantmentBookSettings.getEnchantmentBookItem()));

                            methods.addItemToInventory(player, clone);
                        } else {
                            logger.error("The item at slot 13 is null, We cannot continue!");
                        }

                    } else if (this.time > 60) { // Just in case the cancel fails.
                        cancel();
                    }
                }
            }
        }.runAtFixedRate(1, 1));
    }

    private List<Integer> slowSpin() {
        final List<Integer> slow = new ArrayList<>();

        int full = 120;
        int cut = 15;

        for (int amount = 120; cut > 0; full--) {
            if (full <= amount - cut || full >= amount - cut) {
                slow.add(amount);

                amount = amount - cut;

                cut--;
            }
        }

        return slow;
    }

    private void moveItems(final Inventory inventory, final ItemStack book) {
        List<ItemStack> items = new ArrayList<>();

        for (int slot = 9; slot > 8 && slot < 17; slot++) {
            items.add(inventory.getItem(slot));
        }

        ItemStack newBook = this.enchantmentBookSettings.getNewScrambledBook(book);

        if (newBook == null) {
            this.logger.warn("The item in the method moveItems is null for some reason...");

            return;
        }

        newBook = newBook.withType(ColorUtils.getRandomPaneColor().getMaterial());

        inventory.setItem(9, newBook);

        for (int amount = 0; amount < 8; amount++) {
            inventory.setItem(amount + 10, items.get(amount));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onReRoll(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        final Inventory inventory = event.getClickedInventory();

        if (inventory == null) return;

        final ItemStack currentItem = event.getCurrentItem();

        final ItemStack air = ItemType.AIR.createItemStack(1);

        final ItemStack book = currentItem != null ? currentItem : air;

        final ItemStack scrambler = event.getCursor();

        if (book.getType() == Material.AIR || scrambler.getType() == Material.AIR) return;

        if (book.getAmount() != 1 || scrambler.getAmount() != 1) return;

        if (!this.scramblerData.isScrambler(scrambler) || !this.enchantmentBookSettings.isEnchantmentBook(book)) return;

        if (inventory.getType() != InventoryType.PLAYER) {
            player.sendMessage(Messages.NEED_TO_USE_PLAYER_INVENTORY.getMessage());

            return;
        }

        event.setCancelled(true);

        player.setItemOnCursor(air);

        if (this.scramblerData.isAnimationToggle()) {
            event.setCurrentItem(air);

            openScrambler(player, book);
        } else {
            event.setCurrentItem(this.enchantmentBookSettings.getNewScrambledBook(book));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(this.scramblerData.getGuiName())) event.setCancelled(true); //todo() use inventory holders, because this is terrible...
    }

    @EventHandler(ignoreCancelled = true)
    public void onScramblerClick(PlayerInteractEvent event) {
        final ItemStack item = this.methods.getItemInHand(event.getPlayer());

        if (item.isEmpty()) return;

        if (item.getPersistentDataContainer().has(DataKeys.scrambler.getNamespacedKey())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        try {
            this.roll.remove(player).cancel();
        } catch (Exception ignored) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onScrollClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        final PlayerInventory inventory = player.getInventory();

        if (this.scramblerData.isScrambler(inventory.getItemInMainHand()) || this.scramblerData.isScrambler(inventory.getItemInOffHand())) event.setCancelled(true);
    }
}