package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Velocity extends HackModule {
    private final NumberSetting horizontal = addSetting(new NumberSetting("Horizontal%", 0, 0, 100, 5));
    private final NumberSetting vertical = addSetting(new NumberSetting("Vertical%", 0, 0, 100, 5));
    private Vec3 lastVelocity = Vec3.ZERO;

    public Velocity() {
        super("Velocity", "Anti knockback", ModuleCategory.COMBAT, GLFW.GLFW_KEY_G, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        Vec3 current = mc.player.getDeltaMovement();
        double dx = Math.abs(current.x - lastVelocity.x);
        double dy = current.y - lastVelocity.y;
        double dz = Math.abs(current.z - lastVelocity.z);

        // Detect knockback (sudden horizontal velocity change)
        if (dx > 0.1 || dz > 0.1) {
            double hMul = horizontal.getValue() / 100.0;
            double vMul = vertical.getValue() / 100.0;

            // Calculate the knockback component and reduce it
            double kbX = current.x - lastVelocity.x;
            double kbY = dy > 0.05 ? current.y - lastVelocity.y : 0;
            double kbZ = current.z - lastVelocity.z;

            mc.player.setDeltaMovement(
                lastVelocity.x + kbX * hMul,
                current.y - kbY * (1.0 - vMul),
                lastVelocity.z + kbZ * hMul
            );
        }

        lastVelocity = mc.player.getDeltaMovement();
    }
}
