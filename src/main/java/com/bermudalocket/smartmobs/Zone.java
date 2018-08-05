package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.util.OrderedPair;
import com.bermudalocket.smartmobs.util.Shape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiFunction;

import static com.bermudalocket.smartmobs.Util.CYAN;
import static com.bermudalocket.smartmobs.Util.GRAY;

// ----------------------------------------------------------------------------------------------------------
/**
 * Represents an arbitrary zone. A zone is an explicitly 2-dimensional but implicitly 3-dimensional manifold
 * with a defined center and radius.
 */
public final class Zone {

    // ------------------------------------------------------------------------------------------------------
    /**
     * The zone's name.
     */
    private String _name = "Default";

    /**
     * The zone's color.
     */
    private ChatColor _color = ChatColor.GREEN;

    /**
     * The center coordinates of this zone as an ordered pair (x, y).
     */
    private OrderedPair<Double> _center = new OrderedPair<>(0d, 0d);

    /**
     * The zone's radius, in whole blocks.
     */
    private int _radius = 10;

    /**
     * The world in which this zone is located.
     */
    private World _world;

    /**
     * The shape of this zone.
     */
    private Shape _shape = Shape.CIRCLE;

    /**
     * This zone's xp scalar.
     */
    private double _xpScalar = 1;

    /**
     * Stores this zone's attribute scalars.
     */
    private final Map<Attribute, Double> _scalarMap = new HashMap<>();

    /**
     * Stores this zone's custom drops.
     */
    private final Map<EntityType, HashSet<ItemStack>> _customDrops = new HashMap<>();

    /**
     * A function that tests if a coordinate pair is within the boundaries of this zone.
     */
    private final BiFunction<Double, Double, Boolean> _circleFunction = (x, y) -> {
        double xOffset = x - _center.getX();
        double yOffset = y - _center.getY();
        return xOffset*xOffset + yOffset*yOffset <= _radius*_radius;
    };

    /**
     * A function that tests if a coordinate pair is within the boundaries of this zone.
     */
    private final BiFunction<Double, Double, Boolean> _squareFunction =
            (x, y) ->
               x <= _center.getX() + _radius
            && x >= _center.getX() - _radius
            && y <= _center.getY() + _radius
            && y >= _center.getY() - _radius;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor. Represents an arbitrary zone. A zone is an explicitly 2-dimensional but implicitly
     * 3-dimensional manifold with a defined center and radius.
     *
     * @param name the zone's name.
     * @param world the world in which this zone is located.
     * @param color the zone's color.
     * @param radius the zone's radius, in whole blocks.
     * @param x the x-coordinate of the zone center.
     * @param z the z-coordinate of the zone center.
     * @param shape the {@link Shape} of the zone.
     */
    public Zone(String name, World world, ChatColor color, int radius, double x, double z, Shape shape) {
        _name = name;
        _world = world;
        _color = color;
        _radius = radius;
        _center = new OrderedPair<>(x, z);
        _shape = shape;
    }

