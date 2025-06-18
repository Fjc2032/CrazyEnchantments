package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

public class AxeEnchantments implements Listener {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final ConfigOptions options = this.plugin.getOptions();

    private final Starter starter = this.plugin.getStarter();

    private final Methods methods = this.starter.getMethods();

    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTreeFeller(BlockBreakEvent event) {
        if (!event.isDropItems() || EventUtils.isIgnoredEvent(event)) return;

        final Player player = event.getPlayer();
        final ItemStack currentItem = this.methods.getItemInHand(player);

        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(currentItem);

        if (!EnchantUtils.isMassBlockBreakActive(player, CEnchantments.TREEFELLER, enchantments)) return;

        final Set<Block> blockList = getTree(event.getBlock(), 5 * enchantments.get(CEnchantments.TREEFELLER.getEnchantment()));
        final boolean damage = this.options.isTreefellerFullDurability();

        if (!new MassBlockBreakEvent(player, blockList).callEvent()) return;

        for (final Block block : blockList) {
            if (block == event.getBlock()) continue;
            if (this.methods.playerBreakBlock(player, block, currentItem, true)) continue;
            if (damage) this.methods.removeDurability(currentItem, player);
        }

        if (!damage) this.methods.removeDurability(currentItem, player);

    }

    private Set<Block> getTree(final Block startBlock, final int maxBlocks) {
        final Set<Block> checkedBlocks = new HashSet<>(), tree = new HashSet<>();
        final Queue<Block> queue = new LinkedList<>();

        queue.add(startBlock);
        checkedBlocks.add(startBlock);

        final int startX = startBlock.getX(), startZ = startBlock.getZ();

        while (!queue.isEmpty()) {
            Block currentBlock = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        if (tree.size() > maxBlocks) break;
                        if (x == 0 && y == 0 && z == 0) continue; // Skip initial block.

                        final Block neighbor = currentBlock.getRelative(x, y, z);
                        if (neighbor.isEmpty() || checkedBlocks.contains(neighbor)) continue;
                        if (notInRange(startX, neighbor.getX()) || notInRange(startZ, neighbor.getZ())) continue;

                        final String neighborType = neighbor.getType().toString();

                        if ((neighborType.endsWith("LOG") || neighborType.endsWith("LEAVES"))) {
                            if (neighborType.endsWith("LOG")) tree.add(neighbor);

                            checkedBlocks.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return tree;
    }

    private boolean notInRange(final int startPos, final int pos2) {
        int range = 5;
        return pos2 > (startPos + range) || pos2 < (startPos - range);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event)) return;
        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(event.getDamager() instanceof Player damager)) return;

        final ItemStack item = this.methods.getItemInHand(damager);

