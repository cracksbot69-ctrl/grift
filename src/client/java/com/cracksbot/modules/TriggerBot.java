package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends HackModule {
    private final NumberSetting cps = addSetting(new NumberSetting("CPS", 12, 1, 20, 1));
    private long lastHit = 0;

    public TriggerBot() {
        super("TriggerBot", "Auto hit crosshair target", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        if (!(mc.hitResult instanceof EntityHitResult ehr)) return;

        Entity target = ehr.getEntity();
        if (!(target instanceof Player) || target == mc.player || !target.isAlive()) return;

        long now = System.currentTimeMillis();
        long interval = (long) (1000.0 / cps.getValue());
        if (now - lastHit < interval) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        lastHit = now;
    }
}
