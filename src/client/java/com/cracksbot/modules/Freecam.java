package com.cracksbot.modules;

import com.cracksbot.settings.BoolSetting;
import com.cracksbot.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

public class Freecam extends HackModule {
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));
    private final BoolSetting noClip  = addSetting(new BoolSetting("NoClip", true));

    private ArmorStand ghost;
    private double savedX, savedY, savedZ;
    private float  savedYaw, savedPitch;

    public Freecam() {
        super("Freecam", "Detach camera from player without moving your actual position", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        LocalPlayer p = mc.player;
        savedX     = p.getX();
        savedY     = p.getY();
        savedZ     = p.getZ();
        savedYaw   = p.getYRot();
        savedPitch = p.getXRot();

        ghost = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
        ghost.setPos(savedX, savedY, savedZ);
        ghost.setYRot(savedYaw);
        ghost.setXRot(savedPitch);
        ghost.setNoGravity(true);
        ghost.setInvulnerable(true);
        ghost.setInvisible(true);
        if (noClip.getValue()) ghost.noPhysics = true;

        mc.setCameraEntity(ghost);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || ghost == null) return;

        // Keep player frozen at saved position
        mc.player.setPos(savedX, savedY, savedZ);
        mc.player.setDeltaMovement(0, 0, 0);

        // Move ghost camera with WASD
        double spd = speed.getValue() * 0.1;
        float yaw  = ghost.getYRot();
        float pitch = ghost.getXRot();

        double yawRad   = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double dx = 0, dy = 0, dz = 0;

        if (mc.options.keyUp.isDown()) {
            dx -= Math.sin(yawRad) * Math.cos(pitchRad) * spd;
            dy -= Math.sin(pitchRad) * spd;
            dz += Math.cos(yawRad) * Math.cos(pitchRad) * spd;
        }
        if (mc.options.keyDown.isDown()) {
            dx += Math.sin(yawRad) * Math.cos(pitchRad) * spd;
            dy += Math.sin(pitchRad) * spd;
            dz -= Math.cos(yawRad) * Math.cos(pitchRad) * spd;
        }
        if (mc.options.keyRight.isDown()) {
            dx += Math.cos(yawRad) * spd;
            dz += Math.sin(yawRad) * spd;
        }
        if (mc.options.keyLeft.isDown()) {
            dx -= Math.cos(yawRad) * spd;
            dz -= Math.sin(yawRad) * spd;
        }
        if (mc.options.keyJump.isDown()) dy += spd;
        if (mc.options.keyShift.isDown()) dy -= spd;

        ghost.setPos(ghost.getX() + dx, ghost.getY() + dy, ghost.getZ() + dz);

        // Sync camera rotation to player look direction
        ghost.setYRot(mc.player.getYRot());
        ghost.setXRot(mc.player.getXRot());
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.setPos(savedX, savedY, savedZ);
            mc.setCameraEntity(mc.player);
        }
        ghost = null;
    }
}
