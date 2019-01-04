package com.bermudalocket.smartmobs.attack;

import com.bermudalocket.smartmobs.Zone;
import com.bermudalocket.smartmobs.ZoneHandler;
import com.bermudalocket.smartmobs.attack.module.Gaussianizable;
import com.bermudalocket.smartmobs.attack.module.ModuleType;
import com.bermudalocket.smartmobs.attack.module.Randomizable;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;

// ----------------------------------------------------------------------------
/**
 * Ghast fireballs inflict blindness.
 */
public class Siren extends AbstractSpecialAttack {

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     */
    public Siren() {
        super("siren", ProjectileHitEvent.class, new Randomizable(), new Gaussianizable());
    }

    @Override
    boolean checkConditions(Event event) {
        ProjectileHitEvent e = (ProjectileHitEvent) event;
        if (e.getEntity() != null) {
            return e.getEntity().getShooter() instanceof Ghast && e.getHitEntity() instanceof Player;
        }
        return false;
    }

    @Override
    void doAttack(Event event) {
        ProjectileHitEvent e = (ProjectileHitEvent) event;
        Ghast ghast = (Ghast) e.getEntity().getShooter();
        Zone zone = ZoneHandler.getZone(ghast.getLocation());

        Player hitPlayer = (Player) e.getHitEntity();

        Randomizable randomizable = (Randomizable) getModule(ModuleType.RANDOMIZABLE);
        if (randomizable.randomize(zone)) {
            double effectDuration = ((Gaussianizable) getModule(ModuleType.GAUSSIANIZABLE)).randomize(zone);
            int duration = 20 * (int) Math.round(effectDuration);
            PotionEffectType.BLINDNESS.createEffect(duration, 1).apply(hitPlayer);
            summonCloud(hitPlayer.getLocation(), 3, 1, Color.PURPLE, Particle.DRAGON_BREATH);
            hitPlayer.playSound(hitPlayer.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1, 1);
        }
    }

}
