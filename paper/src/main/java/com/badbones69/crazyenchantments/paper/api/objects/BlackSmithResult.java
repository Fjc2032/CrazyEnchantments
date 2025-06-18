package com.badbones69.crazyenchantments.paper.api.objects;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.builders.types.blacksmith.BlackSmithManager;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map.Entry;
import java.util.Set;

public class BlackSmithResult {

    private int cost = 0;
    private ItemStack resultItem;
    
    public BlackSmithResult(final Player player, final ItemStack mainItem, final ItemStack subItem) {
        this.resultItem = mainItem.clone();

        final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

        final Starter starter = plugin.getStarter();

        final EnchantmentBookSettings enchantmentBookSettings = starter.getEnchantmentBookSettings();

        final CEBook mainBook = enchantmentBookSettings.getCEBook(mainItem);
        final CEBook subBook = enchantmentBookSettings.getCEBook(subItem);

        if (mainBook != null && subBook != null) {
            // Books are the same enchantment.
            if (mainBook.getEnchantment() == subBook.getEnchantment() &&
            // Books have to be the same level.
            mainBook.getLevel() == subBook.getLevel() &&
            // Makes sure level doesn't go past max.
            mainBook.getLevel() + 1 <= mainBook.getEnchantment().getMaxLevel()) {
                this.resultItem = mainBook.setLevel(mainBook.getLevel() + 1).buildBook();
                this.cost += BlackSmithManager.getBookUpgrade();
            }
        } else {
            if (mainItem.getType() == subItem.getType()) {
                final CEItem mainCE = new CEItem(this.resultItem);
                final CEItem subCE = new CEItem(subItem);
                final BlackSmithCompare compare = new BlackSmithCompare(mainCE, subCE);

                // Checking for duplicate enchantments.
                for (Entry<Enchantment, Integer> entry : mainCE.getVanillaEnchantments().entrySet()) {
                    final Enchantment enchantment = entry.getKey();

                    final int level = entry.getValue();
                    final int subLevel = subCE.getVanillaEnchantmentLevel(enchantment);

                    if (enchantment.canEnchantItem(subItem) && subCE.hasVanillaEnchantment(enchantment)) {
                        if (level == subLevel && level < enchantment.getMaxLevel()) {
                            mainCE.addVanillaEnchantment(enchantment, level + 1);

                            this.cost += BlackSmithManager.getLevelUp();
                        } else if (level < subLevel) {
                            mainCE.addVanillaEnchantment(enchantment, subLevel);

                            this.cost += BlackSmithManager.getLevelUp();
                        }
                    }
                }

                for (final Entry<CEnchantment, Integer> entry : mainCE.getCEnchantments().entrySet()) {
                    final CEnchantment enchantment = entry.getKey();

                    final int level = entry.getValue();
                    final int subLevel = subCE.getCEnchantmentLevel(enchantment);

                    if (enchantment.canEnchantItem(subItem) && subCE.hasCEnchantment(enchantment)) {
                        if (level == subLevel && level < enchantment.getMaxLevel()) {
                            mainCE.addCEnchantment(enchantment, level + 1);

                            this.cost += BlackSmithManager.getLevelUp();
                        } else if (level < subLevel) {
                            mainCE.addCEnchantment(enchantment, subLevel);

                            this.cost += BlackSmithManager.getLevelUp();
                        }
                    }
                }

                // Checking for new enchantments.
                for (final Entry<Enchantment, Integer> entry : compare.getNewVanillaEnchantments().entrySet()) {
                    final Enchantment enchantment = entry.getKey();

                    if (enchantment.canEnchantItem(subItem) && mainCE.canAddEnchantment(player) && !hasConflictingEnchant(mainCE.getVanillaEnchantments().keySet(), enchantment)) {
                        mainCE.addVanillaEnchantment(enchantment, entry.getValue());

                        this.cost += BlackSmithManager.getAddEnchantment();
                    }
                }

                for (final Entry<CEnchantment, Integer> entry : compare.getNewCEnchantments().entrySet()) {
                    final CEnchantment enchantment = entry.getKey();

                    if (enchantment.canEnchantItem(mainItem) && mainCE.canAddEnchantment(player) && !hasConflictingCEEnchant(mainCE.getCEnchantments().keySet(), enchantment)) {
                        mainCE.addCEnchantment(enchantment, entry.getValue());

                        this.cost += BlackSmithManager.getAddEnchantment();
                    }
                }

                mainCE.build();
            }
        }
    }

    /**
     * Check if this enchantment conflicts with another enchantment.
     *
     * @param vanillaEnchantments The enchants to check if they are conflicting.
     * @param enchantment the enchant to check the others against.
     * @return True if there is a conflict.
     */
    private boolean hasConflictingEnchant(final Set<Enchantment> vanillaEnchantments, final Enchantment enchantment) {
        for (final Enchantment enchant : vanillaEnchantments) {
            if (enchantment.conflictsWith(enchant)) return true;
        }

        return false;
    }
    /**
     * Check if this enchantment conflicts with another enchantment.
     *
     * @param ceEnchantments The ceEnchants to check if they are conflicting.
     * @param cEnchantment The ceEnchant to check the others against.
     * @return True if there is a conflict.
     */
    private boolean hasConflictingCEEnchant(final Set<CEnchantment> ceEnchantments, final CEnchantment cEnchantment) {
        for (final CEnchantment enchant : ceEnchantments) {
            if (cEnchantment.conflictsWith(enchant)) return true;
        }

        return false;
    }

    public int getCost() {
        return this.cost;
    }
    
    public ItemStack getResultItem() {
        return this.resultItem;
    }
}