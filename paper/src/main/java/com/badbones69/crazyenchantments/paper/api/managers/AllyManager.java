package com.badbones69.crazyenchantments.paper.api.managers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.AllyMob;
import com.badbones69.crazyenchantments.paper.api.objects.AllyMob.AllyType;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllyManager {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Map<AllyType, String> allyTypeNameCache = new HashMap<>();
    private final Map<UUID, List<AllyMob>> allyOwners = new HashMap<>();
    private final List<AllyMob> allyMobs = new ArrayList<>();
    
    public void load(final CommentedConfigurationNode config) {
        final CommentedConfigurationNode node = config.node("Settings", "EnchantmentOptions", "Ally-Mobs");

        for (final AllyType type : AllyType.values()) {
            this.allyTypeNameCache.put(type, ColorUtils.color(node.node(type.getConfigName()).getString(type.getDefaultName())));
        }
    }
    
    public List<AllyMob> getAllyMobs() {
        return this.allyMobs;
    }
    
    public void addAllyMob(final AllyMob allyMob) {
        if (allyMob != null) {
            this.allyMobs.add(allyMob);

            final UUID owner = allyMob.getOwner().getUniqueId();

            if (this.allyOwners.containsKey(owner)) {
                this.allyOwners.get(owner).add(allyMob);
            } else {
                final List<AllyMob> allies = new ArrayList<>();

                allies.add(allyMob);

                this.allyOwners.put(owner, allies);
            }
        }
    }
    
    public void removeAllyMob(final AllyMob allyMob) {
        if (allyMob != null) {
            this.allyMobs.remove(allyMob);

            final UUID owner = allyMob.getOwner().getUniqueId();

            if (this.allyOwners.containsKey(owner)) {
                this.allyOwners.get(owner).add(allyMob);

                if (this.allyOwners.get(owner).isEmpty()) this.allyOwners.remove(owner);
            }
        }
    }
    
    public void forceRemoveAllies() {
        if (!this.allyMobs.isEmpty()) {
            for (final AllyMob ally : this.allyMobs) {
                final LivingEntity entity = ally.getAlly();

                new FoliaScheduler(this.plugin, null, entity) {
                    @Override
                    public void run() {
                        entity.remove();;
                    }
                }.runNextTick();
            }

            this.allyMobs.clear();
            this.allyOwners.clear();
        }
    }
    
    public void forceRemoveAllies(final Player owner) {
        for (final AllyMob ally : this.allyOwners.getOrDefault(owner.getUniqueId(), new ArrayList<>())) {
            final LivingEntity entity = ally.getAlly();

            new FoliaScheduler(this.plugin, null, entity) {
                @Override
                public void run() {
                    entity.remove();

                    allyMobs.remove(ally);
                }
            }.runNextTick();
        }

        this.allyOwners.remove(owner.getUniqueId());
    }
    
    public void setEnemy(final Player owner, final Entity enemy) {
        this.allyOwners.getOrDefault(owner.getUniqueId(), new ArrayList<>()).forEach(ally -> {
            new FoliaScheduler(this.plugin, null, ally.getAlly()) {
                @Override
                public void run() {
                    ally.attackEnemy((LivingEntity) enemy);
                }
            }.runNextTick();
        });
    }
    
    public Map<AllyType, String> getAllyTypeNameCache() {
        return this.allyTypeNameCache;
    }
    
    public boolean isAlly(final Player player, final Entity livingEntity) {
        if (isAllyMob(livingEntity)) return isAlly(player, getAllyMob(livingEntity));

        return false;
    }
    
    public boolean isAlly(final Player player, final AllyMob ally) {
        return ally.getOwner().getUniqueId() == player.getUniqueId();
    }
    
    public boolean isAllyMob(final Entity livingEntity) {
        for (final AllyMob ally : this.allyMobs) {
            if (ally.getAlly().getUniqueId() == livingEntity.getUniqueId()) return true;
        }

        return false;
    }
    
    public AllyMob getAllyMob(final Entity livingEntity) {
        for (final AllyMob ally : this.allyMobs) {
            if (ally.getAlly().getUniqueId() == livingEntity.getUniqueId()) return ally;
        }

        return null;
    }
}