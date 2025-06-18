package com.badbones69.crazyenchantments.paper.api.utils;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventUtils {
    
    private static final Set<Event> ignoredEvents = new HashSet<>();
    private static final Set<UUID> ignoredUUIDs = new HashSet<>();
    
    public static Set<Event> getIgnoredEvents() {
        return ignoredEvents;
    }

    public static boolean isIgnoredEvent(final Event event) {
        return ignoredEvents.contains(event);
    }

    public static void addIgnoredEvent(final Event event) {
        ignoredEvents.add(event);
    }

    public static void removeIgnoredUUID(final UUID uuid) {
        ignoredUUIDs.remove(uuid);
    }

    public static Set<UUID> getIgnoredUUIDs() {
        return ignoredUUIDs;
    }

    public static boolean isIgnoredUUID(final UUID uuid) {
        return ignoredUUIDs.contains(uuid);
    }

    public static void addIgnoredUUID(final UUID uuid) {
        ignoredUUIDs.add(uuid);
    }

    public static void removeIgnoredEvent(final Event event) {
        ignoredEvents.remove(event);
    }

    public static boolean containsDrop(final EntityDeathEvent event, final Material material) {
        boolean hasDroppedMat = true;

        if (material != null && !material.isAir()) {
            hasDroppedMat = false;

            for (final ItemStack drop : event.getDrops()) {
                if (drop.getType() == material) {
                    hasDroppedMat = true;

                    break;
                }
            }
        }

        return hasDroppedMat;
    }
}