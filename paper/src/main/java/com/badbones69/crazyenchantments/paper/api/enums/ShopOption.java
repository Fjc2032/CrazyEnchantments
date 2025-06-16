package com.badbones69.crazyenchantments.paper.api.enums;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

public enum ShopOption {
    
    GKITZ("GKitz", "GKitz", "Name", "Lore", false),
    BLACKSMITH("BlackSmith", "BlackSmith", "Name", "Lore", false),
    TINKER("Tinker", "Tinker", "Name", "Lore", false),
    INFO("Info", "Info", "Name", "Lore", false),
    
    PROTECTION_CRYSTAL("ProtectionCrystal", "ProtectionCrystal", "GUIName", "GUILore", true),
    SUCCESS_DUST("SuccessDust", "Dust.SuccessDust", "GUIName", "GUILore", true),
    DESTROY_DUST("DestroyDust", "Dust.DestroyDust", "GUIName", "GUILore", true),
    SCRAMBLER("Scrambler", "Scrambler", "GUIName", "GUILore", true),
    
    BLACK_SCROLL("BlackScroll", "BlackScroll", "GUIName", "Lore", true),
    WHITE_SCROLL("WhiteScroll", "WhiteScroll", "GUIName", "Lore", true),
    TRANSMOG_SCROLL("TransmogScroll", "TransmogScroll", "GUIName", "Lore", true),
    SLOT_CRYSTAL("Slot_Crystal", "Slot_Crystal", "GUIName", "GUILore", true);
    
    private static final Map<ShopOption, Option> shopOptions = new HashMap<>();
    private final String optionPath;
    private final String path;
    private final String namePath;
    private final String lorePath;
    private final boolean buyable;
    
    ShopOption(final String optionPath, final String path, final String namePath, final String lorePath, final boolean buyable) {
        this.optionPath = optionPath;
        this.path = path;
        this.namePath = namePath;
        this.lorePath = lorePath;
        this.buyable = buyable;
    }

    private final static CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final static ComponentLogger logger = plugin.getComponentLogger();
    
    public static void loadShopOptions() {
        final FileConfiguration config = Files.CONFIG.getFile();

        shopOptions.clear();

        for (final ShopOption shopOption : values()) {
            final String itemPath = "Settings." + shopOption.getPath() + ".";
            final String costPath = "Settings.Costs." + shopOption.getOptionPath() + ".";

            try {
                shopOptions.put(shopOption, new Option(new ItemBuilder().setName(config.getString(itemPath + shopOption.getNamePath(), "Error getting name."))
                        .setLore(config.getStringList(itemPath + shopOption.getLorePath()))
                        .setMaterial(config.getString(itemPath + "Item", "CHEST"))
                        .setPlayerName(config.getString(itemPath + "Player", ""))
                        .setGlow(config.getBoolean(itemPath + "Glowing", false)),
                        config.getInt(itemPath + "Slot", 1) - 1,
                        config.getBoolean(itemPath + "InGUI", true),
                        config.getInt(costPath + "Cost", 100),
                        Currency.getCurrency(config.getString(costPath + "Currency", "Vault"))));
            } catch (final Exception exception) {
                logger.error("The option {} has failed to load.", shopOption.getOptionPath(), exception);
            }
        }
    }
    
    public ItemStack getItem() {
        return getItemBuilder().build();
    }
    
    public ItemBuilder getItemBuilder() {
        return shopOptions.get(this).itemBuilder();
    }
    
    public int getSlot() {
        return shopOptions.get(this).slot();
    }
    
    public boolean isInGUI() {
        return shopOptions.get(this).inGUI();
    }
    
    public int getCost() {
        return shopOptions.get(this).cost();
    }
    
    public Currency getCurrency() {
        return shopOptions.get(this).currency();
    }
    
    private String getOptionPath() {
        return this.optionPath;
    }
    
    private String getPath() {
        return this.path;
    }
    
    private String getNamePath() {
        return this.namePath;
    }
    
    private String getLorePath() {
        return this.lorePath;
    }
    
    public boolean isBuyable() {
        return this.buyable;
    }

    private record Option(ItemBuilder itemBuilder, int slot, boolean inGUI, int cost, Currency currency) {}
}