package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.lwjgl.glfw.GLFW;

public class AutoPot extends HackModule {
    private final NumberSetting health = addSetting(new NumberSetting("Health", 10, 2, 18, 1));

    public AutoPot() {
        super("AutoPot", "Auto use healing potions", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;
        if (p.getHealth() >= health.getValue()) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack.is(Items.SPLASH_POTION) || stack.is(Items.POTION)) {
                int prev = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(i);
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                p.getInventory().setSelectedSlot(prev);
                return;
            }
        }
    }
}
