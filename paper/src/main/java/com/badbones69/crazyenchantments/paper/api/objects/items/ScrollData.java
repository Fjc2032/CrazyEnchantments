package com.badbones69.crazyenchantments.paper.api.objects.items;

import org.spongepowered.configurate.CommentedConfigurationNode;

public class ScrollData {

    private String suffix;
    private boolean countVanillaEnchantments;
    private boolean useSuffix;
    private boolean blackScrollChanceToggle;
    private int blackScrollChance;

    public void loadScrollControl(final CommentedConfigurationNode config) {
        this.suffix = config.node("Settings", "TransmogScroll", "Amount-of-Enchantments").getString(" &7[&6&n%amount%&7]");
        this.countVanillaEnchantments = config.node("Settings", "TransmogScroll", "Count-Vanilla-Enchantments").getBoolean(true);
        this.useSuffix = config.node("Settings", "TransmogScroll", "Amount-Toggle").getBoolean(true);
        this.blackScrollChance = config.node("Settings", "BlackScroll", "Chance").getInt(75);
        this.blackScrollChanceToggle = config.node("Settings", "BlackScroll", "Chance-Toggle").getBoolean(false);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public boolean isCountVanillaEnchantments() {
        return this.countVanillaEnchantments;
    }

    public boolean isUseSuffix() {
        return this.useSuffix;
    }

    public boolean isBlackScrollChanceToggle() {
        return this.blackScrollChanceToggle;
    }

    public int getBlackScrollChance() {
        return this.blackScrollChance;
    }
}