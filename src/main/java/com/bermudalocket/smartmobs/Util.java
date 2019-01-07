package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.util.ManagedWorld;
import nu.nerd.entitymeta.EntityMeta;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// ----------------------------------------------------------------------------------------------------------
/**
 * A utility class that desperately needs to be refactored.
 */
public final class Util {

    // ------------------------------------------------------------------------------------------------------
    // Math
    // ------------------------------------------------------------------------------------------------------
    /**
     * Formats decimal values (specifically mob health nametag values) to the tenths place.
     */
    private static final DecimalFormat TENTHS = new DecimalFormat("#.#");

    /**
     * An array storing the sine of each index, i.e. SIN_TABLE[180] = 0.
     */
    private static final double[] SIN_TABLE = new double[361];

    /**
     * An array storing the cosine of each index, i.e. COS_TABLE[180] = -1.
     */
    private static final double[] COS_TABLE = new double[361];

    /**
     * Holds the constant sqrt(2)/2.
     */
    private static final double RADICAL_TWO_OVER_TWO = Math.sqrt(2)/2;

    static {
        // populate SIN_TABLE and COS_TABLE
        for (int i = 0; i <= 360; i++) {
            SIN_TABLE[i] = Math.sin(i * (Math.PI / 180));
            COS_TABLE[i] = Math.cos(i * (Math.PI / 180));
        }
    }

    /**
     * Returns the sine of the input as a double.
     *
     * @param degrees the degree argument as an integer.
     * @return the sine of the degree argument as a double.
     */
    private static double sin(int degrees) {
        return (degrees < 0) ? -1 * SIN_TABLE[(-1 * degrees) % 360] : SIN_TABLE[degrees % 360];
    }

    /**
     * Returns the cosine of the input as a double.
     *
     * @param degrees the degree argument as an integer.
     * @return the cosine of the degree argument as a double.
     */
    private static double cos(int degrees) {
        return (degrees < 0) ? COS_TABLE[(-1 * degrees) % 360] : COS_TABLE[degrees % 360];
    }

    /**
     * Generates a random integer between min and max.
     *
     * @param min the lower bound.
     * @param max the upper bound.
     * @return a random integer between min and max.
     */
    public static int random(int min, int max) {
        return (int) (min + (max - min) * Math.random());
    }

    /**
     * Generates a random double between min and max.
     *
     * @param min the lower bound.
     * @param max the upper bound.
     * @return a random double between min and max.
     */
    public static double random(double min, double max) {
        return min + (max - min) * Math.random();
    }

    // ------------------------------------------------------------------------------------------------------
    // Minecraft
    // ------------------------------------------------------------------------------------------------------

    /**
     * Represents the number of ticks per second on a Minecraft server.
     */
    public static final int TPS = 20;

    /**
     * Convenience reference to the {@link ChatColor} "DARK_AQUA," aka cyan.
     */
    static final ChatColor CYAN = ChatColor.DARK_AQUA;

    /**
     * Convenience reference to the {@link ChatColor} "GRAY," aka light gray.
     */
    static final ChatColor GRAY = ChatColor.GRAY;

    /**
     * A set of {@link EntityType}s considered livestock, whose natural spawns should be blocked.
     * TODO : make dynamic in Configuration
     */
    static final Set<EntityType> PASSIVE_MOBS = new HashSet<>(Arrays.asList(
        EntityType.CHICKEN, EntityType.COW, EntityType.MUSHROOM_COW, EntityType.SHEEP,
        EntityType.RABBIT, EntityType.HORSE, EntityType.MULE, EntityType.DONKEY,
        EntityType.LLAMA
    ));

