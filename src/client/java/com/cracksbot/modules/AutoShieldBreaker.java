package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.lwjgl.glfw.GLFW;

public class AutoShieldBreaker extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 3.5, 2.0, 6.0, 0.1));

    public AutoShieldBreaker() {
        super("ShieldBreak", "Auto axe vs shields", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        for (Player target : mc.level.players()) {
            if (target == p || !target.isAlive()) continue;
            if (p.distanceTo(target) > range.getValue()) continue;
            if (!target.isBlocking()) continue;

            // Switch to axe
            int axeSlot = findAxeSlot(p);
            if (axeSlot == -1) return;
            p.getInventory().setSelectedSlot(axeSlot);
            mc.gameMode.attack(p, target);
            p.swing(InteractionHand.MAIN_HAND);
            return;
        }
    }

    private int findAxeSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            Item item = p.getInventory().getItem(i).getItem();
            if (item instanceof AxeItem) return i;
        }
        return -1;
    }
}
