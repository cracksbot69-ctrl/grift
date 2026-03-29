package com.cracksbot.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PredictionUtil {

    private PredictionUtil() {}

    /**
     * Sagt die Position des Gegners in `ticks` Ticks voraus.
     * Basiert auf aktueller Velocity (Differenz letzte Position → aktuelle Position).
     *
     * Genauigkeit: gut für 1 Tick, sinkt bei mehr Ticks da Friction/Gravity
     * nicht modelliert werden.
     */
    public static Vec3 predictPosition(Player target, int ticks) {
        Vec3 current  = target.position();
        Vec3 velocity = new Vec3(
            current.x - target.xOld,
            current.y - target.yOld,
            current.z - target.zOld
        );
        // Einfache lineare Extrapolation mit Friction-Dämpfung
        double friction = 0.91; // Luft-Friction in MC
        Vec3 predicted  = current;
        Vec3 vel        = velocity;
        for (int i = 0; i < ticks; i++) {
            predicted = predicted.add(vel);
            vel = vel.scale(friction);
        }
        return predicted;
    }

    /**
     * Berechnet Velocity des Gegners (Blöcke pro Tick).
     */
    public static Vec3 getVelocity(Player target) {
        Vec3 pos = target.position();
        return new Vec3(pos.x - target.xOld, pos.y - target.yOld, pos.z - target.zOld);
    }
}