    /**
     * Represents a small attribute modifier, specifically a 40% decrease.
     */
    private static final AttributeModifier SMALL_MODIFIER = new AttributeModifier("small", -0.4, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    /**
     * Represents a large attribute modifier, specifically a 90% decrease.
     */
    private static final AttributeModifier LARGE_MODIFIER = new AttributeModifier("large", -0.9, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    /**
     * Returns the skull of the given player as an {@link ItemStack}.
     *
     * @param playerName the name of the player.
     * @return the player's skull.
     */
    static ItemStack getPlayerHead(String playerName) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(playerName);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Parses an EntityDamageByEntityEvent for any mobs, specifically an Entity that is a
     * LivingEntity but not a Player. Basically, this method is called getWhateverIsntAPlayer.
     *
     * @param event the event to parse.
     * @return the result.
     */
    public static LivingEntity parseEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity e = event.getEntity();
        Entity d = event.getDamager();
        if ((e instanceof Player && d instanceof Player)) {
            return null;
        } else if (!(e instanceof Player) && e instanceof LivingEntity) {
            return (LivingEntity) e;
        } else if (!(d instanceof Player) && d instanceof LivingEntity) {
            return (LivingEntity) d;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the entity's original name. Preserves custom names for display.
     *
     * @param livingEntity the entity.
     * @return the entity's original name.
     */
    private static String getOriginalCustomName(LivingEntity livingEntity) {
        return (String) EntityMeta.api().get(livingEntity, SmartMobs.PLUGIN, "original-name");
    }

    /**
     * Formats the given EntityType to be UpperCamelCase, and replaces underscores with a space.
     *
     * @param entityType the entity type.
     * @return the human-readable formatted name.
     */
    static String formatEntityType(EntityType entityType) {
        String s = entityType.toString().replace("_", " ");
        return WordUtils.capitalize(s.toLowerCase());
    }

    /**
     * Updates a mob's custom name (nametag) to show its current health, max health, and custom name.
     * If the entity was not spawned with a custom name, it will display its formatted EntityType.
     *
     * @param livingEntity the entity to update.
     */
    static void updateMobHealthNametag(LivingEntity livingEntity) {
        Zone zone = ZoneHandler.getSpawnZone(livingEntity);
        if (zone != null) {

            // is world configured to show nametags?
            ManagedWorld world = Configuration.adapt(zone.getWorld());
            if (world == null || !world.showHealthNametags()) {
                return;
            }

            String originalName = getOriginalCustomName(livingEntity);
            if (originalName == null) {
                originalName = livingEntity.getType().toString().toLowerCase();
                originalName = WordUtils.capitalize(originalName);
            }
            double health = livingEntity.getHealth();
            double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            final String name = String.format("%s[%s%s%s/%s%s%s]%s %s", ChatColor.WHITE,
                                                                      zone.getColor(),
                                                                      TENTHS.format(health),
                                                                      ChatColor.WHITE,
                                                                      zone.getColor(),
                                                                      TENTHS.format(maxHealth),
                                                                      ChatColor.WHITE,
                                                                      zone.getColor(),
                                                                      originalName);
            livingEntity.setCustomName(name);
        }
    }

    /**
     * Configures the given entity to be a reinforcement by weakening it, clearing equipment, and tagging
     * its metadata.
     *
     * @param livingEntity the entity to configure as a reinforcement.
     */
    public static void configureReinforcement(LivingEntity livingEntity) {
        applyModifier(livingEntity, Attribute.GENERIC_KNOCKBACK_RESISTANCE, LARGE_MODIFIER);
        applyModifier(livingEntity, Attribute.GENERIC_ARMOR, SMALL_MODIFIER);
        applyModifier(livingEntity, Attribute.GENERIC_ARMOR_TOUGHNESS, SMALL_MODIFIER);
        applyModifier(livingEntity, Attribute.GENERIC_ATTACK_DAMAGE, SMALL_MODIFIER);

        try {
            livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(LARGE_MODIFIER);
            livingEntity.setHealth(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } catch (Exception ignored) {
            // a thrown exception means that the given entity's type does not support that attribute.
        }

        livingEntity.getEquipment().clear();

        if (livingEntity instanceof Zombie) {
            ((Zombie) livingEntity).setBaby(true);
        }

        EntityMeta.api().set(livingEntity, SmartMobs.PLUGIN, "reinforcement", true);

        updateMobHealthNametag(livingEntity);
    }

    /**
     * Returns true if the given entity is a reinforcement.
     *
     * @param livingEntity the entity.
     * @return true if the given entity is a reinforcement.
     */
    public static boolean isReinforcement(LivingEntity livingEntity) {
        Object o = EntityMeta.api().get(livingEntity, SmartMobs.PLUGIN, "reinforcement");
        if (o != null) {
            return (boolean) o;
        } else {
            return false;
        }
    }

    /**
     * Applies a given attribute modifier to the entity's attribute.
     *
     * @param livingEntity the entity.
     * @param attribute the attribute.
     * @param modifier the modifier.
     */
    private static void applyModifier(LivingEntity livingEntity, Attribute attribute, AttributeModifier modifier) {
        try {
            livingEntity.getAttribute(attribute).addModifier(modifier);
        } catch (Exception ignored) { }
    }

    // ------------------------------------------------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns a random element from the given collection, or null if the collection is empty.
     *
     * @param collection the collection.
     * @return a random element from the given collection, or null if the collection is empty.
     */
    static Object getRandomElement(Collection collection) {
        int N = collection.size();
        int n = 1;
        int r = random(1, N);
        for (Object o : collection) {
            if (r == n) {
                return o;
            }
            n++;
        }
        return null;
    }

    /**
     * Summons an entity and applies a random velocity vector.
     *
     * @param origin the location to spawn the entity.
     * @param entityType the entity type.
     * @return the entity.
     */
    public static Entity summonEntityWithVelocity(Location origin, EntityType entityType) {
        double yaw = 2.0 * Math.PI * Math.random();
        double x = RADICAL_TWO_OVER_TWO * cos((int) yaw);
        double z = RADICAL_TWO_OVER_TWO * sin((int) yaw);

        Location spawnLoc = origin.clone().add(2 * x, 0.5, 2 * z);
        LivingEntity entity = (LivingEntity) origin.getWorld().spawnEntity(spawnLoc, entityType);
        double speed = Math.random();
        Vector velocity = new Vector(speed * x, speed * RADICAL_TWO_OVER_TWO, speed * z);
        entity.setVelocity(velocity);
        configureReinforcement(entity);
        return entity;
    }

}