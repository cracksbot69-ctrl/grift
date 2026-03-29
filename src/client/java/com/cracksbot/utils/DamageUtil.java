package com.cracksbot.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class DamageUtil {

    private DamageUtil() {}

    /**
     * Berechnet Explosionsschaden nach exakter Vanilla-Formel.
     * Berücksichtigt: Armor, Armor-Toughness, Exposure-Raycasts.
     *
     * Blast-Protection NICHT eingerechnet (server-seitig, nicht zugänglich).
     */
    public static double calcExplosionDamage(Vec3 explosionPos, Player player) {
        Vec3 center = player.getBoundingBox().getCenter();
        double dist = explosionPos.distanceTo(center);
        if (dist > 12.0) return 0.0;

        double exposure = calcExposure(explosionPos, player);
        double impact   = (1.0 - dist / 12.0) * exposure;
        double damage   = (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;

        // Armor mitigation
        double armor     = player.getArmorValue();
        double toughness = player.getAttributeValue(
            net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        double armorMit  = Math.min(20.0,
            Math.max(armor / 5.0, armor - damage / (2.0 + toughness / 4.0)));
        damage *= (1.0 - armorMit / 25.0);

        return Math.max(0.0, damage);
    }

    /**
     * Exposure: Verhältnis von Raycasts die den Spieler treffen ohne Hindernis.
     * Höherer Wert = Crystal sieht den Spieler besser = mehr Schaden.
     */
    public static double calcExposure(Vec3 source, Entity entity) {
        AABB box = entity.getBoundingBox();
        double dx = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double dy = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double dz = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        if (dx <= 0 || dy <= 0 || dz <= 0) return 0.0;

        int total = 0, hit = 0;
        for (double fx = 0.0; fx <= 1.0; fx += dx) {
            for (double fy = 0.0; fy <= 1.0; fy += dy) {
                for (double fz = 0.0; fz <= 1.0; fz += dz) {
                    Vec3 point = new Vec3(
                        box.minX + (box.maxX - box.minX) * fx,
                        box.minY + (box.maxY - box.minY) * fy,
                        box.minZ + (box.maxZ - box.minZ) * fz
                    );
                    var clip = entity.level().clip(new ClipContext(
                        point, source,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, entity
                    ));
                    total++;
                    if (clip.getType() == HitResult.Type.MISS) hit++;
                }
            }
        }
        return total == 0 ? 0.0 : (double) hit / total;
    }

    /**
     * Gesamtes Leben inkl. Absorption (für FacePlace-Berechnung).
     */
    public static float getTotalHealth(LivingEntity e) {
        return e.getHealth() + e.getAbsorptionAmount();
    }
}
