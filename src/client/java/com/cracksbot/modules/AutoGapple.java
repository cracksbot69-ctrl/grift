package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutoGapple extends HackModule {
    private final NumberSetting health = addSetting(new NumberSetting("Health", 8, 2, 18, 1));
    private final BoolSetting preferEnchanted = addSetting(new BoolSetting("PreferGod", true));
    private int cooldown = 0;

    public AutoGapple() {
        super("AutoGapple", "Auto eat golden apple", ModuleCategory.COMBAT, GLFW.GLFW_KEY_B, true);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        LocalPlayer p = mc.player;
        if (p.getHealth() > health.getValue()) return;

        int slot = -1;
        int normalSlot = -1;

        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.ENCHANTED_GOLDEN_APPLE)) {
                slot = i;
                if (preferEnchanted.getValue()) break;
            } else if (p.getInventory().getItem(i).is(Items.GOLDEN_APPLE)) {
                if (normalSlot == -1) normalSlot = i;
            }
        }

        if (slot == -1) slot = normalSlot;
        if (slot == -1) return;

        p.getInventory().setSelectedSlot(slot);
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        cooldown = 40;
    }
}
