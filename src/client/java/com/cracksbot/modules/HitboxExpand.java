package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class HitboxExpand extends HackModule {
    private final NumberSetting expand = addSetting(new NumberSetting("Size", 0.3, 0.1, 1.0, 0.05));

    public HitboxExpand() {
        super("HitboxExpand", "Bigger entity hitboxes", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!mc.options.keyAttack.consumeClick()) return;

        LocalPlayer p = mc.player;
        Vec3 eye = p.getEyePosition(1.0f);
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(3.0));

        Entity best = null;
        double bestDist = 3.0;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == p || !entity.isAlive()) continue;
            if (!(entity instanceof Player)) continue;

            // Expand hitbox by setting amount
            AABB box = entity.getBoundingBox().inflate(expand.getValue());
            var optional = box.clip(eye, end);
            if (optional.isPresent()) {
                double dist = eye.distanceTo(optional.get());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = entity;
                }
            }
        }

        if (best != null) {
            mc.gameMode.attack(p, best);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}
