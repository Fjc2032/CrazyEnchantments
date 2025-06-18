package com.badbones69.crazyenchantments.paper.api.objects;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LostBook {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final ConfigOptions options = this.plugin.getOptions();

    private final int slot;
    private final boolean inGUI;
    private final ItemBuilder displayItem;
    private final int cost;
    private final Currency currency;
    private final boolean useFirework;
    private final List<Color> fireworkColors;
    private final boolean useSound;
    private Sound sound;

    public LostBook(final int slot, final boolean inGUI, final ItemBuilder displayItem, final int cost, final Currency currency, final boolean useFirework,
                    final List<Color> fireworkColors, final boolean useSound, final String sound) {
        if (slot == -1) {
            throw new IllegalArgumentException("Slot in the LostBook config section must be a positive integer i.e. cannot be -1, and must be greater than 0");
        }

        this.slot = slot - 1;
        this.inGUI = inGUI;
        this.displayItem = displayItem;
        this.cost = cost;
        this.currency = currency;
        this.useFirework = !fireworkColors.isEmpty() && useFirework;
        this.fireworkColors = fireworkColors;

        try { // If the sound doesn't exist it will not error.
            this.sound = Sound.valueOf(sound); //todo() mojang mapped
        } catch (Exception e) {
            this.sound = Sound.UI_BUTTON_CLICK;
        }

        this.useSound = sound != null && useSound;
    }
    
    public int getSlot() {
        return this.slot;
    }
    
    public boolean isInGUI() {
        return this.inGUI;
    }
    
    public ItemBuilder getDisplayItem() {
        return this.displayItem;
    }
    
    public int getCost() {
        return this.cost;
    }
    
    public Currency getCurrency() {
        return this.currency;
    }

    public boolean useFirework() {
        return this.useFirework;
    }
    
    public List<Color> getFireworkColors() {
        return this.fireworkColors;
    }
    
    public boolean playSound() {
        return this.useSound;
    }
    
    public Sound getSound() {
        return this.sound;
    }
    
    public ItemBuilder getLostBook(final Category category) {
        return getLostBook(category, 1);
    }

    public ItemBuilder getLostBook(final Category category, final int amount) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%Category%", category.getDisplayItem().getName());

        return new ItemBuilder().setMaterial(this.options.getLostBookItem())
                .setAmount(amount)
                .setName(this.options.getLostBookName())
                .setNamePlaceholders(placeholders)
                .setLore(this.options.getLostBookLore())
                .setLorePlaceholders(placeholders)
                .addKey(DataKeys.lost_book.getNamespacedKey(), category.getName());
    }
}