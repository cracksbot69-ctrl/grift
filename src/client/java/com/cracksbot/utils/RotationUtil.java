package com.cracksbot.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public final class RotationUtil {

    private RotationUtil() {}

    /**
     * Berechnet Yaw/Pitch von eyes → target.
     */
    public static float[] calcRotation(Vec3 eyes, Vec3 target) {
        Vec3 diff = target.subtract(eyes);
        double dist  = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float  yaw   = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float  pitch = (float) -Math.toDegrees(Math.atan2(diff.y, dist));
        return new float[]{ wrapYaw(yaw), clampPitch(pitch) };
    }

    /**
     * Silent Rotation: sendet Rotation-Paket zum Server ohne visuellen Snap.
     * Server registriert die Rotation für Hit/Place, Client-Ansicht unverändert.
     *
     * Verwendung: vor jeder Aktion aufrufen, danach restoreRotation().
     */
    public static void sendSilentRotation(Minecraft mc, float yaw, float pitch) {
        if (mc.getConnection() == null) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        mc.getConnection().send(
            new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision)
        );
    }

    /**
     * Setzt Rotation zurück auf echte Client-Rotation.
     */
    public static void restoreRotation(Minecraft mc) {
        if (mc.getConnection() == null || mc.player == null) return;
        LocalPlayer p = mc.player;
        mc.getConnection().send(
            new ServerboundMovePlayerPacket.Rot(p.getYRot(), p.getXRot(), p.onGround(), p.horizontalCollision)
        );
    }

    private static float wrapYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw >= 180.0f) yaw -= 360.0f;
        if (yaw < -180.0f) yaw += 360.0f;
        return yaw;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }
}
