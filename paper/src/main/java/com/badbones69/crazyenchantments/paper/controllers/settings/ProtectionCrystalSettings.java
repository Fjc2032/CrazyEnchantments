package com.badbones69.crazyenchantments.paper.controllers.settings;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import com.ryderbelserion.fusion.core.files.types.YamlCustomFile;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProtectionCrystalSettings {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final ConfigOptions options = this.plugin.getOptions();

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    private final Map<UUID, List<ItemStack>> crystalItems = new HashMap<>();

    private ItemBuilder crystal;

    public void loadProtectionCrystal(@NotNull final YamlCustomFile config) {
        this.crystal = new ItemBuilder()
                .setMaterial(config.getStringValueWithDefault( "EMERALD", "Settings", "ProtectionCrystal", "Item"))
                .setName(config.getStringValueWithDefault("&5&lProtection &b&lCrystal", "Settings", "ProtectionCrystal", "Name"))
                .setLore(config.getStringList(List.of(
                        "&7A rare crystal that is said to",
                        "&7protect items from getting lost",
                        "&7while the owners away in the after life.",
                        "",
                        "&7&l(&6&l!&7&l) &7Drag and drop on an item."
                ), "Settings", "ProtectionCrystal", "Lore"))
                .setGlow(config.getBooleanValueWithDefault(false, "Settings", "ProtectionCrystal", "Glowing"));
    }

    public final ItemStack getCrystal(final int amount) {
        final ItemStack item = this.crystal.setAmount(amount).build();

        item.editPersistentDataContainer(container -> {
            container.set(DataKeys.protection_crystal.getNamespacedKey(), PersistentDataType.BOOLEAN, true);
        });

        return item;
    }

    public final ItemStack getCrystal() {
        return getCrystal(1);
    }

    /**
     * Add a player to the map to protect items.
     * @param player - The player object.
     * @param items - The items in the player's inventory.
     */
    public void addPlayer(final Player player, final List<ItemStack> items) {
        this.crystalItems.put(player.getUniqueId(), items);
    }

    /**
     * Remove the player from the map.
     * @param player - The player object.
     */
    public void removePlayer(final Player player) {
        this.crystalItems.remove(player.getUniqueId());
    }

    /**
     * Check if the map contains the player.
     * @param player - The player object.
     */
    public boolean containsPlayer(final Player player) {
        return this.crystalItems.containsKey(player.getUniqueId());
    }

    /**
     * Get the player from the map.
     * @param player - The player object.
     * @return Get the player's items stored.
     */
    public List<ItemStack> getPlayer(final Player player) {
        return this.crystalItems.get(player.getUniqueId());
    }

    /**
     * @return The hash map.
     */
    public Map<UUID, List<ItemStack>> getCrystalItems() {
        return this.crystalItems;
    }

    /**
     * Check if the player has permissions & if the option is enabled.
     * @param player - The player to check.
     */
    public final boolean isProtectionSuccessful(final Player player) {
        if (player.hasPermission("crazyenchantments.bypass.protectioncrystal")) return true;

        if (this.options.isProtectionCrystalChanceToggle()) return this.methods.randomPicker(this.options.getProtectionCrystalSuccessChance(), 100);

        return true;
    }

    /**
     * Check if the item is protected or not.
     * @param data - The data to check.
     * @return True if yes otherwise false.
     */
    public static boolean isProtected(final PersistentDataContainerView data) {
        return data != null && data.has(DataKeys.protected_item.getNamespacedKey());
    }

    /**
     * Check if the item is a protection crystal.
     * @param item - The item to check.
     * @return True if the item is a protection crystal.
     */
    public final boolean isProtectionCrystal(final ItemStack item) {
        return item.getPersistentDataContainer().has(DataKeys.protection_crystal.getNamespacedKey());
    }

    /**
     * Remove protection from the item.
     * @param item - The item to remove protection from.
     * @return The new item.
     */
    public final ItemStack removeProtection(final ItemStack item) {
        final PersistentDataContainerView view = item.getPersistentDataContainer();

        if (view.has(DataKeys.protected_item.getNamespacedKey())) {
            item.editPersistentDataContainer(container -> {
                container.remove(DataKeys.protected_item.getNamespacedKey());
            });
        }

        final List<Component> lore = item.lore();

        if (lore != null) {
            lore.removeIf(loreComponent -> ColorUtils.toPlainText(loreComponent).contains(ColorUtils.stripStringColour(this.options.getProtectionCrystalProtected())));

            item.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(lore).build());
        }

        return item;
    }

    /**
     * Add protection to an item.
     * @param item - The item to add protection to.
     * @return The new item.
     */
    public final ItemStack addProtection(final ItemStack item) {
        final List<Component> itemLore = item.lore();

        List<Component> lore = itemLore != null ? itemLore : new ArrayList<>();

        item.editPersistentDataContainer(container -> {
            container.set(DataKeys.protected_item.getNamespacedKey(), PersistentDataType.BOOLEAN, true);
        });

        lore.add(ColorUtils.legacyTranslateColourCodes(this.options.getProtectionCrystalProtected()));

        item.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(lore).build());

        return item;
    }
}