        if (entity.isDead()) return;

        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.BERSERK, damager, item, enchantments)) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, (enchantments.get(CEnchantments.BERSERK.getEnchantment()) + 5) * 20, 1));
                damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (enchantments.get(CEnchantments.BERSERK.getEnchantment()) + 5) * 20, 0));
        }

        if (EnchantUtils.isEventActive(CEnchantments.BLESSED, damager, item, enchantments)) removeBadPotions(damager);

        if (EnchantUtils.isEventActive(CEnchantments.FEEDME, damager, item, enchantments)&& damager.getFoodLevel() < 20) {
            final int food = 2 * enchantments.get(CEnchantments.FEEDME.getEnchantment());

            if (damager.getFoodLevel() + food < 20) damager.setFoodLevel((int) (damager.getSaturation() + food));

            if (damager.getFoodLevel() + food > 20) damager.setFoodLevel(20);
        }

        if (EnchantUtils.isEventActive(CEnchantments.REKT, damager, item, enchantments)) event.setDamage(event.getDamage() * 2);

        if (EnchantUtils.isEventActive(CEnchantments.CURSED, damager, item, enchantments))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, (enchantments.get(CEnchantments.CURSED.getEnchantment()) + 9) * 20, 1));

        if (EnchantUtils.isEventActive(CEnchantments.DIZZY, damager, item, enchantments))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, (enchantments.get(CEnchantments.DIZZY.getEnchantment()) + 9) * 20, 0));

        if (EnchantUtils.isEventActive(CEnchantments.BATTLECRY, damager, item, enchantments)) {
            for (final Entity nearbyEntity : damager.getNearbyEntities(3, 3, 3)) {
                new FoliaScheduler(this.plugin, null, entity) {
                    @Override
                    public void run() {
                        if (!pluginSupport.isFriendly(damager, nearbyEntity)) {
                            Vector vector = damager.getLocation().toVector().normalize().setY(.5);
                            Vector vector1 = nearbyEntity.getLocation().toVector().subtract(vector);

                            nearbyEntity.setVelocity(vector1);
                        }
                    }
                }.runNextTick();
            }
        }

        if (EnchantUtils.isEventActive(CEnchantments.DEMONFORGED, damager, item, enchantments) && entity instanceof Player player) {
            final EntityEquipment equipment = player.getEquipment();

            final ItemStack armorItem = switch (this.methods.percentPick(4, 0)) {
                case 1 -> equipment.getHelmet();
                case 2 -> equipment.getChestplate();
                case 3 -> equipment.getLeggings();
                default -> equipment.getBoots();
            };

            this.methods.removeDurability(armorItem, player);
        }
        //Imperium
        if (EnchantUtils.isEventActive(CEnchantments.REAPER, damager, item, enchantments)) {
            Collection<PotionEffect> effects = new ArrayList<>();
            effects.add(new PotionEffect(PotionEffectType.WITHER, CEnchantments.REAPER.getChance() / 5, 1));
            effects.add(new PotionEffect(PotionEffectType.BLINDNESS, CEnchantments.REAPER.getChance() / 5, 1));
            entity.addPotionEffects(effects);

            int damageAmount = damager.getExpToLevel();
            event.setDamage(event.getDamage() * (1 + (double) damageAmount / 1000));
        }
        if (EnchantUtils.isEventActive(CEnchantments.PUMMEL, damager, item, enchantments)) {
            if (!(event.getDamager() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6, 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.CLEAVE, damager, item, enchantments)) {
            World world = event.getDamager().getWorld();
            if (!(event.getEntity() instanceof LivingEntity victim)) return;
            BoundingBox region = new BoundingBox(damager.getX(), damager.getY(), damager.getZ(), victim.getX() + 3, victim.getY(), victim.getZ() + 3);
            Collection<Entity> targets = world.getNearbyEntities(region);

            for (Entity target : targets) {
                if (!(target instanceof LivingEntity)) return;
                ((LivingEntity) target).damage(event.getDamage() * ((double) CEnchantments.CLEAVE.getChance() / 20));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.CORRUPT, damager, item, enchantments)) {
            damager.sendMessage("Corrupt activated");
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            target.damage(event.getDamage());
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 40L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 60L);
            Bukkit.getScheduler().runTaskLater(plugin, () -> target.damage(event.getDamage()), 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.INSANITY, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player target)) return;
            ItemStack axe = target.getActiveItem();

            Collection<ItemStack> axes = new ArrayList<>();
            axes.add(ItemStack.of(Material.WOODEN_AXE));
            axes.add(ItemStack.of(Material.STONE_AXE));
            axes.add(ItemStack.of(Material.GOLDEN_AXE));
            axes.add(ItemStack.of(Material.IRON_AXE));
            axes.add(ItemStack.of(Material.DIAMOND_AXE));
            axes.add(ItemStack.of(Material.NETHERITE_AXE));

            for (ItemStack selectedItem : axes) {
                if (selectedItem.equals(axe)) {
                    event.setDamage(event.getDamage() + (1 + (double) CEnchantments.INSANITY.getChance() / 100));
                }
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BARBARIAN, damager, item, enchantments)) {
            event.setDamage(event.getDamage() * (1 + ((double) CEnchantments.BARBARIAN.getChance() / 100)));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player player)) return;

            enchantmentBookSettings.createCooldown(CEnchantments.BLEED.getEnchantment(), item, damager.getUniqueId(), 2000L, 2L);

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 5.0F);

            List<BukkitTask> bleedTasks = new ArrayList<>();

            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.spawnParticle(Particle.DUST, player.getLocation(), 12, dustOptions), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.damage(event.getDamage() / (enchantmentBookSettings.getLevel(item, CEnchantments.BLEED.getEnchantment()) * 1.05)), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEVOUR, damager, item, enchantments)) {
            while (EnchantUtils.isEventActive(CEnchantments.BLEED, damager, item, enchantments)) {
                if (!(event.getEntity() instanceof Player player)) return;
                player.damage(event.getDamage() * (1 + ((double) CEnchantments.DEVOUR.getChance() / 10)));
                damager.sendMessage("** Devour - BLEED STACK **");
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLACKSMITH, damager, item, enchantments)) {
            ItemStack[] equipment = damager.getEquipment().getArmorContents();
            for (ItemStack armor : equipment) {
                if (armor == null) return;
                ItemMeta meta = armor.getItemMeta();
                Damageable damageable = (Damageable) meta;
                int modifier = damageable.getDamage() - (2 + enchant.getLevel("Blacksmith"));
                if (modifier < 0) return;
                damageable.setDamage(modifier);
                armor.setItemMeta(meta);
                damager.playSound((net.kyori.adventure.sound.Sound) Sound.BLOCK_CALCITE_BREAK);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.ARROWBREAK, damager, item, enchantments)) {
            if (event.getDamageSource().getDamageType().equals(DamageType.ARROW)) {
                event.setCancelled(true);
                damager.playSound((net.kyori.adventure.sound.Sound) Sound.BLOCK_ANVIL_DESTROY);
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEEPBLEED, damager, item, enchantments)) {
            enchantmentBookSettings.createCooldown(CEnchantments.DEEPBLEED.getEnchantment(), item, damager.getUniqueId(), 500L, 1L);

            if (!(event.getEntity() instanceof Player player)) return;

            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 5.0F);

            List<BukkitTask> bleedTasks = new ArrayList<>();

            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.spawnParticle(Particle.DUST, player.getLocation(), 12, dustOptions), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.damage(event.getDamage() / (enchantmentBookSettings.getLevel(item, CEnchantments.DEEPBLEED.getEnchantment()) * 1.75)), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendMessage("You are bleeding!"), 40L, 20L));
            bleedTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> damager.sendMessage("** BLEED **"), 40L, 20L));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (BukkitTask task : bleedTasks) {
                    task.cancel();
                }
            }, 80L);
        }
        //Imperium
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        if (player.getKiller() == null) return;

        if (!this.pluginSupport.allowCombat(player.getLocation())) return;

        final Player damager = player.getKiller();
        final ItemStack item = this.methods.getItemInHand(damager);

        if (EnchantUtils.isEventActive(CEnchantments.DECAPITATION, damager, item, this.enchantmentBookSettings.getEnchantments(item))) {
            event.getDrops().add(new ItemBuilder().setMaterial(Material.PLAYER_HEAD).setPlayerName(player.getName()).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        final Player killer = event.getEntity().getKiller();

        if (killer == null) return;

        final ItemStack item = this.methods.getItemInHand(killer);
        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        final  Material headMat = EntityUtils.getHeadMaterial(event.getEntity());

        if (headMat != null && !EventUtils.containsDrop(event, headMat)) {
            final double multiplier = this.crazyManager.getDecapitationHeadMap().getOrDefault(headMat, 0.0);

            if (multiplier != 0.0 && EnchantUtils.isEventActive(CEnchantments.DECAPITATION, killer, item, enchantments, multiplier)) {
                final ItemStack head = new ItemBuilder().setMaterial(headMat).build();

                event.getDrops().add(head);
            }
        }
    }


    private void removeBadPotions(final Player player) {
        List<PotionEffectType> bad = new ArrayList<>() {{
            add(PotionEffectType.BLINDNESS);
            add(PotionEffectType.NAUSEA);
            add(PotionEffectType.HUNGER);
            add(PotionEffectType.POISON);
            add(PotionEffectType.SLOWNESS);
            add(PotionEffectType.MINING_FATIGUE);
            add(PotionEffectType.WEAKNESS);
            add(PotionEffectType.WITHER);
        }};

        bad.forEach(player::removePotionEffect);
    }

    public Enchant getEnchant() {
        return enchant;
    }

    public void setEnchant(Enchant enchant) {
        this.enchant = enchant;
    }
}