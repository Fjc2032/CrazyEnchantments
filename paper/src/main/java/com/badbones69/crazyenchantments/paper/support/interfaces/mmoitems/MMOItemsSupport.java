package com.badbones69.crazyenchantments.paper.support.interfaces.mmoitems;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.enchants.EnchantmentType;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingdoms.utils.Validate;

public class MMOItemsSupport implements EnchantPlugin<Enchantment> {


    @Override
    public boolean isCustomEnchant(Enchantment enchantment) {
        return enchantment != null;
    }

    @Override
    public void handleEnchant(ItemStackBuilder itemStackBuilder, Enchantment enchantment, int level) {
        Validate.isTrue(level > 0, "Level cannot be below 0.");
    }

    @Override
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(JavaPlugin.getPlugin(CrazyEnchantments.class), key);
    }
}
