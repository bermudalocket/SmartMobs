# SmartMobs

Looking for the javadocs? [Click here](https://bermudalocket.github.io/SmartMobs/).

## What is SmartMobs?

SmartMobs is a plugin for Spigot Minecraft servers running version 1.12.2. It allows server administrators to customize the hostile living entities -- aka mobs -- in specified worlds. A frequent criticism of vanilla hostiles is that they are simply too easy and lack much depth. This plugin changes that.

## Getting Started

Download the latest release into your server's `plugins` directory and start your server. The plugin will, by default, create a series of `.yml` files in `plugins/SmartMobs/`. The only file that should ever be edited is `config.yml`. This file stores the names of the worlds (others will be ignored by this plugin) as well as an option for colorizing death messages (which defaults to gray for readability). **Do not edit the other files unless you know what you are doing. It is very easy to cause problems with YAML parsing if you are not familiar with it, and the plugin's saved information regarding your world, zones, and other settings may be lost.**

When your server starts, you will notice that no living entities are spawning. This will be fixed as you configure the plugin.

## Zones

Zones are the backbone of this plugin. A zone represents an abstract manifold, explicitly 2-dimensional but implicitly 3-dimensional. There are currently two supported zone shapes: `CIRCLE` and `SQUARE`. Zones are defined 2-dimensionally. For example, when creating a circular zone, one specifies a center as an ordered pair `(x, z)` and a radius `r`. This constructs a 2-dimensional circle, but the plugin will treat it as a 3-dimnesional cylinder extending from `min(y)` to `max(y)`. Likewise for a square, which is treated as a rectangular prism.

You can create a zone with the following command: `/sm zone create <zone-name> <world> <color> <radius> <center-x> <center-z> <shape>`.
  * **world** - the name of the world in which to create the zone.
  * **color** - the [color](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/ChatColor.html) to represent the zone.
  * **radius** - the radius of the zone (as a non-negative integer smaller than half the world border radius).
  * **center-x** - the x-coordinate of the center of the zone, as a double (decimal).
  * **center-z** - the z-coordinate of the center of the zone, as a double (decimal).
  * **shape** - the [shape](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/util/Shape.html) of the zone.

Once created, hostile living entities should begin to spawn within the zone.

You may overlap zones, i.e. by creating a bullseye pattern of overlapping circular zones sharing a center. When an event occurs at a location, the plugin automatically determines the correct zone by choosing the applicable zone with the smallest radius. This also means that one may distribute smaller zones within the other zones.

## Special Attacks

The highlight of this plugin is the special attack system. Special attacks can be customized per-zone with the following command: `/sm zone <zone> attack <attack-name> <property> <value>`.
  * **zone** - the zone in which to modify the attack.
  * **attack-name** - the name of the attack.
  * **property** - the attack property.
  * **value** - the value of the property.

One may refer to the following list of special attacks and their properties:

| Attack        | Property           | Value  |
| ------------- |-------------| -----|
| [Bloat](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/Bloat.html)      | probability | [0.0, 1.0] |
|       | minReinforcements      |   [0, ∞) |
|  | maxReinforcements      |    [0, ∞) |
| [Chemistry](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/Chemistry.html) | probability | [0.0, 1.0] |
| | minReinforcements | [0, ∞) |
| | maxReinforcements | [0, ∞) |
| | choices | \{ x : x ∈ [PotionEffectType](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html) \} |
| [EnderDance](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/EnderDance.html) | probability | [0.0, 1.0] |
| | minReinforcements | [0, ∞) |
| | maxReinforcements | [0, ∞) |
| [Flamethrower](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/Flamethrower.html) | probability | [0.0, 1.0] |
| | minReinforcements | [0, ∞) |
| | maxReinforcements | [0, ∞) |
| [Greneggs](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/Greneggs.html) | probability | [0.0, 1.0] |
| | minReinforcements | [0, ∞) |
| | maxReinforcements | [0, ∞) |
| [Resurrection](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/Resurrection.html) | probability | [0.0, 1.0] |
| | minReinforcements | [0, ∞) |
| | maxReinforcements | [0, ∞) |

### Creating Custom Attacks

Customized special attacks may be created if one is proficient in Java. Simply create a new class that extends the [AbstractSpecialAttack](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/AbstractSpecialAttack.html) class and implement your attack logic. The constructor accepts the following input:

```java
AbstractSpecialAttack(java.lang.String name,
                      java.lang.Class<? extends org.bukkit.event.Event> eventClass,
                      AbstractModule... modules)
```
  * **name** - the name of your attack
  * **eventClass** - the class (which must extend [org.bukkit.event.Event](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/Event.html))
  * **modules** - the modules to implement.

The idea behind the attack system is simple and eliminates the use of timer runnables. When an instance of the specific `eventClass` is fired, the `AbstractSpecialAttack` will hear it (as it is a listener) and pass the event to your implementation of the `AbstractSpecialAttack#checkConditions(Event event)` method (in addition to conducting other eligibility checks, i.e. disallowing reinforcement mobs from conducting special attacks, and disallowing attacks in certain WorldGuard regions). This method must return a `boolean` which determines if the attack *could* happen.

The module system allows for easy plug-and-play designing of attacks. There are currently three types of modules:
1. **Randomizable** - allows an attack to have a probability filter, i.e. only occur 10% of the time. ([doc](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/module/Randomizable.html))
2. **Reinforceable** - allows an attack to create reinforcement entities. ([doc](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/module/Reinforceable.html))
3. **Itemizable** - allows an attack to pull choices from a list. ([doc](https://bermudalocket.github.io/SmartMobs/com/bermudalocket/smartmobs/attack/module/Itemizable.html))

These modules are specified during construction, i.e.:
```java
    public CreeperChemistry() {
        super("CreeperChemistry",
                EntityExplodeEvent.class,
                new Randomizable(),
                new Reinforceable(),
                new Itemizable() {
                    @Override
                    public boolean verifyType(String s) {
                        return EffectType.valueOf(s) != null;
                    }
                });
    }
```

This attack is named `CreeperChemistry`, it will listen for `EntityExplodeEvent`s, and it implements `Randomizable`, `Reinforceable`, and `Itemizable`. Note that when implementing `Itemizable` you must override the `Itemizable#verifyType` method to specify what kind of data you are looking for in the list. In this case, it only accepts strings that match an `EffectType` (which is just an custom enumerated version of `PotionEffectType`).

## Exemptions

Are there certain areas within your world that you do not want SmartMobs handling? If your server is running WorldGuard, you may instruct SmartMobs to ignore a WorldGuard region with the command `/sm exemptions`. To add or remove an exemption, run `/sm exemptions <create/remove> <wg-region-name>`. To list all currently exempted regions, run `/sm exemptions list`.

## Replacements

Want Ghasts to spawn in the overworld? Want Vindicators to spawn in The End? You are free to customize the mobs that spawn in your world with mob replacements. These replacements work on a per-zone, per-mob basis, and can be created with the command `/sm zone <zone> replacement create <from-type> <to-type> <probability>`. For example, in order to make Ghasts replace Zombies with a 100% success rate in zone `myzone`, one would run `/sm zone myzone replacement create zombie ghast 1`. To remove a replacement, run `/sm zone <zone> replacement remove <from-type>`. To list replacements, run `/sm zone <zone> replacement list`.

## Custom Drops

One may configure mobs to drop custom items per-zone upon death with the command `/sm drop <add/remove> <zone> <entityType>` while holding the custom drop **in your main hand**. Custom names, lore, and enchantments are supported and will save correctly.

## Attributes

Looking to customize the mobs on a deeper level? This plugin facilitates the strengthening and weakening of mobs on a per-zone basis with the command `/sm zone <zone> attribute <attribute> <value>`. The [supported attributes](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/attribute/Attribute.html) can be found in the Spigot javadocs. Note that the `value` in this command is applied as an **positive multiplier**, i.e. a value of `0.5` will be interpreted as `1 + 0.5`, aka an increase of 50%. Likewise a value of `-0.5` will be interpreted as `1 - 0.5`, aka a decrease of 50%.

You may also scale dropped xp by specifying `xp` as the attribute.

Note that SmartMobs includes attribute aliases for convenience:

| Spigot Attribute | SmartMobs Alias |
| --- | --- |
| GENERIC_MAX_HEALTH | HEALTH |
| GENERIC_ATTACK_SPEED | ATTACKSPEED |
| GENERIC_KNOCKBACK_RESISTANCE | KNOCKBACK |
| GENERIC_ATTACK_DAMAGE | ATTACK |
| GENERIC_ARMOR | DEFENSE |
| GENERIC_MOVEMENT_SPEED | SPEED |
| GENERIC_FOLLOW_RANGE | FOLLOW |

## Permissions

There is currently one master permission for this plugin, including access to `/sm`: `smartmobs.admin`, default for OP.

## Other Commands

`/sm gps` - lists all zones the player is currently standing in, presented in reverse order. This means that the smallest relevant zone is the first one seen, and the largest zone will require scrolling up in chat.

`/sm butcher <entity-type> <radius>` - silently kills all entities that match `entity-type` in a sphere of radius `radius`. The `entity-type` must be supplied as an [EntityType](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html). Also accepts `HOSTILE` for all hostile mobs and `ALL` for all entities (minus players, armor stands, item frames).

`/sm zone <zone> center <x> <z>` - modifies a zone's center coordinates.

`/sm reload` - reloads the configuration.
