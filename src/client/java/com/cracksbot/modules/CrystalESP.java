package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.lwjgl.glfw.GLFW;

public class CrystalESP extends HackModule {
    public CrystalESP() {
        super("CrystalESP", "Highlight end crystals", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal crystal && crystal.isAlive()) {
                double dist = mc.player.distanceTo(crystal);
                if (dist <= 12.0) {
                    crystal.setGlowingTag(true);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal) {
                entity.setGlowingTag(false);
            }
        }
    }
}
