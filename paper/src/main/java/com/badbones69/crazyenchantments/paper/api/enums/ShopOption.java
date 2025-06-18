package com.badbones69.crazyenchantments.paper.api.enums;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.objects.CEOption;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;
import java.util.Map;

public enum ShopOption {
    
    GKITZ("Name", "Lore", false, "GKitz"),
    BLACKSMITH("Name", "Lore", false, "BlackSmith"),
    TINKER("Name", "Lore", false, "Tinker"),
    INFO("Name", "Lore", false, "Info"),
    
    PROTECTION_CRYSTAL("GUIName", "GUILore", true, "ProtectionCrystal"),
    SUCCESS_DUST("GUIName", "GUILore", true, "SuccessDust"),
    DESTROY_DUST("GUIName", "GUILore", true, "DestroyDust"),
    SCRAMBLER("GUIName", "GUILore", true, "Scrambler"),
    
    BLACK_SCROLL("GUIName", "Lore", true, "BlackScroll"),
    WHITE_SCROLL("GUIName", "Lore", true, "WhiteScroll"),
    TRANSMOG_SCROLL("GUIName", "Lore", true, "TransmogScroll"),
    SLOT_CRYSTAL("GUIName", "GUILore", true, "Slot_Crystal");

    private final boolean buyable;
    private final String namePath;
    private final String lorePath;
    private final String path;
    
    ShopOption(final String namePath, final String lorePath, final boolean buyable, final String path) {
        this.namePath = namePath;
        this.lorePath = lorePath;
        this.buyable = buyable;
        this.path = path;
    }

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final Map<ShopOption, CEOption> shopOptions = this.crazyManager.getShopOptions();
    
    public ItemStack getItem() {
        return getItemBuilder().build();
    }
    
    public ItemBuilder getItemBuilder() {
        return this.shopOptions.get(this).itemBuilder();
    }
    
    public int getSlot() {
        return this.shopOptions.get(this).slot();
    }
    
    public boolean isInGUI() {
        return this.shopOptions.get(this).inGUI();
    }
    
    public int getCost() {
        return this.shopOptions.get(this).cost();
    }
    
    public Currency getCurrency() {
        return this.shopOptions.get(this).currency();
    }
    
    public String getPath() {
        return this.path;
    }
    
    public String getNamePath() {
        return this.namePath;
    }
    
    public String getLorePath() {
        return this.lorePath;
    }
    
    public boolean isBuyable() {
        return this.buyable;
    }
}