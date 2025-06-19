package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.CrazyManager;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.economy.CurrencyAPI;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.Messages;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.Enchant;
import com.badbones69.crazyenchantments.paper.api.events.RageBreakEvent;
import com.badbones69.crazyenchantments.paper.api.objects.CEPlayer;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EntityUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.BossBarController;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.*;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class SwordEnchantments implements Listener {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Server server = this.plugin.getServer();

    private final PluginManager pluginManager = this.server.getPluginManager();

    @NotNull
    private final Starter starter = this.plugin.getStarter();

    @NotNull
    private final CrazyManager crazyManager = this.plugin.getCrazyManager();

    @NotNull
    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    @NotNull
    private final Methods methods = this.starter.getMethods();

    private Enchant enchant;
    private CEnchantments cEnchantments;

    @NotNull
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    // Plugin Support.
    @NotNull
    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    @NotNull
    private final BossBarController bossBarController = this.plugin.getBossBarController();

    // Economy Management.
    @NotNull
    private final CurrencyAPI currencyAPI = this.starter.getCurrencyAPI();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getDamager().getUniqueId())) return;

        if (this.pluginSupport.isFriendly(event.getDamager(), event.getEntity())) return;

        if (this.crazyManager.isBreakRageOnDamageOn() && event.getEntity() instanceof Player player) {
            final CEPlayer cePlayer = this.crazyManager.getCEPlayer(player);

            if (cePlayer != null) {
                final RageBreakEvent rageBreakEvent = new RageBreakEvent(player, event.getDamager(), this.methods.getItemInHand(player));

                this.pluginManager.callEvent(rageBreakEvent);

                if (!rageBreakEvent.isCancelled() && cePlayer.hasRage()) {
                    cePlayer.getRageTask().cancel();
                    cePlayer.setRageMultiplier(0.0);
                    cePlayer.setRageLevel(0);
                    cePlayer.setRage(false);

                    rageInformPlayer(player, Messages.RAGE_DAMAGED, 0f);
                }
            }
        }

        if (!(event.getEntity() instanceof LivingEntity en)) return;
        if (!(event.getDamager() instanceof final Player damager)) return;

        final CEPlayer cePlayer = this.crazyManager.getCEPlayer(damager);
        final ItemStack item = this.methods.getItemInHand(damager);

        if (event.getEntity().isDead()) return;

        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);
        final boolean isEntityPlayer = event.getEntity() instanceof Player;

        if (isEntityPlayer && EnchantUtils.isEventActive(CEnchantments.DISARMER, damager, item, enchantments)) {
            final Player player = (Player) event.getEntity();

            final EquipmentSlot equipmentSlot = getSlot(this.methods.percentPick(4, 0));

            final EntityEquipment equipment = player.getEquipment();

            final ItemStack armor = switch (equipmentSlot) {
                case HEAD -> equipment.getHelmet();
                case CHEST -> equipment.getChestplate();
                case LEGS -> equipment.getLeggings();
                case FEET -> equipment.getBoots();
                default -> null;
            };

            if (armor != null) {
                switch (equipmentSlot) {
                    case HEAD -> equipment.setHelmet(null);
                    case CHEST -> equipment.setChestplate(null);
                    case LEGS -> equipment.setLeggings(null);
                    case FEET -> equipment.setBoots(null);
                }

                this.methods.addItemToInventory(player, armor);
            }
        }

        if (isEntityPlayer && EnchantUtils.isEventActive(CEnchantments.DISORDER, damager, item, enchantments)) {
            final Player player = (Player) event.getEntity();
            final Inventory inventory = player.getInventory();
            final List<ItemStack> items = new ArrayList<>();
            final List<Integer> slots = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                final ItemStack inventoryItem = inventory.getItem(i);

                if (inventoryItem != null) {
                    items.add(inventoryItem);

                    inventory.setItem(i, null);
                }

                slots.add(i);
            }

            Collections.shuffle(items);
            Collections.shuffle(slots);

            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(slots.get(i), items.get(i));
            }

            if (!Messages.DISORDERED_ENEMY_HOT_BAR.getMessageNoPrefix().isEmpty()) damager.sendMessage(Messages.DISORDERED_ENEMY_HOT_BAR.getMessage());
        }

        // Check if CEPlayer is null as plugins like citizen use Player objects.
        if (cePlayer != null && EnchantUtils.isEventActive(CEnchantments.RAGE, damager, item, enchantments)) {

            if (cePlayer.hasRage()) {
                cePlayer.getRageTask().cancel();

                if (cePlayer.getRageMultiplier() <= this.crazyManager.getRageMaxLevel())
                    cePlayer.setRageMultiplier(cePlayer.getRageMultiplier() + (enchantments.get(CEnchantments.RAGE.getEnchantment()) * crazyManager.getRageIncrement()));

                final int rageUp = cePlayer.getRageLevel() + 1;

                if (cePlayer.getRageMultiplier().intValue() >= rageUp) {
                    rageInformPlayer(damager, Messages.RAGE_RAGE_UP, Map.of("%Level%", String.valueOf(rageUp)), ((float) rageUp / (float) (this.crazyManager.getRageMaxLevel() + 1)));

                    cePlayer.setRageLevel(rageUp);
                }

                event.setDamage(event.getDamage() * cePlayer.getRageMultiplier());
            } else {
                cePlayer.setRageMultiplier(1.0);
                cePlayer.setRage(true);
                cePlayer.setRageLevel(1);

                rageInformPlayer(damager, Messages.RAGE_BUILDING, ((float) cePlayer.getRageLevel() / (float) this.crazyManager.getRageMaxLevel()));
            }

            cePlayer.setRageTask(new FoliaScheduler(this.plugin, null, cePlayer.getPlayer()) {
                @Override
                public void run() {
                    cePlayer.setRageMultiplier(0.0);
                    cePlayer.setRage(false);
                    cePlayer.setRageLevel(0);

                    rageInformPlayer(damager, Messages.RAGE_COOLED_DOWN, 0f);
                }
            }.runDelayed(80));
        }

        if (en instanceof Player player && EnchantUtils.isEventActive(CEnchantments.SKILLSWIPE, damager, item, enchantments)) {
            int amount = 4 + enchantments.get(CEnchantments.SKILLSWIPE.getEnchantment());

            if (player.getTotalExperience() > 0) {

                if (this.currencyAPI.getCurrency(player, Currency.XP_TOTAL) >= amount) {
                    this.currencyAPI.takeCurrency(player, Currency.XP_TOTAL, amount);
                } else {
                    player.setTotalExperience(0);
                }

                this.currencyAPI.giveCurrency(damager, Currency.XP_TOTAL, amount);
            }
        }

        if (damager.getHealth() > 0 && EnchantUtils.isEventActive(CEnchantments.LIFESTEAL, damager, item, enchantments)) {
            final int steal = enchantments.get(CEnchantments.LIFESTEAL.getEnchantment());
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.
            final double maxHealth = damager.getAttribute(Attribute.MAX_HEALTH).getValue(); //todo() deprecated

            if (damager.getHealth() + steal < maxHealth) damager.setHealth(damager.getHealth() + steal);

            if (damager.getHealth() + steal >= maxHealth) damager.setHealth(maxHealth);
        }

        if (EnchantUtils.isEventActive(CEnchantments.NUTRITION, damager, item, enchantments)) {
            if (damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())) <= 20) damager.setSaturation(damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())));

            if (damager.getSaturation() + (2 * enchantments.get(CEnchantments.NUTRITION.getEnchantment())) >= 20) damager.setSaturation(20);
        }

        if (damager.getHealth() > 0 && EnchantUtils.isEventActive(CEnchantments.VAMPIRE, damager, item, enchantments)) {
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.
            double maxHealth = damager.getAttribute(Attribute.MAX_HEALTH).getValue();

            if (damager.getHealth() + event.getDamage() / 2 < maxHealth) damager.setHealth(damager.getHealth() + event.getDamage() / 2);

            if (damager.getHealth() + event.getDamage() / 2 >= maxHealth) damager.setHealth(maxHealth);
        }

	//Imperium Enchants: Insomnia VVV
        if (EnchantUtils.isEventActive(CEnchantments.INSOMNIA, damager, item, enchantments)) {
            //get UUID
            UUID playerUUID = damager.getUniqueId();

            //get enchantment level
            int level = enchantmentBookSettings.getLevel(item, CEnchantments.INSOMNIA.getEnchantment());
            long cooldown = Math.max(10000L - (level * 300L), 3000L);
            int duration = (level * 800);  //0 + 40 per level;

            // Check if the player is on cooldown
            if (System.currentTimeMillis() - playerCooldowns.getOrDefault(playerUUID, 0L) < cooldown) {
                return; //skip if cooldown
            }

            //effects
            if (event.getEntity() instanceof LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
            }

            //start cooldown store time
            playerCooldowns.put(playerUUID, System.currentTimeMillis());
        }
        //Insomnia ^^^

        if (EnchantUtils.isEventActive(CEnchantments.BLINDNESS, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, enchantments.get(CEnchantments.BLINDNESS.getEnchantment()) - 1));
        }

        if (EnchantUtils.isEventActive(CEnchantments.CONFUSION, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 5 + (enchantments.get(CEnchantments.CONFUSION.getEnchantment())) * 20, 0));
        }

        if (EnchantUtils.isEventActive(CEnchantments.DOUBLEDAMAGE, damager, item, enchantments)) {
            event.setDamage((event.getDamage() * 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.EXECUTE, damager, item, enchantments)) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3 + (enchantments.get(CEnchantments.EXECUTE.getEnchantment())) * 20, 3));
        }

        if (EnchantUtils.isEventActive(CEnchantments.FASTTURN, damager, item, enchantments)) {
            event.setDamage(event.getDamage() + (event.getDamage() / 3));
        }

        if (EnchantUtils.isEventActive(CEnchantments.LIGHTWEIGHT, damager, item, enchantments)) {
            damager.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 5 * 20, enchantments.get(CEnchantments.LIGHTWEIGHT.getEnchantment()) - 1));
        }

        if (EnchantUtils.isEventActive(CEnchantments.OBLITERATE, damager, item, enchantments)) {
            event.getEntity().setVelocity(damager.getLocation().getDirection().multiply(2).setY(1.25));
        }

        if (EnchantUtils.isEventActive(CEnchantments.PARALYZE, damager, item, enchantments)) {
            for (LivingEntity entity : this.methods.getNearbyLivingEntities(2D, damager)) {
                EntityDamageEvent damageByEntityEvent = new EntityDamageEvent(entity, EntityDamageEvent.DamageCause.MAGIC, DamageSource.builder(DamageType.INDIRECT_MAGIC).withDirectEntity(damager).build(), 5D);

                this.methods.entityEvent(damager, entity, damageByEntityEvent);
            }

            en.getWorld().strikeLightningEffect(en.getLocation());
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2));
            en.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 3 * 20, 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.SLOWMO, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, enchantments.get(CEnchantments.SLOWMO.getEnchantment())));
        }

        if (EnchantUtils.isEventActive(CEnchantments.SNARE, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 0));
            en.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 3 * 20, 0));
        }

        if (EnchantUtils.isEventActive(CEnchantments.TRAP, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.VIPER, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, enchantments.get(CEnchantments.VIPER.getEnchantment())));
        }

        if (EnchantUtils.isEventActive(CEnchantments.WITHER, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 2 * 20, 2));
        }

        if (EnchantUtils.isEventActive(CEnchantments.FAMISHED, damager, item, enchantments)) {
            en.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 1));
        }
        //IMPERIUM
        if (EnchantUtils.isEventActive(CEnchantments.ENDERSLAYER, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof EnderMan) || !(event.getEntity() instanceof EnderDragon)) return;
            double damage = event.getDamage();
            event.setDamage(damage * ((double) CEnchantments.ENDERSLAYER.getChance() / 20));
        }
        if (EnchantUtils.isEventActive(CEnchantments.NETHERSLAYER, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Blaze) || !(event.getEntity() instanceof ZombifiedPiglin)) return;
            double damage = event.getDamage();
            event.setDamage(damage * ((double) CEnchantments.NETHERSLAYER.getChance() / 20));
        }
        if (EnchantUtils.isEventActive(CEnchantments.SHACKLE, damager, item, enchantments)) {
            if (event.getEntity() instanceof Player) return;
            Location playerPos = damager.getLocation();
            Vector vector = damager.getLocation().toVector().subtract(en.getLocation().toVector());
            vector.normalize().multiply(1);
            en.setVelocity(vector);
            
        }
        if (EnchantUtils.isEventActive(CEnchantments.GREATSWORD, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof Player target)) return;
            if (target.getActiveItem().getType().equals(Material.BOW)) {
                event.setDamage(event.getDamage() * (damager.getVelocity().normalize().length() / 2));
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.DOMINATE, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10, CEnchantments.DOMINATE.getChance() / 20));
        }
        if (EnchantUtils.isEventActive(CEnchantments.BLOCK, damager, item, enchantments)) {
            event.setCancelled(true);
            if (!(event.getEntity() instanceof Player target)) return;
            target.damage(4 + ((double) CEnchantments.BLOCK.getChance() / 11));
        }
        if (EnchantUtils.isEventActive(CEnchantments.DEMONIC, damager, item, enchantments)) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            if (target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) target.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }
        if (EnchantUtils.isEventActive(CEnchantments.DISTANCE, damager, item, enchantments)) {
            damager.setVelocity(damager.getLocation().getDirection().multiply(-2).normalize());
            damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, CEnchantments.DISTANCE.getChance() / 10, 1));
        }
        if (EnchantUtils.isEventActive(CEnchantments.INVERSION, damager, item, enchantments)) {
            event.setCancelled(true);
            damager.setHealth(damager.getHealth() + ((double) CEnchantments.INVERSION.getChance() / 10));
        }
        if (EnchantUtils.isEventActive(CEnchantments.SILENCE, damager, item, enchantments)) {
            if (!(en instanceof Player target)) return;

            int oldChance = (30 * (enchant.getLevel("Silence")));

            ItemStack[] playerItems = target.getInventory().getContents();

            for (ItemStack targetItem : playerItems) {
                @NotNull Map<CEnchantment, Integer> enchantmentIntegerMap = this.enchantmentBookSettings.getEnchantments(targetItem);
                @NotNull Set<CEnchantment> enchantmentSet = enchantmentIntegerMap.keySet();

                for (CEnchantment enchantment : enchantmentSet) {
                    @NotNull List<BukkitTask> silenceTask = new ArrayList<>();
                    silenceTask.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> enchantment.setChance(0), 20L, 20L));

                    for (BukkitTask task : silenceTask) {
                        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 60L);
                        enchantment.setChance(oldChance);
                    }
                }
            }
        }
        if (EnchantUtils.isEventActive(CEnchantments.STUN, damager, item, enchantments)) {
            if (!en.hasPotionEffect(PotionEffectType.SLOWNESS)) en.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, CEnchantments.STUN.getChance() / 7, enchant.getLevel("Stun")));
            if (!en.hasPotionEffect(PotionEffectType.WEAKNESS)) en.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, CEnchantments.STUN.getChance() / 7, enchant.getLevel("Stun")));
            if (damager.hasPotionEffect(PotionEffectType.SLOWNESS)) damager.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        if (EnchantUtils.isEventActive(CEnchantments.SWARM, damager, item, enchantments)) {
            //The more entities there are in an area, the higher the buff to damage.
            //Radius considered is raised by each level.
            //Maximum level should be 5.
            List<Entity> total = damager.getNearbyEntities(
                    5 + enchantmentBookSettings.getLevel(item, CEnchantments.SWARM.getEnchantment()),
                    5 + enchantmentBookSettings.getLevel(item, CEnchantments.SWARM.getEnchantment()),
                    5 + enchantmentBookSettings.getLevel(item, CEnchantments.SWARM.getEnchantment())
            );
            event.setDamage(event.getDamage() * ((double) total.size() / 2));
        }
        if (EnchantUtils.isEventActive(CEnchantments.INSOMNIA, damager, item, enchantments)) {
            //get UUID
            UUID playerUUID = damager.getUniqueId();


            //get enchantment level
            int level = enchantmentBookSettings.getLevel(item, CEnchantments.INSOMNIA.getEnchantment());
            long cooldown = Math.max(10000L - (level * 300L), 3000L);
            int duration = (level * 800);  //0 + 40 per level;

            // Check if the player is on cooldown
            if (System.currentTimeMillis() - playerCooldowns.getOrDefault(playerUUID, 0L) < cooldown) {
                return; //skip if cooldown
            }


            //effects
            if (event.getEntity() instanceof LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, enchantments.get(CEnchantments.INSOMNIA.getEnchantment()) - 1));
            }

            //start cooldown store time
            playerCooldowns.put(playerUUID, System.currentTimeMillis());
        }
        //IMPERIUM
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        final Player damager = event.getEntity().getKiller();
        final Player player = event.getEntity();
        final ItemStack item = this.methods.getItemInHand(damager);
        final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

        if (EnchantUtils.isEventActive(CEnchantments.HEADLESS, damager, item, enchantments)) {
            final ItemStack head = new ItemBuilder().setMaterial("PLAYER_HEAD").setPlayerName(player.getName()).build();

            event.getDrops().add(head);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            final Player damager = event.getEntity().getKiller();
            final ItemStack item = this.methods.getItemInHand(damager);
            final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(item);

            if (EnchantUtils.isEventActive(CEnchantments.INQUISITIVE, damager, item, enchantments)) {
                event.setDroppedExp(event.getDroppedExp() * (enchantments.get(CEnchantments.INQUISITIVE.getEnchantment()) + 1));
            }

            final Material headMat = EntityUtils.getHeadMaterial(event.getEntity());

            if (headMat != null && !EventUtils.containsDrop(event, headMat)) {
                final double multiplier = this.crazyManager.getDecapitationHeadMap().getOrDefault(headMat, 0.0);

                if (multiplier != 0.0 && EnchantUtils.isEventActive(CEnchantments.HEADLESS, damager, item, enchantments, multiplier)) {
                    ItemStack head = new ItemBuilder().setMaterial(headMat).build();
                    event.getDrops().add(head);
                }
			}

            // The entity that is killed is a player.
            if (event.getEntity() instanceof Player && EnchantUtils.isEventActive(CEnchantments.CHARGE, damager, item, enchantments)) {
                int radius = 4 + enchantments.get(CEnchantments.CHARGE.getEnchantment());
                damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));

                damager.getNearbyEntities(radius, radius, radius).stream().filter(entity ->
                        this.pluginSupport.isFriendly(entity, damager)).forEach(entity ->
                        ((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1)));
            }
            if (EnchantUtils.isEventActive(CEnchantments.KILLAURA, damager, item, enchantments)) {
                World world = event.getEntity().getWorld();
                Collection<LivingEntity> entities = world.getNearbyLivingEntities(event.getEntity().getLocation(), 1);
                for (LivingEntity entity : entities) {
                    if (entity instanceof Player) return;
                    entity.setHealth(0);
                }
            }
        }
    }

    private EquipmentSlot getSlot(final int slot) {
        return switch (slot) {
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> EquipmentSlot.HEAD;
        };
    }

    private void rageInformPlayer(final Player player, final Messages message, final Map<String, String> placeholders, final float progress) {
        if (message.getMessageNoPrefix().isBlank()) return;

        if (this.crazyManager.useRageBossBar()) {
            this.bossBarController.updateBossBar(player, message.getMessageNoPrefix(placeholders), progress);
        } else {
            player.sendMessage(message.getMessage(placeholders));
        }
    }

    private void rageInformPlayer(final Player player, final Messages message, final float progress) {
        if (message.getMessageNoPrefix().isBlank()) return;

        if (this.crazyManager.useRageBossBar()) {
            this.bossBarController.updateBossBar(player, message.getMessageNoPrefix(), progress);
        } else {
            player.sendMessage(message.getMessage());
        }
    }

    public void setEnchant(Enchant enchant) {
        this.enchant = enchant;
    }

    public CEnchantments getcEnchantments() {
        return cEnchantments;
    }

    public void setcEnchantments(CEnchantments cEnchantments) {
        this.cEnchantments = cEnchantments;
    }
}
