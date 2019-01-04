package com.bermudalocket.smartmobs;

import com.bermudalocket.smartmobs.attack.AbstractSpecialAttack;
import com.bermudalocket.smartmobs.attack.SpecialAttackRegistry;
import com.bermudalocket.smartmobs.attack.module.AbstractModule;
import com.bermudalocket.smartmobs.attack.module.Gaussianizable;
import com.bermudalocket.smartmobs.attack.module.Itemizable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import com.bermudalocket.smartmobs.attack.module.Reinforceable;
import com.bermudalocket.smartmobs.util.Replacement;
import com.bermudalocket.smartmobs.util.Shape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.bermudalocket.smartmobs.Util.CYAN;
import static com.bermudalocket.smartmobs.Util.GRAY;

// ----------------------------------------------------------------------------------------------------------
/**
 * The command-handling class. I'm so sorry. Godspeed.
 *
 * TODO : refactor
 */
public final class Commands implements CommandExecutor {

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     */
    Commands() {
        SmartMobs.PLUGIN.getCommand("sm").setExecutor(this);
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * The command handler.
     *
     * @param commandSender the command sender.
     * @param command the command.
     * @param s the name of the command as a string.
     * @param args the array of arguments supplied.
     * @return true if the command was successfully interpreted.
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // --------------------------------------------------------------------------------------------------

        if (args.length == 1) {

            /*
            /sm gps
             */
            if (args[0].equals("gps")) {
                if (commandSender instanceof Player) {
                    Location loc = ((Player) commandSender).getLocation();

                    success(commandSender, "------------------------------");
                    warn(commandSender, "You are in the following zones:");
                    for (Zone zone : ZoneHandler.getZones(loc)) {
                        if (zone != null) {
                            success(commandSender, zone.getDescription());
                            warn(commandSender, "The following attacks are registered:");
                            printZoneAttacks(commandSender, zone);
                            warn(commandSender, "The following attributes have been customized:");
                            printZoneAttributes(commandSender, zone);
                        }
                    }
                    success(commandSender, "------------------------------");

                } else {
                    warn(commandSender, "You can only do that from in-game!");
                }
                return true;
            }

            /*
            /sm drop
             */
            if (args[0].equals("drop")) {
                success(commandSender, "/sm drop <add/remove> <zone> <entityType>");
                warn(commandSender, "Remember to hold the drop in your main hand!");
                return true;
            }

            /*
            /sm zone
             */
            if (args[0].equals("zone")) {
                success(commandSender, "/sm zone list");
                success(commandSender, "/sm zone <zone> <create/remove/attack/attribute/replacement/xp>");
                return true;
            }

            /*
            /sm exemptions
             */
            if (args[0].equals("exemptions")) {
                success(commandSender, "/sm exemptions <create/remove> <wg-region-name>");
                success(commandSender, "/sm exemptions <list>");
                return true;
            }

            /*
            /sm reload
             */
            if (args[0].equals("reload")) {
                Configuration.reload(true);
                success(commandSender, "Configuration reloaded.");
                return true;
            }

        }

        // --------------------------------------------------------------------------------------------------

        if (args.length == 2) {

            // /sm zone create <name> <world> <color> <radius> <center-x> <center-z> <shape>
            if (args[0].equals("zone") && args[1].equals("create")) {
                success(commandSender, "/sm zone create <name> <world> <color> <radius> <center-x> <center-z> <shape>");
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm exemptions list
             */
            if (args[0].equalsIgnoreCase("exemptions") && args[1].equalsIgnoreCase("list")) {
                success(commandSender, "The following exemption regions were found: "
                                        + String.join(", ", ExemptionHandler.getAllExemptions()));
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone list
             */
            if (args[0].equals("zone") && args[1].equals("list")) {
                Commands.warn(commandSender, "The following zones are registered:");
                for (Zone zone : ZoneHandler.getZones()) {
                    Commands.success(commandSender, zone.getDescription());
                }
                return true;
            }

        }

        // --------------------------------------------------------------------------------------------------

        if (args.length == 3) {

            /*
            /sm zone <zone> replacement
             */
            if (args[0].equals("zone") && args[2].equals("replacement")) {
                success(commandSender, "/sm zone <zone> replacement <list/create/remove>");
                return true;
            }

            /*
            /sm zone <zone> remove
             */
            if (args[0].equals("zone") && args[2].equals("remove")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    ZoneHandler.removeZone(zone);
                    success(commandSender, zone.getFormattedName() + " has been removed.");
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm butcher <EntityType> <radius>
             */
            if (args[0].equals("butcher")) {

                if (!(commandSender instanceof Player)) {
                    warn(commandSender, "You have to be in-game to do that!");
                    return true;
                }

                Player player = (Player) commandSender;
                String type = args[1];
                String radiusAsString = args[2];

                double radius;
                try {
                    radius = Double.valueOf(radiusAsString);
                } catch (Exception e) {
                    warn(commandSender, "The radius must be a double!");
                    return true;
                }

                Predicate<Entity> searchPredicate;
                if (type.equalsIgnoreCase("ALL")) {
                    searchPredicate = (e) -> e instanceof LivingEntity &&
                                             !(e instanceof ArmorStand) &&
                                             !(e instanceof ItemFrame) &&
                                             !(e instanceof Player);
                } else if (type.equalsIgnoreCase("HOSTILE")) {
                    searchPredicate = (e) -> e instanceof Monster ||
                                             e instanceof Ghast ||
                                             e instanceof Slime;
                } else {
                    EntityType entityType;
                    try {
                        entityType = EntityType.valueOf(type.toUpperCase());
                        searchPredicate = (e) -> e.getType() == entityType;
                        if (entityType == EntityType.PLAYER) {
                            throw new IllegalArgumentException();
                        }
                    } catch (Exception e) {
                        warn(commandSender, "That's not a valid entity type!");
                        return true;
                    }
                }

                int i = 0;
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (searchPredicate.test(entity)) {
                        entity.remove();
                        i++;
                    }
                }

                success(player, "Successfully removed " + i + " entities from around you.");
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm exemptions <create/remove> <wg-region-name>
             */
            if (args[0].equals("exemptions")) {
                String name = args[2];
                if (name == null) {
                    warn(commandSender, "You must specify a WorldGuard region name.");
                    return true;
                }

                if (args[1].equals("create")) {
                    ExemptionHandler.addWorldGuardExemption(name);
                    success(commandSender, "Excluding WorldGuard region " + name);
                } else if (args[1].equals("remove")) {
                    ExemptionHandler.removeWorldGuardExemption(name);
                    success(commandSender, "No longer excluding WorldGuard region " + name);
                }
                return true;
            }

        }

        // --------------------------------------------------------------------------------------------------

        if (args.length == 4) {

            /*
            sm attack <attack> enabled <true/false>
             */
            if (args[0].equals("attack") && args[2].equals("enabled")) {
                AbstractSpecialAttack attack = SpecialAttackRegistry.getAttack(args[1]);
                if (attack != null) {
                    boolean state;
                    try {
                        state = Boolean.valueOf(args[3]);
                    } catch (Exception e) {
                        warn(commandSender, "The state must be a boolean!");
                        return true;
                    }
                    attack.setEnabled(state);
                    success(commandSender, attack.getName() + " now " + ((state) ? "enabled" : "disabled"));
                }
                return true;
            }

            /*
            /sm zone <zone> xp <value>
             */
            if (args[0].equals("zone") && args[2].equals("xp")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    String xpAsString = args[3];

                    double xp;
                    try {
                        xp = Double.valueOf(xpAsString);
                        if (xp < 0) throw new NumberFormatException();
                    } catch (Exception e) {
                        warn(commandSender, "The xp scalar must be a positive decimal (double)!");
                        return true;
                    }
                    zone.setXpScalar(xp);
                    return true;
                }
            }

            /*
            /sm zone <zone> replacement create
             */
            if (args[0].equals("zone") && args[2].equals("replacement") && args[3].equals("create")) {
                success(commandSender, "/sm zone " + args[1] + " replacement create <fromType> <toType> <probability>");
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone <zone> replacement list
             */
            if (args[0].equals("zone") && args[2].equals("replacement") && args[3].equals("list")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    warn(commandSender, "The following replacements are configured for " + zone.getFormattedName() + ":");
                    Set<Replacement> replacements = ReplacementHandler.getReplacements(zone);
                    if (replacements == null) {
                        success(commandSender, "None!");
                    } else {
                        for (Replacement replacement : replacements) {
                            success(commandSender, replacement.getFrom() + " -> " + replacement.getTo() + " ("
                                    + replacement.getProbability() + ")");
                        }
                    }
                }
                return true;
            }

            /*
            /sm zone <zone> drop list
             */
            if (args[0].equals("zone") && args[2].equals("drop") && args[3].equals("list")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    Map<EntityType, HashSet<ItemStack>> drops = zone.getAllDrops();
                    if (drops != null) {
                        for (Map.Entry<EntityType, HashSet<ItemStack>> entry : drops.entrySet()) {
                            warn(commandSender, entry.getKey().toString() + ": ");
                            for (ItemStack i : entry.getValue()) {
                                success(commandSender, "-- " + i.toString());
                            }
                        }
                    } else {
                        success(commandSender, "That zone has no registered drops.");
                    }
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }

        }

        // --------------------------------------------------------------------------------------------------

        if (args.length == 5) {

            /*
            /sm zone <zone> center <x> <z>
             */
            if (args[0].equals("zone") && args[2].equals("center")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    double x, z;
                    try {
                        x = Double.valueOf(args[3]);
                        z = Double.valueOf(args[4]);
                    } catch (Exception e) {
                        warn(commandSender, "The center coordinates must be doubles!");
                        return true;
                    }
                    zone.setCenter(x, z);
                    success(commandSender, "Set center of zone " + zone.getFormattedName() + " to ("+x+", "+z+")");
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone <zone> replacement remove <entityType>
                 0     1        2         3        4
             */
            if (args[0].equals("zone") && args[2].equals("replacement") && args[3].equals("remove")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    EntityType from;
                    try {
                        from = EntityType.valueOf(args[4].toUpperCase());
                    } catch (Exception e) {
                        warn(commandSender, "That is not a valid entity type!");
                        return true;
                    }
                    if (ReplacementHandler.removeReplacement(zone, from)) {
                        success(commandSender, "Successfully removed replacement.");
                    } else {
                        warn(commandSender, "That entity type does not have a replacement in that zone!");
                    }
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone <zone> attribute <attribute> <value>
                  0     1       2          3         4
            */
            if (args[0].equals("zone") && args[2].equals("attribute")) {

                String zoneName = args[1];
                String attributeName = args[3];
                String valueAsString = args[4];

                Attribute attribute;
                try {
                    attribute = Attribute.valueOf(attributeName.toUpperCase());
                } catch (Exception e) {
                    try {
                        attribute = AttributeAliases.valueOf(attributeName.toUpperCase()).get();
                    } catch (Exception ignored) {
                        Commands.warn(commandSender, "That attribute does not exist.");
                        return true;
                    }
                }

                double value;
                try {
                    value = Double.valueOf(valueAsString);
                } catch (Exception e) {
                    Commands.warn(commandSender, "That value is not a double!");
                    return true;
                }

                Zone zone = ZoneHandler.getZone(zoneName);
                if (zone != null) {
                    zone.setScalar(attribute, value);
                    Commands.success(commandSender, "Attribute " + attribute.toString() + " set to " + value + " in " + zoneName + "!");
                } else {
                    Commands.warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone <zone> drop <add/remove> <entityType>
            */
            if (args[0].equals("zone") && args[2].equals("drop")) {
                String zoneName = args[1];
                String entityTypeAsString = args[4];

                Zone zone = ZoneHandler.getZone(zoneName);
                if (zone != null) {
                    EntityType entityType;
                    try {
                        entityType = EntityType.valueOf(entityTypeAsString.toUpperCase());
                    } catch (Exception e) {
                        warn(commandSender, "That entity type does not exist.");
                        return true;
                    }
                    if (entityType != null) {
                        if (!(commandSender instanceof Player)) {
                            warn(commandSender, "That cannot be done from the console.");
                            return true;
                        }
                        Player player = (Player) commandSender;
                        ItemStack mainHand = player.getEquipment().getItemInMainHand();
                        switch (args[3]) {
                            case "add":
                                if (mainHand != null && mainHand.getType() != Material.AIR) {
                                    zone.addCustomDrop(entityType, mainHand);
                                    success(player, "Custom drop added to " + zone.getFormattedName() + "!");
                                } else {
                                    warn(player, "There's nothing in your hand!");
                                }
                                break;
                            case "remove":
                                if (zone.removeCustomDrop(entityType, mainHand)) {
                                    success(commandSender, "Custom drop successfully removed.");
                                } else {
                                    warn(commandSender, "That entity type does not have that custom drop " +
                                                                "assigned!");
                                }
                                break;
                            default:
                                warn(commandSender, "/sm zone <zone> drop <add/clear> <entityType>");
                                break;
                        }
                    }
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }
        }

        // --------------------------------------------------------------------------------------------------

        if (args.length == 6) {

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone <zone> attack <attack> <property> <value>
                 0     1      2       3         4         5
             */
            if (args[0].equals("zone") && args[2].equals("attack")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    AbstractSpecialAttack attack = SpecialAttackRegistry.getAttack(args[3]);
                    if (attack != null) {

                        String attackName = attack.getName();
                        String propertyName = args[4];
                        String value = args[5];

                        if (propertyName.equalsIgnoreCase("pdf")) {                         // PROBABILITY DENSITY FUNCTION
                            // value is formatted as MEAN:STDEV
                            String[] splitValue = value.split(":");
                            try {
                                double mean = Double.valueOf(splitValue[0]);
                                double stdev = Double.valueOf(splitValue[1]);
                                Gaussianizable pdf = (Gaussianizable) attack.getModule(ModuleType.GAUSSIANIZABLE);
                                pdf.setDistribution(zone, mean, stdev);
                            } catch (Exception e) {
                                Arrays.stream(e.getStackTrace())
                                      .map(StackTraceElement::toString)
                                      .forEach(SmartMobs::log);
                            }
                        } else if (propertyName.equalsIgnoreCase("probability")) {                  // PROBABILITY
                            double probability = Double.valueOf(value);
                            Randomizable randomizable = (Randomizable) attack.getModule(ModuleType.RANDOMIZABLE);
                            randomizable.setProbability(zone, probability);
                            Commands.success(commandSender, "Probability for " + attackName + " set to " + probability
                                    + " in " + zone.getFormattedName() + "!");
                        } else if (propertyName.equalsIgnoreCase("minReinforcements")) {     // MIN REINFORCE
                            int min = Integer.valueOf(value);
                            Reinforceable reinforceable = (Reinforceable) attack.getModule(ModuleType.REINFORCEABLE);
                            reinforceable.setMinReinforcements(zone, min);
                            Commands.success(commandSender, "Minimum reinforcements for " + attackName + " set to " + min
                                    + " in " + zone.getFormattedName() + "!");
                        } else if (propertyName.equalsIgnoreCase("maxReinforcements")) {     // MAX REINFORCE
                            int max = Integer.valueOf(value);
                            Reinforceable reinforceable = (Reinforceable) attack.getModule(ModuleType.REINFORCEABLE);
                            reinforceable.setMaxReinforcements(zone, max);
                            Commands.success(commandSender, "Maximum reinforcements for " + attackName + " set to " + max
                                    + " in " + zone.getFormattedName() + "!");
                        } else if (propertyName.equalsIgnoreCase("choices")) {               // CHOICES (ITEMIZABLE)
                            String rawValue = value.replace("+", "").replace("-", "");
                            Itemizable itemizable = (Itemizable) attack.getModule(ModuleType.ITEMIZABLE);
                            if (!itemizable.verifyType(rawValue)) {
                                Commands.warn(commandSender, "That's not a valid choice type!");
                                return true;
                            }
                            if (value.indexOf("+") == 0) {
                                if (itemizable.add(zone, value.replace("+", ""))) {
                                    Commands.success(commandSender, "Choice " + value + " added for " + attackName + " in "
                                            + zone.getFormattedName());
                                } else {
                                    Commands.warn(commandSender, "That's already a choice!");
                                    return true;
                                }
                            } else if (value.indexOf("-") == 0) {
                                if (itemizable.remove(zone, value.replace("-", ""))) {
                                    Commands.success(commandSender, "Choice " + value + " removed for " + attackName + " in "
                                            + zone.getFormattedName());
                                } else {
                                    Commands.warn(commandSender, "That's not a choice!");
                                    return true;
                                }
                            } else {
                                Commands.warn(commandSender, "/sm-attack <attack> set choices to <+/-><item> in <zone>");
                                return true;
                            }
                        }

                    }
                }
                return true;
            }

        }

        if (args.length == 7) {

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone ZONE replacement create TYPE1 TYPE2 PERCENT-CHANCE
                0     1       2         3      4      5      6
             */
            if (args[0].equals("zone") && args[2].equals("replacement")) {
                Zone zone = ZoneHandler.getZone(args[1]);
                if (zone != null) {
                    if (args[3].equals("create")) {
                        String fromEntity = args[4];
                        String toEntity = args[5];
                        String percentAsString = args[6];

                        EntityType from;
                        EntityType to;
                        double probability;

                        try {
                            from = EntityType.valueOf(fromEntity.toUpperCase());
                        } catch (Exception e) {
                            warn(commandSender, fromEntity + " is not a valid entity type!");
                            return true;
                        }

                        try {
                            to = EntityType.valueOf(toEntity.toUpperCase());
                        } catch (Exception e) {
                            warn(commandSender, toEntity + " is not a valid entity type!");
                            return true;
                        }

                        try {
                            probability = Double.valueOf(percentAsString);
                        } catch (Exception e) {
                            warn(commandSender, "Probability must be a double!");
                            return true;
                        }

                        ReplacementHandler.addReplacement(zone, from, to, probability);
                        success(commandSender, "Successfully added replacement to " + zone.getFormattedName() + "!");
                        return true;
                    }
                } else {
                    warn(commandSender, "That zone doesn't exist!");
                }
                return true;
            }
        }

        if (args.length == 9) {

            // ----------------------------------------------------------------------------------------------
            /*
            /sm zone create <name> <world> <color> <radius> <center-x> <center-z> <shape>
             */
            if (args[0].equals("zone") && args[1].equals("create")) {
                String zoneName = args[2];
                String worldName = args[3];
                String colorName = args[4];
                String radiusAsString = args[5];
                String centerX = args[6];
                String centerZ = args[7];
                String shapeAsString = args[8];

                if (ZoneHandler.zoneExists(zoneName)) {
                    warn(commandSender, "A zone with that name already exists.");
                    return true;
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    warn(commandSender, "That world does not exist.");
                    return true;
                }

                int radius;
                try {
                    radius = Integer.valueOf(radiusAsString);
                    if (radius < 0 || radius > world.getWorldBorder().getSize()/2) {
                        throw new NumberFormatException();
                    }
                } catch (Exception e) {
                    warn(commandSender, "Radius must be a positive integer!");
                    return true;
                }

                double x;
                try {
                    x = Double.valueOf(centerX);
                } catch (Exception e) {
                    warn(commandSender, "The center x coordinate must be decimals (Doubles)!");
                    return true;
                }

                double z;
                try {
                    z = Double.valueOf(centerZ);
                } catch (Exception e) {
                    warn(commandSender, "The center z coordinate must be decimals (Doubles)!");
                    return true;
                }

                ChatColor color;
                try {
                    color = ChatColor.valueOf(colorName.toUpperCase());
                } catch (Exception e) {
                    warn(commandSender, "That is not a valid ChatColor.");
                    return true;
                }

                Shape shape;
                try {
                    shape = Shape.valueOf(shapeAsString.toUpperCase());
                } catch (Exception e) {
                    warn(commandSender, "Valid shapes are CIRCLE and SQUARE.");
                    return true;
                }

                Zone zone = new Zone(zoneName, world, color, radius, x, z, shape);
                ZoneHandler.addZone(zone);
                success(commandSender, "Zone created successfully.");
                Configuration.reload(true);
                return true;
            }

        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Sends a success message.
     *
     * @param commandSender the recipient.
     * @param message the message.
     */
    private static void success(CommandSender commandSender, String message) {
        commandSender.sendMessage(PREFIX + " " + GRAY + message);
    }

    /**
     * Sends a failure/warning message. Also used for emphasis.
     *
     * @param commandSender the recipient.
     * @param message the message.
     */
    private static void warn(CommandSender commandSender, String message) {
        commandSender.sendMessage(ChatColor.DARK_RED + "[SmartMobs]" + ChatColor.RED + " " + message +
                                  ChatColor.WHITE);
    }

    /**
     * Messages details about the given zone's attacks to the recipient.
     *
     * @param commandSender the recipient.
     * @param zone the zone.
     */
    private void printZoneAttacks(CommandSender commandSender, Zone zone) {
        int i = 0;
        for (AbstractSpecialAttack a : SpecialAttackRegistry.getAttacks()) {
            String attackInfo = rainbow[i % 6] + a.getName().toUpperCase() + " " + GRAY + "[";
            for (AbstractModule module : a.getModules()) {
                attackInfo += module.getDescription(zone) + " | ";
            }
            attackInfo += GRAY + "]";
            success(commandSender, attackInfo);
            i++;
        }
    }

    /**
     * Messages details about the given zone's mob attributes to the recipient.
     *
     * @param commandSender the recipient.
     * @param zone the zone.
     */
    private void printZoneAttributes(CommandSender commandSender, Zone zone) {
        Map<Attribute, Double> scalars = zone.getScalars();
        if (scalars.isEmpty()) {
            success(commandSender, GRAY + "None!");
        } else {
            for (Attribute attribute : scalars.keySet()) {
                success(commandSender, attribute.toString() + ": " + scalars.get(attribute));
            }
        }
    }

    /**
     * Stores this plugin's colorized prefix for messages.
     */
    private static final String PREFIX = String.format("%s[%s%sSmartMobs%s%s]%s",
            GRAY, ChatColor.BOLD, CYAN, ChatColor.RESET, GRAY, ChatColor.WHITE);

    /**
     * Stores the rainbow (ROYGBIV) for colorizing results.
     */
    private static ChatColor[] rainbow = new ChatColor[7];

    static {
        rainbow[0] = ChatColor.RED;
        rainbow[1] = ChatColor.GOLD;
        rainbow[2] = ChatColor.YELLOW;
        rainbow[3] = ChatColor.DARK_GREEN;
        rainbow[4] = ChatColor.DARK_BLUE;
        rainbow[5] = ChatColor.DARK_PURPLE;
        rainbow[6] = ChatColor.LIGHT_PURPLE;
    }

    /**
     * Enumeration of attribute aliases for easier command entering.
     */
    private enum AttributeAliases {

        HEALTH(Attribute.GENERIC_MAX_HEALTH),
        ATTACKSPEED(Attribute.GENERIC_ATTACK_SPEED),
        KNOCKBACK(Attribute.GENERIC_KNOCKBACK_RESISTANCE),
        ATTACK(Attribute.GENERIC_ATTACK_DAMAGE),
        DEFENSE(Attribute.GENERIC_ARMOR),
        SPEED(Attribute.GENERIC_MOVEMENT_SPEED),
        FOLLOW(Attribute.GENERIC_FOLLOW_RANGE);

        private final Attribute _attribute;

        AttributeAliases(Attribute attribute) {
            _attribute = attribute;
        }

        Attribute get() {
            return _attribute;
        }

    }

}
