package com.badbones69.crazyenchantments.paper.api.utils;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.events.EnchantmentUseEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.Category;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantUtils {

    @NotNull
    private final static CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private static final Server server = plugin.getServer();

    private static final PluginManager pluginManager = server.getPluginManager();

    private static final Starter starter = plugin.getStarter();

    private static final CrazyManager crazyManager = starter.getCrazyManager();

    /**
     * Get the highest category rarity the enchantment is in.
     * @param enchantment The enchantment you are checking.
     * @return The highest category based on the rarities.
     */
    public static Category getHighestEnchantmentCategory(final CEnchantment enchantment) {
        Category topCategory = null;

        int rarity = 0;

        for (final Category category : enchantment.getCategories()) {
            if (category.getRarity() >= rarity) {
                rarity = category.getRarity();
                topCategory = category;
            }
        }

        return topCategory;
    }

    public static boolean isEventActive(final CEnchantments enchant, final Entity damager, final ItemStack item, final Map<CEnchantment, Integer> enchants) {
        return isEventActive(enchant, damager, item, enchants, 1.0);
    }

    public static boolean isEventActive(final CEnchantments enchant, final Entity damager, final ItemStack item, final Map<CEnchantment, Integer> enchants, final double multiplier) {
        return isActive((Player) damager, enchant, enchants, multiplier) && normalEnchantEvent(enchant, damager, item);
    }

    public static boolean isMassBlockBreakActive(final Player player, final CEnchantments enchant, final Map<CEnchantment, Integer> enchants) {
        return isActive(player, enchant, enchants, 1.0);
    }


    private static boolean isActive(final Player player, final CEnchantments enchant, final Map<CEnchantment, Integer> enchants) {
        return isActive(player, enchant, enchants, 1.0);
    }

    /**
     * Main Event used to validate that all enchants can work.
     * Global method that should be used before every enchantment is activated.
     *
     * @param player the player
     * @param enchant the enchant to use
     * @param enchants the map of enchants
     * @param multiplier the multipler of the enchant.
     * @return True if the enchant is active and can be used if the event is passed.
     */
    private static boolean isActive(final Player player, final CEnchantments enchant, final Map<CEnchantment, Integer> enchants, final double multiplier) {
        return enchants.containsKey(enchant.getEnchantment()) && (player.isOp() ||
                ((!enchant.hasChanceSystem() || enchant.chanceSuccessful(enchants.get(enchant.getEnchantment()), multiplier)) &&
                        !(player.hasPermission("crazyenchantments.%s.deny".formatted(enchant.getName())))));
        // TODO Potentially add in entity support.
    }

    public static boolean normalEnchantEvent(final CEnchantments enchant, final Entity damager, final ItemStack item) {
        EnchantmentUseEvent useEvent = new EnchantmentUseEvent((Player) damager, enchant.getEnchantment(), item);

        pluginManager.callEvent(useEvent);

        return !useEvent.isCancelled();
    }

    public static boolean isAuraActive(final Player player, final CEnchantments enchant, final Map<CEnchantment, Integer> enchants) {
        if (crazyManager.getCEPlayer(player.getUniqueId()).onEnchantCooldown(enchant, 20*3)) return false;

        return isActive(player, enchant, enchants);
    }

    public static boolean isArmorEventActive(final Player player, final CEnchantments enchant, final ItemStack item) {
        if (player.isOp()) return true;

        if (player.hasPermission("crazyenchantments.%s.deny".formatted(enchant.getName()))) return false;

        return normalEnchantEvent(enchant, player, item);
    }

    public static boolean isMoveEventActive(final CEnchantments enchant, final Player player, final Map<CEnchantment, Integer> enchants) {
        if (!isActive(player, enchant, enchants)) return false;

        return !crazyManager.getCEPlayer(player.getUniqueId()).onEnchantCooldown(enchant, 20);
    }
}