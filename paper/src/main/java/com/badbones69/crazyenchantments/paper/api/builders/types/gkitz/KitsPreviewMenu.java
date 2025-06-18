package com.badbones69.crazyenchantments.paper.api.builders.types.gkitz;

import com.badbones69.crazyenchantments.paper.api.builders.InventoryBuilder;
import com.badbones69.crazyenchantments.paper.api.objects.gkitz.GKitz;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class KitsPreviewMenu extends InventoryBuilder {

    public KitsPreviewMenu(final Player player, final int size, final String title, final GKitz kit) {
        super(player, size, title, kit);
    }

    @Override
    public InventoryBuilder build() {
        final Inventory inv = getInventory();

        getKit().getPreviewItems().forEach(inv::addItem);

        inv.setItem(inv.getSize() - 1, KitsManager.getBackRight());

        return this;
    }
}