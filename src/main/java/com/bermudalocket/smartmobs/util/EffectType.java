package com.bermudalocket.smartmobs.util;

import org.bukkit.potion.PotionEffectType;

/**
 * An enumeration of Bukkit's {@link PotionEffectType}s.
 */
public enum EffectType {

    ABSORBPTION(PotionEffectType.ABSORPTION),
    BLINDNESS(PotionEffectType.BLINDNESS),
    CONFUSION(PotionEffectType.CONFUSION),
    DAMAGE_RESISTANCE(PotionEffectType.DAMAGE_RESISTANCE),
    FAST_DIGGING(PotionEffectType.FAST_DIGGING),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE),
    GLOWING(PotionEffectType.GLOWING),
    HARM(PotionEffectType.HARM),
    HEAL(PotionEffectType.HEAL),
    HUNGER(PotionEffectType.HUNGER),
    INCREASE_DAMAGE(PotionEffectType.INCREASE_DAMAGE),
    INVISIBILITY(PotionEffectType.INVISIBILITY),
    JUMP(PotionEffectType.JUMP),
    LEVITATION(PotionEffectType.LEVITATION),
    NIGHT_VISION(PotionEffectType.NIGHT_VISION),
    POISON(PotionEffectType.POISON),
    REGENERATION(PotionEffectType.REGENERATION),
    SATURATION(PotionEffectType.SATURATION),
    SLOW_DIGGING(PotionEffectType.SLOW_DIGGING),
    SLOW(PotionEffectType.SLOW),
    SPEED(PotionEffectType.SPEED),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING),
    WEAKNESS(PotionEffectType.WEAKNESS),
    WITHER(PotionEffectType.WITHER);

    // ------------------------------------------------------------------------------------------------------
    /**
     * The Bukkit {@link PotionEffectType} corresponding to the selected EffectType.
     */
    private final PotionEffectType _bukkitType;

    // ------------------------------------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param type the {@link PotionEffectType}.
     */
    EffectType(PotionEffectType type) {
        _bukkitType = type;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Returns the Bukkit {@link PotionEffectType}.
     *
     * @return the Bukkit {@link PotionEffectType}.
     */
    public PotionEffectType getBukkitType() {
        return _bukkitType;
    }

    /**
     * Returns the EffectType corresponding to a given {@link PotionEffectType}. Essentially a valueOf()
     * method for a non-String.
     *
     * @param potionEffectType the {@link PotionEffectType}.
     * @return the corresponding EffectType.
     */
    public static EffectType of(PotionEffectType potionEffectType) {
        for (EffectType effectType : values()) {
            if (effectType.getBukkitType() == potionEffectType) {
                return effectType;
            }
        }
        return null;
    }

}
