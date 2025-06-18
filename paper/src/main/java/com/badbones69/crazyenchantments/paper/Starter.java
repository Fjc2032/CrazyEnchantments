package com.badbones69.crazyenchantments.paper;

import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.FileManager;
import com.badbones69.crazyenchantments.paper.api.builders.types.MenuManager;
import com.badbones69.crazyenchantments.paper.api.builders.types.blacksmith.BlackSmithManager;
import com.badbones69.crazyenchantments.paper.api.builders.types.gkitz.KitsManager;
import com.badbones69.crazyenchantments.paper.api.economy.CurrencyAPI;
import com.badbones69.crazyenchantments.paper.api.economy.vault.VaultSupport;
import com.badbones69.crazyenchantments.paper.api.managers.AllyManager;
import com.badbones69.crazyenchantments.paper.api.managers.ArmorEnchantmentManager;
import com.badbones69.crazyenchantments.paper.api.managers.BowEnchantmentManager;
import com.badbones69.crazyenchantments.paper.api.managers.ShopManager;
import com.badbones69.crazyenchantments.paper.api.managers.WingsManager;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.BowUtils;
import com.badbones69.crazyenchantments.paper.controllers.EnchantmentControl;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.controllers.settings.ProtectionCrystalSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import com.badbones69.crazyenchantments.paper.support.PluginSupport.SupportedPlugins;
import com.badbones69.crazyenchantments.paper.support.claims.SuperiorSkyBlockSupport;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class Starter {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private FileManager fileManager;
    private CrazyManager crazyManager;
    private Methods methods;

    private Logger logger = Bukkit.getLogger();

    // Settings.
    private ProtectionCrystalSettings protectionCrystalSettings;
    private EnchantmentBookSettings enchantmentBookSettings;

    // Plugin Utils.
    private BowUtils bowUtils;

    // Plugin Support.
    private SuperiorSkyBlockSupport superiorSkyBlockSupport;
    private PluginSupport pluginSupport;
    private VaultSupport vaultSupport;

    private EnchantPluginBuilder<CEnchantment> enchantPluginBuilder;
    private EnchantPlugin enchantPlugin;
    private CrazyEnchantStats crazyEnchantStats;
    private MMOItemsSupport mmoItemsSupport;

    // Plugin Managers.
    private ArmorEnchantmentManager armorEnchantmentManager;
    private BowEnchantmentManager bowEnchantmentManager;
    private WingsManager wingsManager;
    private AllyManager allyManager;
    private ShopManager shopManager;

    // Economy Management.
    private CurrencyAPI currencyAPI;

    public void run() {
        this.fileManager = new FileManager();
        this.fileManager.setup();

        // Plugin Support.
        this.pluginSupport = new PluginSupport();
        this.pluginSupport.initializeWorldGuard();

        if (SupportedPlugins.SUPERIORSKYBLOCK.isPluginLoaded()) this.superiorSkyBlockSupport = new SuperiorSkyBlockSupport();

        // Methods
        this.methods = new Methods();

        // Settings.
        this.protectionCrystalSettings = new ProtectionCrystalSettings();
        this.enchantmentBookSettings = new EnchantmentBookSettings();

        BlackSmithManager.load(FileKeys.config.getConfig());
        KitsManager.load();

        MenuManager.load();

        // Economy Management.
        this.currencyAPI = new CurrencyAPI();

        this.shopManager = new ShopManager();

        // Plugin Managers.
        this.armorEnchantmentManager = new ArmorEnchantmentManager();
        this.bowEnchantmentManager = new BowEnchantmentManager();
        this.wingsManager = new WingsManager();
        this.allyManager = new AllyManager();

        this.crazyManager = new CrazyManager();

        // Plugin Utils.
        this.bowUtils = new BowUtils();

        this.plugin.pluginManager.registerEvents(new EnchantmentControl(), this.plugin);
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public Methods getMethods() {
        return this.methods;
    }

    public CrazyManager getCrazyManager() {
        return this.crazyManager;
    }

    // Settings.
    public ProtectionCrystalSettings getProtectionCrystalSettings() {
        return this.protectionCrystalSettings;
    }

    public EnchantmentBookSettings getEnchantmentBookSettings() {
        return this.enchantmentBookSettings;
    }

    // Plugin Support.
    public PluginSupport getPluginSupport() {
        return this.pluginSupport;
    }

    public VaultSupport getVaultSupport() {
        return this.vaultSupport;
    }

    public void setVaultSupport(VaultSupport vaultSupport) {
        this.vaultSupport = vaultSupport;

        vaultSupport.loadVault();
    }

    public SuperiorSkyBlockSupport getSuperiorSkyBlockSupport() {
        if (this.superiorSkyBlockSupport == null) this.superiorSkyBlockSupport = new SuperiorSkyBlockSupport();

        return this.superiorSkyBlockSupport;
    }
    // Economy Management.
    public CurrencyAPI getCurrencyAPI() {
        return this.currencyAPI;
    }

    // Plugin Managers.
    public ArmorEnchantmentManager getArmorEnchantmentManager() {
        return this.armorEnchantmentManager;
    }

    public BowEnchantmentManager getBowEnchantmentManager() {
        return this.bowEnchantmentManager;
    }

    public WingsManager getWingsManager() {
        return this.wingsManager;
    }

    public AllyManager getAllyManager() {
        return this.allyManager;
    }

    public ShopManager getShopManager() {
        return this.shopManager;
    }

    // Plugin Utils.
    public BowUtils getBowUtils() {
        return this.bowUtils;
    }




    //
    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}