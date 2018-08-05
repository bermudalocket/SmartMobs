package com.bermudalocket.smartmobs.attack;

import java.util.HashSet;
import java.util.Set;

public final class SpecialAttackRegistry {

    private static final Set<AbstractSpecialAttack> ATTACK_SET = new HashSet<>();

    private SpecialAttackRegistry() { }

    public static void register(AbstractSpecialAttack abstractSpecialAttack) {
        ATTACK_SET.add(abstractSpecialAttack);
    }

    public static AbstractSpecialAttack getAttack(String name) {
        for (AbstractSpecialAttack abstractSpecialAttack : ATTACK_SET) {
            if (abstractSpecialAttack.getName().equalsIgnoreCase(name)) {
                return abstractSpecialAttack;
            }
        }
        return null;
    }

    public static Set<AbstractSpecialAttack> getAttacks() {
        return ATTACK_SET;
    }

}