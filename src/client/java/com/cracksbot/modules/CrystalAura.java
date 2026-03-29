package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.lwjgl.glfw.GLFW;

public class CrystalAura extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 5.0, 2.0, 6.0, 0.5));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 10, 1, 20, 1));
    private long lastBreak = 0;

    public CrystalAura() {
        super("CrystalAura", "Break all crystals in range", ModuleCategory.COMBAT, GLFW.GLFW_KEY_X, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        long interval = (long)(1000.0 / speed.getValue());
        if (now - lastBreak < interval) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) continue;
            if (!crystal.isAlive()) continue;

            double dist = mc.player.distanceTo(crystal);
            if (dist <= range.getValue()) {
                mc.gameMode.attack(mc.player, crystal);
                mc.player.swing(InteractionHand.MAIN_HAND);
                lastBreak = now;
                return;
            }
        }
    }
}
