package com.badbones69.crazyenchantments.paper.listeners;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.builders.types.MenuManager;
import com.badbones69.crazyenchantments.paper.api.enums.Messages;
import com.badbones69.crazyenchantments.paper.api.enums.Scrolls;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.Enchant;
import com.badbones69.crazyenchantments.paper.api.objects.CEBook;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.enchants.EnchantmentType;
import com.badbones69.crazyenchantments.paper.api.objects.items.ScrollData;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.badbones69.crazyenchantments.paper.api.utils.NumberUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.controllers.settings.ProtectionCrystalSettings;
import com.google.gson.Gson;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ScrollListener implements Listener {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final ScrollData scrollData = this.crazyManager.getScrollData();

    private final Starter starter = this.plugin.getStarter();

    private final Methods methods = this.starter.getMethods();

    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @EventHandler(ignoreCancelled = true)
    public void onScrollUse(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        final ItemStack item = event.getCurrentItem();
        final ItemStack scroll = event.getCursor();

        if (item == null || item.getType().isAir() || scroll.getType().isAir()) return;

        final InventoryType.SlotType slotType = event.getSlotType();

        if (slotType != InventoryType.SlotType.ARMOR && slotType != InventoryType.SlotType.CONTAINER && slotType != InventoryType.SlotType.QUICKBAR) return;

        final Scrolls type = Scrolls.getFromPDC(scroll);

        if (type == null) return;

        if (scroll.getAmount() > 1) {
            player.sendMessage(Messages.NEED_TO_UNSTACK_ITEM.getMessage());

            return;
        }

        switch (type.getConfigName()) {
            case "BlackScroll" -> {
                if (this.methods.isInventoryFull(player)) return;

                final List<CEnchantment> enchantments = this.enchantmentBookSettings.getEnchantmentsOnItem(item);

                if (!enchantments.isEmpty()) { // Item has enchantments
                    event.setCancelled(true);

                    player.setItemOnCursor(this.methods.removeItem(scroll));

                    if (this.scrollData.isBlackScrollChanceToggle() && !this.methods.randomPicker(this.scrollData.getBlackScrollChance(), 100)) {
                        player.sendMessage(Messages.BLACK_SCROLL_UNSUCCESSFUL.getMessage());

                        return;
                    }

                    final CEnchantment enchantment = enchantments.get(ThreadLocalRandom.current().nextInt(enchantments.size()));

                    player.getInventory().addItem(new CEBook(enchantment, this.enchantmentBookSettings.getLevel(item, enchantment), 1).buildBook());

                    event.setCurrentItem(this.enchantmentBookSettings.removeEnchantment(item, enchantment));
                }
            }

            case "WhiteScroll" -> {
                if (Scrolls.hasWhiteScrollProtection(item)) return;

                for (final EnchantmentType enchantmentType : MenuManager.getEnchantmentTypes()) {
                    if (enchantmentType.getEnchantableMaterials().contains(item.getType())) {
                        event.setCancelled(true);

                        event.setCurrentItem(Scrolls.addWhiteScrollProtection(item));

                        player.setItemOnCursor(this.methods.removeItem(scroll));

                        return;
                    }
                }
            }

            case "TransmogScroll" -> {
                if (this.enchantmentBookSettings.getEnchantments(item).isEmpty()) return;

                if (item.lore() == null) return;

                ItemStack orderedItem = newOrderNewEnchantments(item.clone()); //todo() wut

                if (item.isSimilar(orderedItem)) return; //todo() why?

                event.setCancelled(true);

                event.setCurrentItem(orderedItem);

                player.setItemOnCursor(this.methods.removeItem(scroll));
            }
        }
    }

    @EventHandler()
    public void onScrollClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();

        if (checkScroll(inventory.getItemInMainHand(), player, event)) return;

        checkScroll(inventory.getItemInOffHand(), player, event);

    }

    private boolean checkScroll(final ItemStack scroll, final Player player, final PlayerInteractEvent event) {
        if (scroll.isEmpty()) return false;

        final PersistentDataContainerView container = scroll.getPersistentDataContainer();

        if (!container.has(DataKeys.scroll.getNamespacedKey())) return false;

        final String data = container.get(DataKeys.scroll.getNamespacedKey(), PersistentDataType.STRING);

        if (data == null) return false;

        if (data.equalsIgnoreCase(Scrolls.BLACK_SCROLL.getConfigName())) {
            event.setCancelled(true);

            player.sendMessage(Messages.RIGHT_CLICK_BLACK_SCROLL.getMessage());

            return true;
        } else if (data.equalsIgnoreCase(Scrolls.WHITE_SCROLL.getConfigName()) || data.equalsIgnoreCase(Scrolls.TRANSMOG_SCROLL.getConfigName())) {
            event.setCancelled(true);

            return true;
        }

        return false;
    }

    private ItemStack newOrderNewEnchantments(final ItemStack item) {
        Gson gson = new Gson();

        final List<Component> lore = item.lore();

        final PersistentDataContainerView container = item.getPersistentDataContainer();

        final Enchant data = gson.fromJson(container.get(DataKeys.enchantments.getNamespacedKey(), PersistentDataType.STRING), Enchant.class);

        final FileConfiguration configuration = Files.CONFIG.getFile();

        final boolean addSpaces = configuration.getBoolean("Settings.TransmogScroll.Add-Blank-Lines", true);

        final List<CEnchantment> newEnchantmentOrder = new ArrayList<>();

        final Map<CEnchantment, Integer> enchantments = new HashMap<>();

        List<String> order = configuration.getStringList("Settings.TransmogScroll.Lore-Order");

        if (order.isEmpty()) order = Arrays.asList("CE_Enchantments", "Protection", "Normal_Lore");

        if (data == null) return item; // Only order if it has CE_Enchants

        for (final CEnchantment enchantment : this.enchantmentBookSettings.getRegisteredEnchantments()) {
            if (!data.hasEnchantment(enchantment.getName())) continue;

            enchantments.put(enchantment,ColorUtils.stripStringColour((enchantment.getCustomName() + " " + NumberUtils.toRoman(data.getLevel(enchantment.getName())))).length());

            newEnchantmentOrder.add(enchantment);
        }

        orderInts(newEnchantmentOrder, enchantments); // Order Enchantments by length.

        List<Component> enchantLore = newEnchantmentOrder.stream().map(i ->
                ColorUtils.legacyTranslateColourCodes("%s %s".formatted(i.getCustomName(), NumberUtils.toRoman(data.getLevel(i.getName()))))).collect(Collectors.toList());

        List<Component> normalLore = stripNonNormalLore(lore == null ? new ArrayList<>() : lore, newEnchantmentOrder);

        List<Component> protectionLore = getAllProtectionLore(container);

        List<Component> newLore = new ArrayList<>();

        boolean wasEmpty = true;

        for (String selection : order) {
            switch (selection) {
                case "CE_Enchantments" -> {
                    if (addSpaces && !wasEmpty && !enchantLore.isEmpty()) newLore.add(Component.text(""));
                    newLore.addAll(enchantLore);

                    wasEmpty = enchantLore.isEmpty();
                }

                case "Protection" -> {
                    if (addSpaces && !wasEmpty && !protectionLore.isEmpty()) newLore.add(Component.text(""));
                    newLore.addAll(protectionLore);

                    wasEmpty = protectionLore.isEmpty();
                }

                case "Normal_Lore" -> {
                    if (addSpaces && !wasEmpty && !normalLore.isEmpty()) newLore.add(Component.text(""));
                    newLore.addAll(normalLore);

                    wasEmpty = normalLore.isEmpty();
                }
            }
        }

        useSuffix(item, newEnchantmentOrder);

        item.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(newLore).build());

        return item;
    }

    private List<Component> getAllProtectionLore(final PersistentDataContainerView container) {
        List<Component> lore = new ArrayList<>();

        final FileConfiguration configuration = Files.CONFIG.getFile();

        if (Scrolls.hasWhiteScrollProtection(container)) lore.add(ColorUtils.legacyTranslateColourCodes(configuration.getString("Settings.WhiteScroll.ProtectedName", "&b&lPROTECTED")));
        if (ProtectionCrystalSettings.isProtected(container)) lore.add(ColorUtils.legacyTranslateColourCodes(configuration.getString("Settings.ProtectionCrystal.Protected", "&6Ancient Protection")));

        return lore;
    }

    private List<Component> stripNonNormalLore(final List<Component> lore, final List<CEnchantment> enchantments) {
        // Remove blank lines
        lore.removeIf(loreComponent -> ColorUtils.toPlainText(loreComponent).replaceAll(" ", "").isEmpty());

        // Remove CE enchantment lore
        enchantments.forEach(enchant -> lore.removeIf(loreComponent ->
                ColorUtils.toPlainText(loreComponent).contains(ColorUtils.stripStringColour(enchant.getCustomName()))
        ));

        // Remove white-scroll protection lore
        lore.removeIf(loreComponent -> ColorUtils.toPlainText(loreComponent).contains(ColorUtils.stripStringColour(Scrolls.getWhiteScrollProtectionName())));

        // Remove Protection-crystal protection lore
        lore.removeIf(loreComponent -> ColorUtils.toPlainText(loreComponent).contains(
                ColorUtils.stripStringColour(Files.CONFIG.getFile().getString("Settings.ProtectionCrystal.Protected", "&6Ancient Protection"))
        ));

        return lore;
    }

    private void useSuffix(final ItemStack item, final List<CEnchantment> newEnchantmentOrder) {
        if (this.scrollData.isUseSuffix()) {
            final boolean hasName = item.hasData(DataComponentTypes.ITEM_NAME);

            String newName = hasName ? ColorUtils.toLegacy(item.getData(DataComponentTypes.ITEM_NAME)) :
                    "&b" + WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));

            if (hasName) {
                for (int i = 0; i <= 100; i++) {
                    String suffixWithAmount = this.scrollData.getSuffix().replace("%Amount%", String.valueOf(i)).replace("%amount%", String.valueOf(i));

                    if (!newName.endsWith(suffixWithAmount)) continue;

                    newName = newName.substring(0, newName.length() - suffixWithAmount.length());

                    break;
                }
            }

            String amount = String.valueOf(this.scrollData.isCountVanillaEnchantments() ? newEnchantmentOrder.size() + item.getEnchantments().size() : newEnchantmentOrder.size());

            item.setData(DataComponentTypes.ITEM_NAME, ColorUtils.legacyTranslateColourCodes(newName + this.scrollData.getSuffix().replace("%Amount%", amount).replace("%amount%", amount)));
        }
    }

    private void orderInts(final List<CEnchantment> list, final Map<CEnchantment, Integer> map) {
        list.sort((a1, a2) -> {
            Integer string1 = map.get(a1);
            Integer string2 = map.get(a2);
            return string2.compareTo(string1);
        });
    }
}