    /**
     * Constructor. Represents an arbitrary zone. A zone is an explicitly 2-dimensional but implicitly
     * 3-dimensional manifold with a defined center and radius.
     *
     * @param configurationSection the configuration to load.
     */
    Zone(ConfigurationSection configurationSection) {
        load(configurationSection);
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns this zone's name.
     *
     * @return this zone's name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns this zone's color.
     *
     * @return this zone's color.
     */
    ChatColor getColor() {
        return _color;
    }

    /**
     * Returns this zone's name with its color applied. Resets its color to white.
     *
     * @return this zone's name with its color applied.
     */
    String getFormattedName() {
        return String.format("%s%s%s%s", _color, _name, ChatColor.RESET, GRAY);
    }

    /**
     * Returns a short description of this zone.
     *
     * @return a short description of this zone.
     */
    String getDescription() {
        return String.format("%s [%s%s%s, radius %s%s%s, center %s(%s, %s)%s, shape %s%s%s]",
                getFormattedName(),
                CYAN,
                _world.getName(),
                ChatColor.WHITE,
                CYAN,
                _radius,
                ChatColor.WHITE,
                CYAN,
                _center.getX(),
                _center.getY(),
                ChatColor.WHITE,
                CYAN,
                _shape.toString(),
                ChatColor.WHITE);
    }

    /**
     * Returns this zone's radius.
     *
     * @return this zone's radius.
     */
    int getRadius() {
        return _radius;
    }

    /**
     * Set the center of this zone to new coordinates.
     *
     * @param x the x coordinate of the new center.
     * @param z the z coordinate of the new center.
     */
    void setCenter(double x, double z) {
        _center = new OrderedPair<>(x, z);
    }

    /**
     * Determines if this zone contains the given location.
     *
     * @param location the location.
     * @return if this zone contains the location.
     */
    boolean contains(Location location) {
        if (_world != location.getWorld()) return false;
        double x = location.getX();
        double z = location.getZ();
        switch (_shape) {
            default:
            case CIRCLE:
                return _circleFunction.apply(x, z);
            case SQUARE:
                return _squareFunction.apply(x, z);
        }
    }

    /**
     * Configures a LivingEntity's {@link AttributeInstance} based on this zone's attributes.
     *
     * @param livingEntity the LivingEntity to configure.
     */
    void configure(LivingEntity livingEntity) {
        if (livingEntity != null && !Util.PASSIVE_MOBS.contains(livingEntity.getType())) {
            for (Attribute attribute : Attribute.values()) {
                AttributeInstance attributeInstance = livingEntity.getAttribute(attribute);
                if (attributeInstance != null) {
                    double baseValue = attributeInstance.getBaseValue();
                    double scalar = _scalarMap.getOrDefault(attribute, 1.0);
                    double newBaseValue = (baseValue * scalar < 2048) ? baseValue * scalar : 2048;
                    attributeInstance.setBaseValue(newBaseValue);
                    if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                        livingEntity.setHealth(newBaseValue);
                    }
                }
            }
            Util.updateMobHealthNametag(livingEntity);
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // TODO refactor scalars out of this class
    /**
     * Sets the given {@link Attribute} to the given value. This only applies to mobs in this zone.
     *
     * @param attribute the attribute to set.
     * @param value the value.
     */
    void setScalar(Attribute attribute, double value) {
        _scalarMap.put(attribute, value);
    }

    /**
     * Sets this zone's XP scalar.
     *
     * @param scalar the xp scalar to set.
     */
    void setXpScalar(double scalar) {
        _xpScalar = scalar;
    }

    /**
     * Scales the given xp according to this zone's xp scalar.
     *
     * @return the xp scaled according to this zone's xp scalar.
     */
    int scaleXp(int xp) {
        return (int) (_xpScalar * xp);
    }

    /**
     * Returns a map of all of the scalars defined in this zone.
     *
     * @return a map of all of the scalars defined in this zone.
     */
    Map<Attribute, Double> getScalars() {
        return _scalarMap;
    }

    // ------------------------------------------------------------------------------------------------------
    // TODO refactor custom drops out of this class
    /**
     * Adds a custom drop to this zone for an entity type.
     *
     * @param entityType the entity type to drop the item(s).
     * @param itemStack the item(s).
     */
    void addCustomDrop(EntityType entityType, ItemStack itemStack) {
        HashSet<ItemStack> drops = _customDrops.get(entityType);
        if (drops == null) {
            drops = new HashSet<>();
        }
        drops.add(itemStack);
        _customDrops.put(entityType, drops);
    }

    /**
     * Get a custom drop for an entity type.
     *
     * @param entityType the entity type.
     * @return the custom drop, if one exists.
     */
    HashSet<ItemStack> getCustomDrops(EntityType entityType) {
        return getDrops(entityType);
    }

    /**
     * Removes an entity type's custom drop in this zone.
     *
     * @param entityType the entity type whose drop should be removed.
     * @return true if the entity type had a custom drop and it was removed.
     */
    boolean removeCustomDrop(EntityType entityType, ItemStack itemStack) {
        HashSet<ItemStack> drops = getDrops(entityType);
        boolean result = drops.removeIf(i -> i.equals(itemStack));
        if (result) {
            _customDrops.put(entityType, drops);
        }
        return result;
    }

    /**
     * Returns a set of all custom drops for a particular {@link EntityType}. May be empty but not null.
     *
     * @param entityType the entity type.
     * @return a set of all custom drops for the given entity type.
     */
    private HashSet<ItemStack> getDrops(EntityType entityType) {
        HashSet<ItemStack> drops = _customDrops.get(entityType);
        return (drops != null) ? drops : new HashSet<>();
    }

    /**
     * Returns all custom drops defined in this zone.
     *
     * @return all custom drops defined in this zone.
     */
    Map<EntityType, HashSet<ItemStack>> getAllDrops() {
        return _customDrops;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Writes this zone's data to a {@link ConfigurationSection}.
     *
     * @param zoneSection the {@link ConfigurationSection}.
     */
    void save(ConfigurationSection zoneSection) {
        zoneSection.set("name", _name);
        zoneSection.set("color", _color.name());
        zoneSection.set("world", _world.getName());
        zoneSection.set("radius", _radius);
        zoneSection.set("xp-scalar", _xpScalar);
        zoneSection.set("shape", _shape.toString());
        zoneSection.set("center-x", _center.getX());
        zoneSection.set("center-z", _center.getY());
        for (Map.Entry<Attribute, Double> entry : _scalarMap.entrySet()) {
            String key = "scalar." + entry.getKey().toString();
            Double value = entry.getValue();
            zoneSection.set(key, value);
        }
        for (Map.Entry<EntityType, HashSet<ItemStack>> entry : _customDrops.entrySet()) {
            String key = "drop." + entry.getKey().toString();
            zoneSection.set(key, new ArrayList<>(entry.getValue()));
        }
    }

    /**
     * Configures this zone based on data loaded from a {@link ConfigurationSection}.
     *
     * @param zoneSection the {@link ConfigurationSection}.
     */
    private void load(ConfigurationSection zoneSection) {
        _name = zoneSection.getString("name", "Default");
        _color = ChatColor.valueOf(zoneSection.getString("color", "GRAY"));
        _world = Bukkit.getWorld(zoneSection.getString("world"));
        _radius = zoneSection.getInt("radius", 10);
        _xpScalar = zoneSection.getInt("xp-scalar", 1);
        _shape = Shape.valueOf(zoneSection.getString("shape", "CIRCLE"));
        double x = zoneSection.getDouble("center-x");
        double z = zoneSection.getDouble("center-z");
        _center = new OrderedPair<>(x, z);
        SmartMobs.log("Loading zone " + getFormattedName() + " from saved data.");
        ConfigurationSection scalars = zoneSection.getConfigurationSection("scalar");
        if (scalars != null) {
            for (String key : scalars.getKeys(false)) {
                Attribute attribute = Attribute.valueOf(key);
                Double value = scalars.getDouble(key);
                _scalarMap.put(attribute, value);
            }
        }
        ConfigurationSection drops = zoneSection.getConfigurationSection("drop");
        if (drops != null) {
            for (String key : drops.getKeys(false)) {
                EntityType entityType = EntityType.valueOf(key);
                ArrayList<ItemStack> list = (ArrayList<ItemStack>) drops.getList(key);
                if (list != null) {
                    _customDrops.put(entityType, new HashSet<>(list));
                }
            }
        }
    }

}
